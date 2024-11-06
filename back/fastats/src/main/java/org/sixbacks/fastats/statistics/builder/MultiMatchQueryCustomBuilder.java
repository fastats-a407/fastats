package org.sixbacks.fastats.statistics.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.util.ObjectBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MultiMatchQueryCustomBuilder {

	private String keyword;
	private final Map<String, Float> fieldsWithBoostsMap = new HashMap<>();
	private TextQueryType queryType;
	private String analyzer;
	private Pageable pageable;

	public MultiMatchQueryCustomBuilder withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public MultiMatchQueryCustomBuilder queryType(TextQueryType queryType) {
		this.queryType = queryType;
		return this;
	}

	public MultiMatchQueryCustomBuilder withAnalyzer(String analyzer) {
		this.analyzer = analyzer;
		return this;
	}

	public MultiMatchQueryCustomBuilder addPageable(Pageable pageable) {
		this.pageable = pageable;
		return this;
	}

	public MultiMatchQueryCustomBuilder addFieldWithBoost(String field) {
		this.fieldsWithBoostsMap.put(field, 1.0f);
		return this;
	}

	// 필드와 가중치를 설정하는 메서드 (여러 번 호출 가능)
	public MultiMatchQueryCustomBuilder addFieldWithBoost(String field, Float boost) {
		this.fieldsWithBoostsMap.put(field, boost);
		return this;
	}

	// ObjectBuilder<MultiMatchQuery>를 반환하는 Function 객체 생성
	private final Function<MultiMatchQuery.Builder, ObjectBuilder<MultiMatchQuery>> multiMatchFunction = m -> m
		.query(keyword)
		.fields(generateFieldListFrom(fieldsWithBoostsMap))
		.type(queryType)
		.analyzer(analyzer);

	// multiMatchFunction을 그대로 전달하여 Query 빌드
	public Query build() {
		return NativeQuery.builder()
			.withQuery(q -> q
				.multiMatch(multiMatchFunction))
			.withPageable(pageable)
			.build();
	}

	private List<String> generateFieldListFrom(Map<String, Float> fieldsWithBoostsMap) {
		return fieldsWithBoostsMap.entrySet().stream()
			.map(entry -> entry.getKey() + "^" + entry.getValue())
			.collect(Collectors.toList());
	}
}

