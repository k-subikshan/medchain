package meciblock.main.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.builder()
                .capacity(120)
                .refillGreedy(120, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(
                ip,
                key -> createBucket()
        );

        if (bucket.tryConsume(1)) {

            filterChain.doFilter(request, response);

        } else {

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            response.getWriter().write(
                    "Too many requests. Try again later."
            );
        }
    }
}
