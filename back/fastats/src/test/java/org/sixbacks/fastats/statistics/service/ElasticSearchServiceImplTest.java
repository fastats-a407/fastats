package org.sixbacks.fastats.statistics.service;

import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:.env")
public class ElasticSearchServiceImplTest {

	@Autowired
	private ElasticSearchService elasticSearchService;

	@Autowired
	private StatSurveyJdbcRepository statSurveyJdbcRepository;

	@Autowired
	private ElasticsearchRepository<StatDataDocument, Long> elasticsearchRepository;

	@Test
	public void getCountData() {
		// Elasticsearch에서 데이터가 저장되었는지 확인
		Iterable<StatDataDocument> responses = elasticsearchRepository.findAll();

		// 저장된 데이터 출력
		responses.forEach(System.out::println);

		// 데이터가 들어갔는지 개수를 확인
		long count = StreamSupport.stream(responses.spliterator(), false).count();
		System.out.println("Stored documents count: " + count);
	}

	@Test
	public void testSaveDataWithBulkPerformance() throws InterruptedException {
		// 시작 시간 기록
		long startTime = System.nanoTime();

		// saveDataWithBulk 호출
		elasticSearchService.saveDataWithBulk();

		// 종료 시간 기록
		long endTime = System.nanoTime();

		// 시간 계산
		long duration = endTime - startTime;
		System.out.println("saveDataWithBulk 실행 시간 (밀리초): " + (duration / 1_000_000) + "ms");

		// Elasticsearch에서 데이터가 저장되었는지 확인
		Iterable<StatDataDocument> responses = elasticsearchRepository.findAll();

		// 데이터 개수 확인 및 출력
		long count = StreamSupport.stream(responses.spliterator(), false).count();
		System.out.println("saveDataWithBulk로 엘라스틱 서치에 저장된 데이터의 개수 : " + count);
	}

	@Test
	public void testSaveDataPerformance() throws InterruptedException {
		// 시작 시간 기록
		long startTime = System.nanoTime();

		// saveData 호출
		elasticSearchService.saveData();

		// 종료 시간 기록
		long endTime = System.nanoTime();

		// 시간 계산
		long duration = endTime - startTime;
		System.out.println("saveData 실행 시간 (밀리초): " + (duration / 1_000_000) + "ms");

		// Elasticsearch에서 데이터가 저장되었는지 확인
		Iterable<StatDataDocument> responses = elasticsearchRepository.findAll();

		// 데이터 개수 확인 및 출력
		long count = StreamSupport.stream(responses.spliterator(), false).count();
		System.out.println("saveData로 엘라스틱 서치에 저장된 데이터의 개수 : " + count);
	}

	@Test
	public void testSaveDataWithBulkThroughMultiThreadsPerformance() throws InterruptedException {
		long startTime = System.nanoTime();
		elasticSearchService.saveDataWithBulkThroughMultiThreads();
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("saveDataWithBulkThroughMultiThreads 실행 시간 (밀리초): " + (duration / 1_000_000) + "ms");
		// Elasticsearch에서 데이터가 저장되었는지 확인
		Iterable<StatDataDocument> responses = elasticsearchRepository.findAll();
		// 데이터 개수 확인 및 출력
		long count = StreamSupport.stream(responses.spliterator(), false).count();
		System.out.println("saveDataWithBulkThroughMultiThreads 엘라스틱 서치에 저장된 데이터의 개수 : " + count);
	}
}
