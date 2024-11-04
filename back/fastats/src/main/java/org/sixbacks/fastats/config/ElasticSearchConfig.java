package org.sixbacks.fastats.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ElasticSearchConfig {
	@Bean
	public RestHighLevelClient restHighLevelClient() {
		return new RestHighLevelClient(
			// TODO : localhost에서 변경 필요
			RestClient.builder(new HttpHost("localhost", 9200, "http")));
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
