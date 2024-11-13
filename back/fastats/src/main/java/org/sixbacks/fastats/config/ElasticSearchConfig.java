package org.sixbacks.fastats.config;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
		// SSLContext 설정
		SSLContext sslContext = SSLContextBuilder.create()
			.loadTrustMaterial((chain, authType) -> true)  // 모든 인증서 신뢰
			.build();

		// RequestConfig 설정 (연결 타임아웃과 소켓 타임아웃 설정)
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(15000)  // 연결 타임아웃 설정 (15초)
			.setSocketTimeout(15000)   // 소켓 타임아웃 설정 (15초)
			.build();

		// HttpClient 설정 (기본 인증 추가)
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(
			AuthScope.ANY,
			new UsernamePasswordCredentials(elasticsearchUser, elasticsearchPass)
		);

		CloseableHttpClient httpClient = HttpClients.custom()
			.setSSLContext(sslContext)
			.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // CN 검증 비활성화
			.setDefaultRequestConfig(requestConfig) // 타임아웃 설정 추가
			.setDefaultCredentialsProvider(credentialsProvider) // 기본 인증 추가
			.build();

		// RestClientBuilder 설정
		RestClientBuilder builder = RestClient.builder(new HttpHost("elasticsearch-node1", 9200, "https"))
			.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
				.setSSLContext(sslContext)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setDefaultRequestConfig(requestConfig) // RestClient에 타임아웃 설정 적용
				.setDefaultCredentialsProvider(credentialsProvider) // 기본 인증 추가
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
