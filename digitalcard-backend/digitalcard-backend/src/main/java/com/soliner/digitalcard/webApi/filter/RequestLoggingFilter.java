package com.soliner.digitalcard.webApi.filter;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Gelen HTTP isteklerini loglayan basit bir filtre (middleware).
 * Bu filtre, HTTP istek/yanıt akışıyla ilgili olduğu için webApi katmanına aittir.
 * Spring Boot'ta bu filtreyi etkinleştirmek için bir yapılandırma sınıfı gerekebilir.
 */
@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        logger.info("Gelen İstek: {} {}", method, requestURI);
        chain.doFilter(request, response);
        logger.info("İstek Tamamlandı: {} {}", method, requestURI);
    }

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) throws ServletException {
        logger.info("RequestLoggingFilter başlatıldı.");
    }

    @Override
    public void destroy() {
        logger.info("RequestLoggingFilter sonlandırıldı.");
    }
}
