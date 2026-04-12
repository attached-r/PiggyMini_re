package gateway.filter;

import common.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证全局过滤器
 * 解析 Token 并将 userId 放入请求头传递给下游服务
 *
 * @author: rj
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JWTUtil jwtUtil;

    // 白名单路径（不需要 Token 校验）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. 白名单直接放行
        if (isWhiteList(path)) {
            log.debug("白名单路径放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("请求缺少有效 Token: {}", path);
            return unauthorized(exchange, "请先登录");
        }

        token = token.substring(7); // 去掉 "Bearer " 前缀

        // 3. 解析 Token 获取 userId
        try {
            String userId = jwtUtil.getSubject(token);
            if (userId == null) {
                return unauthorized(exchange, "Token 无效");
            }

            // 4. 将 userId 放入请求头传递给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            log.debug("Token 解析成功, userId: {}, path: {}", userId, path);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", path);
            return unauthorized(exchange, "登录已过期,请重新登录");
        } catch (Exception e) {
            log.error("Token 解析失败: {}, error: {}", path, e.getMessage());
            return unauthorized(exchange, "Token 无效");
        }
    }

    /**
     * 判断是否为白名单路径
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis());
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -100; // 优先级高，确保在其他过滤器之前执行
    }
}
