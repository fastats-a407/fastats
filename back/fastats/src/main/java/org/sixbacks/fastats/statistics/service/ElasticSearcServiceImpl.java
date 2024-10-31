package org.sixbacks.fastats.statistics.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearcServiceImpl implements ElasticSearchService {

	private final RestHighLevelClient client;
	private final JdbcTemplate jdbcTemplate;

	public ElasticSearcServiceImpl(RestHighLevelClient client, JdbcTemplate jdbcTemplate) {
		this.client = client;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String saveData() {
		String sql = "SELECT * FROM board";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

		List<String> results = rows.stream()
			.map(this::indexData)
			.collect(Collectors.toList());

		if (results.contains("Failed")) {
			return "Data migration failed for some entries.";
		}
		return "Data migration completed successfully.";
	}

	@Override
	public String indexData(Map<String, Object> row) {
		try {
			IndexRequest request = new IndexRequest("board_index")
				.id(row.get("id").toString())
				.source(row, XContentType.JSON);

			client.index(request, RequestOptions.DEFAULT);
			return "Success";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed";
		}
	}
}
