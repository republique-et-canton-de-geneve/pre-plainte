package ch.ge.police.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

  private static final String TRACE_ID = "traceId";
  private static final String HEADER_TRACE_ID = "X-Trace-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    String traceId = Optional.ofNullable(request.getHeader(HEADER_TRACE_ID))
      .filter(value -> !value.isBlank())
      .orElse(UUID.randomUUID().toString());

    MDC.put(TRACE_ID, traceId);
    response.setHeader(HEADER_TRACE_ID, traceId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(TRACE_ID);
    }
  }
}
