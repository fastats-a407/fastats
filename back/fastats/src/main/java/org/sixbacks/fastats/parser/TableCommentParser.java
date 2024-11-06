package org.sixbacks.fastats.parser;

import java.util.concurrent.CompletableFuture;

import org.sixbacks.fastats.statistics.dto.preprocessing.TableCommentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TableCommentParser {

	private final ApiKeyManager apiKeyManager;

	private final String url;

	private final ObjectMapper mapper;

	private final RestClient restClient;

	public TableCommentParser(ApiKeyManager apiKeyManager, @Value("${OPENAPI.URL}") String url,
		ObjectMapper mapper
	) {
		this.apiKeyManager = apiKeyManager;
		this.url = url;
		this.mapper = mapper;
		this.restClient = RestClient.create();
	}

	@Async
	public CompletableFuture<TableCommentDto> getCommentAndContentsByTableId(String tableId) {
		return CompletableFuture.supplyAsync(() -> callApi(tableId));
	}

	public TableCommentDto callApi(String tableId) {
		String key = apiKeyManager.getApiKey();

		try {
			String response = restClient.get()
				.uri(url, key, tableId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(String.class);
			apiKeyManager.useApiKey(key);

			if (response == null) {
				log.warn("응답 잘못됨. {}", tableId);
				return new TableCommentDto(null, null);
			}
			response = response.replaceAll("(\\b[a-zA-Z0-9_]+)(\\s*):(?!//)\\s*\"?([^\"]*)\"?", "\"$1\": \"$3\"");
			JsonNode root = mapper.readTree(response);
			if (root.isArray()) {
				for (JsonNode item : root) {
					String comment = item.path("ITEM03").asText();
					String content = item.path("CONTENTS").asText();
					return new TableCommentDto(comment, content);
				}
			} else {
				log.warn("테이블({}) 주석 요청 에러. err : {} , errMsg: {}", tableId, root.path("err"),
					root.path("errMsg"));
				if (root.path("err").asText().equals("40")) {
					apiKeyManager.invalidateApiKey(key);
					return callApi(tableId);
				} else {
					return new TableCommentDto(null, null);
				}
			}
		} catch (Exception e) {
			log.error("테이블 ({}) 주석 및 컨텐츠 파싱 중 에러 발생 : {}", tableId, e.getMessage());
			log.debug("에러 원인 : ", e.getCause());
		}
		return new TableCommentDto(null, null);
	}

	@Async
	public CompletableFuture asyncDummy(int num) {
		return CompletableFuture.supplyAsync(() -> {
				log.info("Thread {} is started.", num);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return num;
			}

		);

	}
}
