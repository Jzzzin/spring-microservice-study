package com.bloknoma.zuulsvr.filters;

import com.bloknoma.zuulsvr.model.AbTestingRoute;
import com.bloknoma.zuulsvr.utils.UserContextHolder;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class SpecialRoutesFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(SpecialRoutesFilter.class);

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;

    @Autowired
    FilterUtils filterUtils;

    @Autowired
    RestTemplate restTemplate;

    // proxy helper @Autowired 붙이지 않으면 프록시가 제대로 작동하지 않는다.
    // private ProxyRequestHelper helper = new ProxyRequestHelper();
    @Autowired
    private ProxyRequestHelper helper;

    @Override
    public String filterType() {
        return FilterUtils.ROUTE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        // special route record 확인
        AbTestingRoute abTestRoute = getAbRoutingInfo(filterUtils.getServiceId());

        // special route record 사용 여부 판단
        if (abTestRoute != null && useSpecialRoute(abTestRoute)) {
            // special route 경로 생성
            String route = buildRouteString(ctx.getRequest().getRequestURI(),
                    abTestRoute.getEndpoint(), ctx.get("serviceId").toString());
            // special route 호출
            forwardToSpecialRoute(route);
        }
        return null;
    }

    // special route record 확인
    private AbTestingRoute getAbRoutingInfo(String serviceName) {
        ResponseEntity<AbTestingRoute> restExchange = null;
        try {
            // logger 추가
            logger.debug(">>> In SpecialRouterFilter.getAbRoutingInfo: {}. Thread Id: {}",
                    UserContextHolder.getContext().getCorrelationId(), Thread.currentThread().getId());
            restExchange = restTemplate.exchange(
                    "http://specialroutesservice/v1/route/abtesting/{serviceName}",
                    HttpMethod.GET, null, AbTestingRoute.class, serviceName);
        } catch (HttpClientErrorException ex) {
            // special route record 없으면 null 리턴
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return null;
            throw ex;
        }
        return restExchange.getBody();
    }

    // special route record 사용 여부 판단
    public boolean useSpecialRoute(AbTestingRoute testRoute) {
        Random random = new Random();

        // route record 활성 여부
        if (testRoute.getActive().equals("N")) return false;

        int value = random.nextInt((10 - 1) + 1) + 1;

        // route record 사용 여부 결정
        if (testRoute.getWeight() < value) return true;

        return false;
    }

    // special route 경로 생성
    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName) {
        int index = oldEndpoint.indexOf(serviceName);

        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        logger.debug("Target route: " + String.format("%s/%s", newEndpoint, strippedRoute));
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }

    // special route 호출
    private void forwardToSpecialRoute(String route) {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        // header 복사본 생성
        MultiValueMap<String, String> headers = this.helper.buildZuulRequestHeaders(request);
        // param 복사본 생성
        MultiValueMap<String, String> params = this.helper.buildZuulRequestQueryParams(request);

        // HTTP Method return
        String verb = getVerb(request);
        // RequestBody return
        InputStream requestEntity = getRequestBody(request);

        // RequestBody 없는 경우 처리?
        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        this.helper.addIgnoredHeaders();
        CloseableHttpClient httpClient = null;
        HttpResponse response = null;

        try {
            // http client 생성
            httpClient = HttpClients.createDefault();
            // http request 호출
            response = forward(httpClient, verb, route, request, headers, params, requestEntity);
            // http response 생성
            setResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {

            }
        }
    }

    // HTTP Method return
    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }

    // RequestBody return
    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok
        }
        return requestEntity;
    }

    // http request 호출
    private HttpResponse forward(HttpClient httpClient, String verb, String uri,
                                 HttpServletRequest request, MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {

        Map<String, Object> info = this.helper.debug(verb, uri, headers, params, requestEntity);

        URL host = new URL(uri);
        // HTTP HOST return
        HttpHost httpHost = getHttpHost(host);

        HttpRequest httpRequest;

        // entity 생성
        int contentLength = request.getContentLength();
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                request.getContentType() != null ? ContentType.create(request.getContentType()) : null);

        // http Method 에 따른 http request 설정(setEntity 순서가 바뀐거 아닌가?)
        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uri);
        }

        try {
            // header 설정(map to array)
            httpRequest.setHeaders(convertHeaders(headers));
            // http request 호출
            HttpResponse zuulResponse = forwardRequest(httpClient, httpHost, httpRequest);

            return zuulResponse;
        } finally {

        }
    }

    // HTTP HOST return
    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getProtocol());
        return httpHost;
    }

    // header map to array
    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    // http request 호출
    private HttpResponse forwardRequest(HttpClient httpClient, HttpHost httpHost, HttpRequest httpRequest)
            throws IOException {
        return httpClient.execute(httpHost, httpRequest);
    }

    // http response 생성
    private void setResponse(HttpResponse response) throws IOException {
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

    // header array to map
    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }
}
