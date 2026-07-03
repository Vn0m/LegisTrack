package com.legistrack.app.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration NYSENATE_READ_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration HUGGINGFACE_READ_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    public RestClient nySenateRestClient(@Value("${app.nysenate.baseUrl}") String baseUrl) {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory(NYSENATE_READ_TIMEOUT))
            .build();
    }

    @Bean
    public RestClient huggingFaceRestClient(@Value("${app.huggingface.apiKey}") String apiKey) {
        return RestClient.builder()
            .baseUrl("https://router.huggingface.co")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .requestFactory(requestFactory(HUGGINGFACE_READ_TIMEOUT))
            .build();
    }

    private HttpComponentsClientHttpRequestFactory requestFactory(Duration readTimeout) {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT))
                .setSocketTimeout(Timeout.of(readTimeout))
                .build())
            .setMaxConnTotal(50)
            .setMaxConnPerRoute(20)
            .build();
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
