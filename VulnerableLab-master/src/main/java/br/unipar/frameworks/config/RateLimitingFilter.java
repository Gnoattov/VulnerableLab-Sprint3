package br.unipar.frameworks.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, UserRequestInfo> requests = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_SECONDS = 60;

    private static class UserRequestInfo {
        int count;
        Instant windowStart;

        UserRequestInfo() {
            this.count = 1;
            this.windowStart = Instant.now();
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIP = getClientIP(request);
        UserRequestInfo info = requests.get(clientIP);

        if (info == null) {
            requests.put(clientIP, new UserRequestInfo());
            filterChain.doFilter(request, response);
            return;
        }

        Instant now = Instant.now();
        if (now.isAfter(info.windowStart.plusSeconds(TIME_WINDOW_SECONDS))) {
            info.count = 1;
            info.windowStart = now;
            filterChain.doFilter(request, response);
            return;
        }

        if (info.count < MAX_REQUESTS) {
            info.count++;
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Muitas requisições. Aguarde um momento.\"}");
        }
    }
}