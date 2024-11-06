package org.sixbacks.fastats.statistics.service;

import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.Query;

public interface ElasticSearchService {
	// String indexData(Map<String, Object> row);

	void saveData();

	void saveDataWithBulk();

	void saveDataWithBulkThroughMultiThreads();

	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size);

	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size, Query query);
}
