package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticSearchRepository extends ElasticsearchRepository<StatDataDocument, Long> {

}
