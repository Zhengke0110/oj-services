package fun.timu.oj.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    /**
     * 配置跨域请求过滤器
     * 该方法用于创建并返回一个WebFilter，用于处理跨域请求（CORS）相关的问题
     * 主要通过在HTTP响应中添加相关的跨域头来实现跨域请求的支持
     *
     * @return WebFilter 一个处理跨域请求的过滤器实例
     */
    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            // 获取当前请求
            ServerHttpRequest request = ctx.getRequest();
            // 检查是否为跨域请求
            if (CorsUtils.isCorsRequest(request)) {
                // 获取请求头
                HttpHeaders requestHeaders = request.getHeaders();
                // 获取当前响应
                ServerHttpResponse response = ctx.getResponse();
                // 获取请求头中的跨域请求方法
                HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
                // 获取响应头
                HttpHeaders headers = response.getHeaders();
                // 设置允许的跨域请求源
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
                // 设置允许的跨域请求头
                headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders.getAccessControlRequestHeaders());
                // 如果请求方法不为空，则设置允许的跨域请求方法
                if (requestMethod != null) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
                }
                // 设置允许跨域请求带上用户凭证
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                // 设置允许跨域请求可以暴露的头
                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                // 如果请求方法为OPTIONS，则直接返回OK状态码，通常用于预检请求
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            // 继续执行其他过滤器
            return chain.filter(ctx);
        };
    }

}