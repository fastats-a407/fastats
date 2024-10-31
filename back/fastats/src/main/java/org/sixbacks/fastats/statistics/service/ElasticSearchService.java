package org.sixbacks.fastats.statistics.service;

import java.util.Map;

public interface ElasticSearchService {
	String indexData(Map<String, Object> row);

	String saveData();
}
