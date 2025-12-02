package com.sewage.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器
 * 验证请求中的JWT令牌，并将用户信息传递给下游服务
 */
@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 白名单路径（不需要认证）
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/health",
            "/api/auth/info"
    );

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("请求路径: {}", path);

            // 检查是否在白名单中
            if (isWhiteListed(path)) {
                log.debug("白名单路径，直接放行: {}", path);
                return chain.filter(exchange);
            }

            // 获取Token
            String token = resolveToken(request);

            if (!StringUtils.hasText(token)) {
                log.warn("请求路径 {} 缺少认证令牌", path);
                return onError(exchange, "未提供认证令牌", HttpStatus.UNAUTHORIZED);
            }

            try {
                // 验证Token
                Claims claims = validateToken(token);

                if (claims == null) {
                    log.warn("请求路径 {} 的令牌无效", path);
                    return onError(exchange, "无效的认证令牌", HttpStatus.UNAUTHORIZED);
                }

                // 将用户信息添加到请求头中，传递给下游服务
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", claims.get("userId", Long.class).toString())
                        .header("X-User-Name", claims.get("username", String.class))
                        .header("X-User-Role", claims.get("role", String.class))
                        .build();

                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(mutatedRequest)
                        .build();

                log.debug("用户 {} 通过认证，访问路径: {}", claims.get("username"), path);
                return chain.filter(mutatedExchange);

            } catch (Exception e) {
                log.error("令牌验证失败: {}", e.getMessage());
                return onError(exchange, "令牌验证失败: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                return path.startsWith(pattern.substring(0, pattern.length() - 3));
            }
            return pattern.equals(path);
        });
    }

    /**
     * 从请求中解析Token
     */
    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    private SecretKey getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    /**
     * 验证Token
     */
    private Claims validateToken(String token) {
        try {
            // 使用已定义的getSignKey()方法
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())  // 改为使用getSignKey()
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 返回错误响应
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> result = new HashMap<>();
        result.put("code", status.value());
        result.put("message", message);
        result.put("success", false);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("响应序列化失败: {}", e.getMessage());
            return response.setComplete();
        }
    }

    /**
     * 配置类
     */
    public static class Config {
        // 可以添加自定义配置
    }
}