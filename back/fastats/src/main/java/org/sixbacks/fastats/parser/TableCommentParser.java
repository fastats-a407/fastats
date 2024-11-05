package org.sixbacks.fastats.parser;

import java.util.concurrent.CompletableFuture;

import org.sixbacks.fastats.statistics.dto.preprocessing.TableCommentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TableCommentParser {

	private final String key;

	private final String url;

	private final ObjectMapper mapper;

	private final RestClient restClient;

	public TableCommentParser(@Value("${OPENAPI.KEY}") String key, @Value("${OPENAPI.URL}") String url,
		ObjectMapper mapper
	) {
		this.key = key;
		this.url = url;
		this.mapper = mapper;
		this.restClient = RestClient.create();
	}

	@Async
	public CompletableFuture<TableCommentDto> getCommentAndContentsByTableId(String tableId) {
		return CompletableFuture.supplyAsync(() -> {
			String response = restClient.get().uri(url, key, tableId).retrieve().body(String.class);
			if (response == null) {
				log.warn("응답 잘못됨. {}", tableId);
				return new TableCommentDto(null, null);
			}
			try {
				response = response.replaceAll("(\\b[a-zA-Z0-9_]+)(\\s*):(?!//)", "\"$1\":");
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
				}
			} catch (Exception e) {
				log.error("테이블 ({}) 주석 및 컨텐츠 파싱 중 에러 발생 : {}", tableId, e.getMessage());
				log.error("response : {}", response);
			}
			return new TableCommentDto(null, null);
		});
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
