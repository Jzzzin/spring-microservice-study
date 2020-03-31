package com.bloknoma.zuulsvr.filters;

import com.bloknoma.zuulsvr.model.AbTestingRoute;
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
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    @Autowired
    private ProxyRequestHelper helper;

    // special routes service 확인 용
    @Autowired
    private ZuulProperties zuulProperties;

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

        // check special routes service running
        if (zuulProperties.getRoutes().get("specialroutesservice") == null) {
            logger.debug("special routes services is not running");
            return null;
        }

        // special routes service call
        AbTestingRoute abTestingRoute = getAbRoutingInfo(filterUtils.getServiceId());

        // special routes use decision
        if (abTestingRoute != null && useSpecialRoute(abTestingRoute)) {
/*
            // new route string build
            String route = buildRouteString(ctx.getRequest().getRequestURI(),
                    abTestingRoute.getEndpoint(), ctx.get("serviceId").toString());
            // new route call
            forwardToSpecialRoute(route);
*/
            // setRouteHost 해서 RibbonRoutingFilter skip 하고 SimpleHostingRouteFilter 처리한다
            try {
                URL host = new URL(abTestingRoute.getEndpoint());
                ctx.setRouteHost(host);
                logger.debug("RouteHost: {}", host.toString());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    // special routes service call
    private AbTestingRoute getAbRoutingInfo(String serviceName) {
        ResponseEntity<AbTestingRoute> restExchange = null;

        // correlation id header setting
        HttpHeaders headers = new HttpHeaders();
        headers.set(FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        try {
            logger.debug(">>> In SpecialRouterFilter.getAbRoutingInfo: {}", filterUtils.getCorrelationId());
            restExchange = restTemplate.exchange(
                    "http://specialroutesservice/v1/route/abtesting/{serviceName}",
                    HttpMethod.GET, entity, AbTestingRoute.class, serviceName);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return null;
            throw ex;
        }

        return restExchange.getBody();
    }

    // special routes use decision
    private boolean useSpecialRoute(AbTestingRoute testRoute) {
        Random random = new Random();

        if (testRoute.getActive().equals("N")) return false;

        int value = random.nextInt((10 - 1) + 1) + 1;

        if (testRoute.getWeight() < value) return true;

        return false;
    }

    // new route string build
    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName) {
        int index = oldEndpoint.indexOf(serviceName);

        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        logger.debug("Target route: " + String.format("%s/%s", newEndpoint, strippedRoute));
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }

    // new route call
    private void forwardToSpecialRoute(String route) {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        MultiValueMap<String, String> headers = this.helper.buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper.buildZuulRequestQueryParams(request);
        // http method
        String verb = getVerb(request);
        // body
        InputStream requestEntity = getRequestBody(request);

        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }
        this.helper.addIgnoredHeaders();

        CloseableHttpClient httpClient = null;
        HttpResponse response = null;

        try {
            httpClient = HttpClients.createDefault();
            // http call
            response = forward(httpClient, verb, route, request, headers, params, requestEntity);
            // set response
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

    // http method
    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }

    // body
    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok
        }
        return requestEntity;
    }

    // set http
    private HttpResponse forward(HttpClient httpClient, String verb, String uri,
                                 HttpServletRequest request, MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params, requestEntity);
        URL host = new URL(uri);
        // host
        HttpHost httpHost = getHttpHost(host);
        HttpRequest httpRequest;
        int contentLength = request.getContentLength();
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                request.getContentType() != null ? ContentType.create(request.getContentType()) : null);
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
            // header map to array
            httpRequest.setHeaders(convertHeaders(headers));
            // http request
            HttpResponse zuulResponse = forwardRequest(httpClient, httpHost, httpRequest);

            return zuulResponse;
        } finally {

        }
    }

    // host
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

    // http request
    private HttpResponse forwardRequest(HttpClient httpClient, HttpHost httpHost,
                                        HttpRequest httpRequest) throws IOException {
        return httpClient.execute(httpHost, httpRequest);
    }

    // set response
    private void setResponse(HttpResponse response) throws IOException {
        RequestContext.getCurrentContext().set("zuulResponse", response);
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
        System.out.println(RequestContext.getCurrentContext());
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
