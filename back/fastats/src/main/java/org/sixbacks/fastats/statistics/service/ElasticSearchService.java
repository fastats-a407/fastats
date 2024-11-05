package org.sixbacks.fastats.statistics.service;

import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.springframework.data.domain.Page;

public interface ElasticSearchService {
	// String indexData(Map<String, Object> row);

	void saveData();

	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size);
}
