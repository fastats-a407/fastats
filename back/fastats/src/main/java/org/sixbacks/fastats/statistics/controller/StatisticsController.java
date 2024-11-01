package org.sixbacks.fastats.statistics.controller;

import java.util.List;

import org.sixbacks.fastats.global.response.ApiResponse;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.service.CollInfoService;
import org.sixbacks.fastats.statistics.service.SectorService;
import org.sixbacks.fastats.statistics.service.StatSurveyService;
import org.sixbacks.fastats.statistics.service.StatTableService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	/*
		TODO: application.yaml 내 data module 관련 설정이 JDBC 일 때 초기화하도록 추가
	 */
	public StatisticsController(
		@Qualifier("sectorJdbcServiceImpl") SectorService sectorService,
		@Qualifier("statSurveyJdbcServiceImpl") StatSurveyService statSurveyService,
		@Qualifier("statTableJdbcServiceImpl") StatTableService statTableService,
		@Qualifier("collInfoJdbcServiceImpl") CollInfoService collInfoService) {
		this.sectorService = sectorService;
		this.statSurveyService = statSurveyService;
		this.statTableService = statTableService;
		this.collInfoService = collInfoService;
	}

	@GetMapping("?keyword={keyword}&page={page}&size={size}&ctg={ctg}")
	public ResponseEntity<ApiResponse<List<StatTableListResponse>>> getStatTableList(
		@RequestParam String keyword,
		@RequestParam int page,
		@RequestParam int size,
		@RequestParam String ctg
	) {

		return null;
	}

	@GetMapping("/categories?keyword={keyword}")
	public ResponseEntity<ApiResponse<CategoryListResponse>> getCategoryList(
		@RequestParam String keyword
	) {

		return null;
	}

	/*
		연관 검색어 추천 관련 Fast API 서버 응답 확정 시 구현
		NOTE: 현재 generic을 ?로 설정했으므로 기능 코드 선제 작성 가능
	 */
	@GetMapping("/suggestions?keyword={keyword}")
	public ResponseEntity<ApiResponse<?>> getKeywordSuggestions(
		@RequestParam String keyword
	) {

		return null;
	}

}
