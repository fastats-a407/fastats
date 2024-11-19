package org.sixbacks.fastats.statistics.service;

import java.util.List;

import org.sixbacks.fastats.statistics.dto.request.SearchCriteria;
import org.sixbacks.fastats.statistics.dto.response.CategoryListResponse;
import org.sixbacks.fastats.statistics.dto.response.SearchByKeywordDto;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.Query;

public interface ElasticSearchService {
	// String indexData(Map<String, Object> row);

	void saveData();

	void saveDataWithBulk();

	void saveDataWithBulkThroughMultiThreads();

	void saveDataNgramWithBulkThroughMultiThreads();

	/**
	 * @deprecated {@link #searchByKeyword(SearchCriteria)} 를 이용.
	 */
	@Deprecated
	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size);

	/**A
	 * @deprecated {@link #searchByKeyword(SearchCriteria, Query)} 를 이용.
	 */
	@Deprecated
	Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size, Query query);

	SearchByKeywordDto searchByKeyword(SearchCriteria searchCriteria);

	SearchByKeywordDto searchByKeyword(SearchCriteria searchCriteria, Query query);

	CategoryListResponse getCategoriesByKeyword(String keyword);

	CategoryListResponse getCategoriesByKeyword(String keyword, List<String> aggrList, Query query);

	List<String> getSuggestions(String userInput);
}
