package org.sixbacks.fastats.config;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ElasticSearchConfig {

	@Value("${spring.elasticsearch.username}")
	private String elasticsearchUser;

	@Value("${spring.elasticsearch.password}")
	private String elasticsearchPass;

	@Bean
	public RestHighLevelClient restHighLevelClient() throws Exception {
		// SSLContext 설정: 모든 인증서 신뢰
		SSLContext sslContext = SSLContextBuilder.create()
			.loadTrustMaterial((chain, authType) -> true)
			.build();

		IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
			.setIoThreadCount(Runtime.getRuntime().availableProcessors())
			.setConnectTimeout(15000)
			.setSoTimeout(15000)
			.build();

		// ConnectingIOReactor 생성
		DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

		// RequestConfig 설정
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(15000)  // 연결 타임아웃 설정 (15초)
			.setSocketTimeout(15000)   // 소켓 타임아웃 설정 (15초)
			.setConnectionRequestTimeout(10000) // 연결 요청 대기시간 추가 (5초)
			.build();

		// CredentialsProvider 설정: 기본 인증
		BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(
			AuthScope.ANY,
			new UsernamePasswordCredentials(elasticsearchUser, elasticsearchPass)
		);

		// 커넥션 매니저 설정: 커넥션 풀 크기
		PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
		connectionManager.setMaxTotal(600);           // 전체 최대 커넥션 수
		connectionManager.setDefaultMaxPerRoute(200);  // 호스트 당 최대 커넥션 수

		// RestClientBuilder 설정
		RestClientBuilder builder = RestClient.builder(new HttpHost("elasticsearch-node1", 9200, "https"))
			.setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder
				.setSSLContext(sslContext)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCredentialsProvider(credentialsProvider)
				.setConnectionManager(connectionManager)
				.setMaxConnTotal(600)          // 전체 커넥션 수
				.setMaxConnPerRoute(200)       // 호스트 당 커넥션 수
			)
			.setRequestConfigCallback(requestConfigBuilder ->
				requestConfigBuilder
					.setConnectTimeout(15000) // 연결 타임아웃 (15초)
					.setSocketTimeout(15000)  // 소켓 타임아웃 (15초)
			);

		return new RestHighLevelClient(builder);
	}

	// @Bean
	// public RestHighLevelClient restHighLevelClient() {
	// 	return new RestHighLevelClient(
	// 		// TODO : localhost에서 변경 필요
	// 		RestClient.builder(new HttpHost("localhost", 9200, "http")));
	// }

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}
