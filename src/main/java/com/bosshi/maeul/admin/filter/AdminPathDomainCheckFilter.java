package com.bosshi.maeul.admin.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminPathDomainCheckFilter extends OncePerRequestFilter {

    @Value("${kraftadmin.allowed-host}")
    private String allowedAdminHost;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // /admin 경로 또는 /admin/ 하위 경로로 들어오는 요청 검사
        if (requestURI.equals("/admin") || requestURI.startsWith("/admin/")) {
            String host = request.getHeader("Host");

            // 포트 번호 제거 (예: admin.maeul.duckdns.org:8080 -> admin.maeul.duckdns.org)
            if (host != null && host.contains(":")) {
                host = host.split(":")[0];
            }

            // Host가 admin.maeul.duckdns.org 가 아니면 403 차단
            if (host == null || !allowedAdminHost.equalsIgnoreCase(host)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Access Denied");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}