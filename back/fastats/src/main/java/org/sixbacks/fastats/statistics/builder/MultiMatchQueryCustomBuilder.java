package org.sixbacks.fastats.statistics.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.util.ObjectBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MultiMatchQueryCustomBuilder {

	private String keyword;
	private final Map<String, Float> fieldsWithBoostsMap = new HashMap<>();
	private final List<String> aggregationFields = new ArrayList<>();
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

	public MultiMatchQueryCustomBuilder addAggregationField(String field) {
		this.aggregationFields.add(field);
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
		NativeQueryBuilder queryBuilder = NativeQuery.builder()
			.withQuery(q -> q
				.multiMatch(multiMatchFunction));

		if (pageable != null) {
			queryBuilder.withPageable(pageable);
		}

		// Aggregation을 위한 필드 추가
		if (!aggregationFields.isEmpty()) {
			aggregationFields.forEach(aggr -> queryBuilder.withAggregation(aggr,
				new Aggregation.Builder()
					.terms(t -> t.field(aggr + ".keyword"))
					.build()
			));
		}

		return queryBuilder.build();
	}

	public Query build(NativeQueryBuilder queryBuilder) {
		queryBuilder
			.withQuery(q -> q
				.multiMatch(multiMatchFunction));

		if (pageable != null) {
			queryBuilder.withPageable(pageable);
		}

		// Aggregation을 위한 필드 추가
		if (!aggregationFields.isEmpty()) {
			NativeQueryBuilder finalQueryBuilder = queryBuilder;
			aggregationFields.forEach(aggr -> finalQueryBuilder.withAggregation(aggr,
				new Aggregation.Builder()
					.terms(t -> t.field(aggr + ".keyword"))
					.build()
			));
		}

		return queryBuilder.build();
	}

	private List<String> generateFieldListFrom(Map<String, Float> fieldsWithBoostsMap) {
		return fieldsWithBoostsMap.entrySet().stream()
			.map(entry -> entry.getKey() + "^" + entry.getValue())
			.collect(Collectors.toList());
	}
}

