package org.sixbacks.fastats.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdbcConfig {
	@Bean
	public RestHighLevelClient restHighLevelClient() {
		return new RestHighLevelClient(
			// TODO : localhost에서 변경 필요
			RestClient.builder(new HttpHost("localhost", 9200, "http")));
	}
}
