package org.sixbacks.fastats.search.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sixbacks.fastats.search.dto.SearchAutoCompleteResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SearchService {

	private final RestTemplate restTemplate;
	private final String NGRAM_URL;

	public SearchService(@Value("${spring.elasticsearch.uris}") String elasticsearchUrl,
						 @Value("${spring.elasticsearch.username}") String username,
						 @Value("${spring.elasticsearch.password}") String password) {
		this.restTemplate = new RestTemplate();
		this.NGRAM_URL = elasticsearchUrl + "/ngram_index/_search";

		// Basic Authentication 설정
		restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
	}

	public List<SearchAutoCompleteResponseDto> searchAutoComplete(String query) {

		String requestJson = String.format("{\n" +
				"  \"size\": 5,\n" +
				"  \"query\": {\n" +
				"    \"bool\": {\n" +
				"      \"should\": [\n" +
				"        { \"match_phrase\": { \"statTableName\": \"%s\" }},\n" +
				"        { \"match_phrase\": { \"statSurveyName\": \"%s\" }},\n" +
				"        { \"match_phrase\": { \"statOrgName\": \"%s\" }}\n" +
				"      ],\n" +
				"      \"minimum_should_match\": 1\n" +
				"    }\n" +
				"  },\n" +
				"  \"aggs\": {\n" +  // 필드별 집계를 추가
				"    \"statTableName_suggestions\": { \"terms\": { \"field\": \"statTableName.keyword\", \"size\": 5 }},\n" +
				"    \"statSurveyName_suggestions\": { \"terms\": { \"field\": \"statSurveyName.keyword\", \"size\": 5 }},\n" +
				"    \"statOrgName_suggestions\": { \"terms\": { \"field\": \"statOrgName.keyword\", \"size\": 5 }}\n" +
				"  }\n" +
				"}", query, query, query);
		// HTTP 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// HTTP 요청 엔티티 생성
		HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

		// Elasticsearch에 POST 요청
		String response = restTemplate.postForObject(NGRAM_URL, requestEntity, String.class);

		// 결과 파싱 및 변환
		return parseResponse(response);
	}

private List<SearchAutoCompleteResponseDto> parseResponse(String response) {
	List<SearchAutoCompleteResponseDto> results = new ArrayList<>();
	JSONObject jsonResponse = new JSONObject(response);

	// aggregations가 있는지 확인
	if (jsonResponse.has("aggregations")) {
		JSONObject aggregations = jsonResponse.getJSONObject("aggregations");

		// 각 필드에 대한 결과를 가져와 DTO로 변환하여 리스트에 추가
		if (aggregations.has("statTableName_suggestions")) {
			JSONArray buckets = aggregations.getJSONObject("statTableName_suggestions").getJSONArray("buckets");
			for (int i = 0; i < buckets.length(); i++) {
				String suggestion = buckets.getJSONObject(i).getString("key");
				results.add(SearchAutoCompleteResponseDto.builder().keyword(suggestion).build());
			}
		}
		if (aggregations.has("statSurveyName_suggestions")) {
			JSONArray buckets = aggregations.getJSONObject("statSurveyName_suggestions").getJSONArray("buckets");
			for (int i = 0; i < buckets.length(); i++) {
				String suggestion = buckets.getJSONObject(i).getString("key");
				results.add(SearchAutoCompleteResponseDto.builder().keyword(suggestion).build());
			}
		}
		if (aggregations.has("statOrgName_suggestions")) {
			JSONArray buckets = aggregations.getJSONObject("statOrgName_suggestions").getJSONArray("buckets");
			for (int i = 0; i < buckets.length(); i++) {
				String suggestion = buckets.getJSONObject(i).getString("key");
				results.add(SearchAutoCompleteResponseDto.builder().keyword(suggestion).build());
			}
		}
	}

	// 상위 5개 항목만 반환하도록 자르기
	return results.size() > 5 ? results.subList(0, 5) : results;
}

}