package org.sixbacks.fastats.search.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sixbacks.fastats.search.dto.SearchAutoCompleteResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SearchService {

	private final RestTemplate restTemplate = new RestTemplate();
	private final String ELASTICSEARCH_URL = "http://localhost:9200/ngram_index/_search";

	public List<SearchAutoCompleteResponseDto> searchAutoComplete(String query) {
		// JSON 요청 본문 생성
		String requestJson = String.format("{\n" +
				"  \"size\": 0,\n" +
				"  \"query\": {\n" +
				"    \"multi_match\": {\n" +
				"      \"query\": \"%s\",\n" +
				"      \"fields\": [\"statTableName\", \"statSurveyName\", \"statOrgName\"],\n" +
				"      \"type\": \"phrase_prefix\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"aggs\": {\n" +
				"    \"statTableName_suggestions\": {\n" +
				"      \"terms\": { \"field\": \"statTableName.keyword\", \"size\": 10 }\n" +
				"    },\n" +
				"    \"statSurveyName_suggestions\": {\n" +
				"      \"terms\": { \"field\": \"statSurveyName.keyword\", \"size\": 10 }\n" +
				"    },\n" +
				"    \"statOrgName_suggestions\": {\n" +
				"      \"terms\": { \"field\": \"statOrgName.keyword\", \"size\": 10 }\n" +
				"    }\n" +
				"  }\n" +
				"}", query);

		// HTTP 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// HTTP 요청 엔티티 생성
		HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

		// Elasticsearch에 POST 요청
		String response = restTemplate.postForObject(ELASTICSEARCH_URL, requestEntity, String.class);

		// 결과 파싱 및 변환
		return parseResponse(response);
	}


	private List<SearchAutoCompleteResponseDto> parseResponse(String response) {
		List<SearchAutoCompleteResponseDto> results = new ArrayList<>();
		JSONObject jsonResponse = new JSONObject(response);
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
		return results;
	}
}