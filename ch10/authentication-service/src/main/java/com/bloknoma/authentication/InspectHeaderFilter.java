package com.bloknoma.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class InspectHeaderFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(InspectHeaderFilter.class);

    private static final String AUTH_TOKEN = "Authorization";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        logger.debug("I am hitting the Auth server: " + httpServletRequest.getHeader(AUTH_TOKEN));

        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
