package org.sixbacks.fastats.statistics.service;

import java.util.Map;

import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.springframework.data.domain.Page;

public interface ElasticSearchService {
	// String indexData(Map<String, Object> row);

	void saveData();

	void saveDataWithBulk();

	void saveDataWithBulkThroughMultiThreads();

	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size);
}
