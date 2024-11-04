package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticSearchRepository extends ElasticsearchRepository<StatDataDocument, Long> {

	@Query("{ \"multi_match\": { " +
		"  \"query\": \"?0\"," +
		"  \"fields\": [\"statSurveyName\", \"statOrgName\", \"statTableName\", \"statTableContent\"]," +
		"  \"type\": \"best_fields\"" +
		"} }")
	Page<StatDataDocument> searchByKeyword(String keyword, Pageable pageable);

}
