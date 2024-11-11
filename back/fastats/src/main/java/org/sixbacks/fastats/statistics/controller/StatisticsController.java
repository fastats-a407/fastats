package org.sixbacks.fastats.statistics.controller;

import org.sixbacks.fastats.global.response.ApiResponse;
import org.sixbacks.fastats.statistics.dto.response.CategoryListResponse;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.service.CollInfoService;
import org.sixbacks.fastats.statistics.service.ElasticSearchService;
import org.sixbacks.fastats.statistics.service.SectorService;
import org.sixbacks.fastats.statistics.service.StatSurveyService;
import org.sixbacks.fastats.statistics.service.StatTableService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {

	private final SectorService sectorService;
	private final StatSurveyService statSurveyService;
	private final StatTableService statTableService;
	private final CollInfoService collInfoService;
	private final ElasticSearchService elasticSearchService;

	/*
		TODO: application.yaml 내 data module 관련 설정이 JDBC 일 때 초기화하도록 추가
	 */
	public StatisticsController(@Qualifier("sectorJdbcServiceImpl") SectorService sectorService,
		@Qualifier("statSurveyJdbcServiceImpl") StatSurveyService statSurveyService,
		@Qualifier("statTableJdbcServiceImpl") StatTableService statTableService,
		@Qualifier("collInfoJdbcServiceImpl") CollInfoService collInfoService,
		@Qualifier("elasticSearchServiceImpl") ElasticSearchService elasticSearchService) {
		this.sectorService = sectorService;
		this.statSurveyService = statSurveyService;
		this.statTableService = statTableService;
		this.collInfoService = collInfoService;
		this.elasticSearchService = elasticSearchService;
	}

	/*
		TODO: ES Document 재작성 완료 후 ctg 관련 로직 작업 필요
	 */
	@GetMapping("")
	public ResponseEntity<ApiResponse<Page<StatTableListResponse>>> getStatTableList(@RequestParam String keyword,
		@RequestParam int page, @RequestParam(defaultValue = "10") int size, @RequestParam String ctg) {

		// size가 비어 있으면 위 RequestParam에서 10으로 설정되나, 유저가 옳지 않은 값 입력 시 size 제한
		if (size != 10 && size != 20 && size != 30) {
			size = 10;
		}

		Page<StatTableListResponse> pages = elasticSearchService.searchByKeyword(keyword, page, size);
		ApiResponse<Page<StatTableListResponse>> response = ApiResponse.success("검색이 성공했습니다.", pages);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/categories")
	public ResponseEntity<ApiResponse<CategoryListResponse>> getCategoryList(@RequestParam String keyword) {

		ApiResponse<CategoryListResponse> response = ApiResponse.success("카테고리 검색에 성공했습니다.", null);

		return null;
	}

	/*
		연관 검색어 추천 관련 Fast API 서버 응답 확정 시 구현
		NOTE: 현재 generic을 ?로 설정했으므로 기능 코드 선제 작성 가능
	 */
	@GetMapping("/suggestions")
	public ResponseEntity<ApiResponse<?>> getKeywordSuggestions(@RequestParam String keyword) {
		return null;
	}

	@PostMapping("/elastic")
	public ResponseEntity<ApiResponse<Void>> saveData() {
		elasticSearchService.saveData();
		return ResponseEntity.ok(ApiResponse.success("Elastic Search 데이터 적재를 성공했습니다.", null));
	}

}
