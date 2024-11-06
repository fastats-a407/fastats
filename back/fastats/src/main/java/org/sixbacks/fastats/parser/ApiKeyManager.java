package org.sixbacks.fastats.parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiKeyManager {
	private final ConcurrentHashMap<String, AtomicInteger> apiKeyMap = new ConcurrentHashMap<>();
	private final ConcurrentLinkedQueue<String> apiKeyQueue = new ConcurrentLinkedQueue<>();

	private final AtomicInteger MAX_REQUESTS = new AtomicInteger(300);

	public ApiKeyManager(@Value("${OPENAPI.KEYS}") String apiKeys) {
		for (String apiKey : apiKeys.split("=")) {
			apiKeyMap.put(apiKey + "=", new AtomicInteger(0));
			apiKeyQueue.add(apiKey + "=");
		}
	}

	public String getApiKey() {
		String apiKey = apiKeyQueue.peek();
		if (apiKey == null) {
			log.error("모든 API 키 한도 소진");
			throw new RuntimeException("사용가능한 API 없음");
		}
		return apiKey;
	}

	public void useApiKey(String apiKey) {
		int oldValue = apiKeyMap.get(apiKey).incrementAndGet();
		if (oldValue > MAX_REQUESTS.get()) {
			invalidateApiKey(apiKey);
		}
	}

	public void invalidateApiKey(String apiKey) {
		log.warn("키 한도 소진 {}", apiKeyQueue.size());
		apiKeyMap.remove(apiKey);
	}
}
