package fun.timu.oj.common.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {


    /**
     * 创建并配置RestTemplate实例
     * <p>
     * 此方法通过注入的ClientHttpRequestFactory实例来创建RestTemplate实例
     * 主要作用是使得应用程序可以发起HTTP请求，并通过配置请求工厂来自定义请求的行为
     *
     * @param requestFactory HTTP请求工厂实例，用于创建定制的HTTP请求
     * @return 返回配置好的RestTemplate实例，用于执行HTTP请求
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory requestFactory) {
        return new RestTemplate(requestFactory);
    }


    /**
     * 创建并配置HTTP请求工厂
     * <p>
     * 此方法定义了一个Spring的@Bean方法，用于创建一个ClientHttpRequestFactory实例
     * 它选择使用HttpComponentsClientHttpRequestFactory作为实现，这是因为Apache HttpClient提供了丰富的功能和良好的性能
     * 通过这个方法，我们可以确保应用程序中所有出站HTTP请求都使用相同的配置和设置
     *
     * @return ClientHttpRequestFactory 实例，用于创建HTTP请求
     */
    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    /**
     * 创建并配置HttpClient bean
     * <p>
     * 该方法首先设置HTTP和HTTPS连接的Socket工厂注册表，然后配置连接池管理器，
     * 包括最大连接数和每个路由的最大连接数此外，还配置了请求的超时参数，
     * 包括数据返回、服务器连接和获取连接的超时时间最后，使用这些配置创建并返回一个CloseableHttpClient实例
     *
     * @return CloseableHttpClient 配置好的HttpClient实例
     */
    @Bean
    public HttpClient httpClient() {


        // 创建一个注册表，用于定义如何创建不同类型的连接
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory()).build();

        // 使用上述注册表创建一个连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);

        //设置连接池最大是500个连接
        connectionManager.setMaxTotal(500);
        //MaxPerRoute是对maxtotal的细分，每个主机的并发最大是300，route是指域名
        connectionManager.setDefaultMaxPerRoute(300);
        /**
         * 只请求 xdclass.net,最大并发300
         *
         * 请求 xdclass.net,最大并发300
         * 请求 open1024.com,最大并发200
         *
         * //MaxtTotal=400 DefaultMaxPerRoute=200
         * //只连接到http://xdclass.net时，到这个主机的并发最多只有200；而不是400；
         * //而连接到http://xdclass.net 和 http://open1024.com时，到每个主机的并发最多只有200；
         * // 即加起来是400（但不能超过400）；所以起作用的设置是DefaultMaxPerRoute。
         *
         */

        // 配置请求的超时参数
        RequestConfig requestConfig = RequestConfig.custom()
                //返回数据的超时时间
                .setSocketTimeout(20000)
                //连接上服务器的超时时间
                .setConnectTimeout(10000)
                //从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1000).build();


        // 使用配置好的连接池管理器和请求配置创建HttpClient实例
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager).build();

        // 返回配置好的HttpClient实例
        return closeableHttpClient;
    }


}
