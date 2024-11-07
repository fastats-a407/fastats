package org.sixbacks.fastats.statistics.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:.env")
public class StatSurveyJdbcRepositoryIntegrationTest {

	@Autowired
	private StatSurveyJdbcRepository statSurveyJdbcRepository;

	@Test
	@DisplayName("LIKE 쿼리를 통한 키워드 검색 시 제한된 결과 개수와 실행 시간 검증")
	public void testFindStatTableListResponsesByKeywordThroughLikePerformance() {
		// Given: keyword, limit, offset 제공
		String keyword = "인구";
		int limit = 10;
		int offset = 2;

		// When: DB에 직접 LIKE 문을 통한 검색 요청을 보낼 때
		long startTime = System.currentTimeMillis();

		List<StatTableListResponse> results = statSurveyJdbcRepository.findStatTableListResponsesByKeywordThroughLike(
			keyword, limit, offset);

		long endTime = System.currentTimeMillis();

		// 성능 측정 결과 출력
		long duration = endTime - startTime;
		System.out.println("Query Execution Time: " + duration + " ms");

		// Then: 존재하는 테이블에 대해서 검색 결과가 있어야 하며, 개수가 limit 내여야 함.
		assertThat(results).isNotNull();
		assertThat(results.size()).isLessThanOrEqualTo(limit);
	}

	@Test
	@DisplayName("FULL TEXT INDEX를 통한 키워드 검색 시 제한된 결과 개수와 실행 시간 검증")
	public void testFindStatTableListResponsesByKeywordWithFullTextPerformance() {
		// Given: keyword, limit, offset 제공
		String keyword = "일반 가구";
		int limit = 10;
		int offset = 0;

		// When: DB에 직접 LIKE 문을 통한 검색 요청을 보낼 때
		long startTime = System.currentTimeMillis();

		List<StatTableListResponse> results = statSurveyJdbcRepository.findStatTableListResponsesByKeywordWithFullText(
			keyword, limit, offset);

		long endTime = System.currentTimeMillis();

		long duration = endTime - startTime;
		System.out.println("Query Execution Time: " + duration + " ms");

		// Then: 존재하는 테이블에 대해서 검색 결과가 있어야 하며, 개수가 limit 내여야 함.
		assertThat(results).isNotNull();
		assertThat(results.size()).isLessThanOrEqualTo(limit);
	}

}
