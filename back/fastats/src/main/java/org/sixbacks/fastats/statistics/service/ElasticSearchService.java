package org.sixbacks.fastats.statistics.service;

import java.util.List;

import org.sixbacks.fastats.statistics.dto.request.SearchCriteria;
import org.sixbacks.fastats.statistics.dto.response.CategoryListResponse;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.Query;

public interface ElasticSearchService {
	// String indexData(Map<String, Object> row);

	void saveData();

	void saveDataWithBulk();

	void saveDataWithBulkThroughMultiThreads();

	/**
	 * @deprecated {@link #searchByKeyword(SearchCriteria)} 를 이용.
	 */
	@Deprecated
	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size);

	/**
	 * @deprecated {@link #searchByKeyword(SearchCriteria, Query)} 를 이용.
	 */
	@Deprecated
	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size, Query query);

	Page<StatTableListResponse> searchByKeyword(SearchCriteria searchCriteria);

	Page<StatTableListResponse> searchByKeyword(SearchCriteria searchCriteria, Query query);

	CategoryListResponse getCategoriesByKeyword(String keyword);

	CategoryListResponse getCategoriesByKeyword(String keyword, List<String> aggrList, Query query);

	List<String> getSuggestions(String userInput);
}
