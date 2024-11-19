package org.sixbacks.fastats.statistics.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sixbacks.fastats.global.error.ErrorCode;
import org.sixbacks.fastats.global.exception.CustomException;
import org.sixbacks.fastats.statistics.dto.response.CategoryListResponse;
import org.sixbacks.fastats.statistics.dto.response.StatSurveyInfoDto;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.dto.response.TableByDto;
import org.sixbacks.fastats.statistics.service.CollInfoService;
import org.sixbacks.fastats.statistics.service.ElasticSearchService;
import org.sixbacks.fastats.statistics.service.SectorService;
import org.sixbacks.fastats.statistics.service.StatSurveyService;
import org.sixbacks.fastats.statistics.service.StatTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatisticsController.class)
public class StatisticsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	@Qualifier("elasticSearchServiceImpl")
	private ElasticSearchService elasticSearchService;

	@MockBean
	@Qualifier("sectorJdbcServiceImpl")
	private SectorService sectorService;

	@MockBean
	@Qualifier("statSurveyJdbcServiceImpl")
	private StatSurveyService statSurveyService;

	@MockBean
	@Qualifier("statTableJdbcServiceImpl")
	private StatTableService statTableService;

	@MockBean
	@Qualifier("collInfoJdbcServiceImpl")
	private CollInfoService collInfoService;

	@Test
	@DisplayName("통계표 검색 결과 불러오기 - 타당한 요청에 대해 적절한 ApiResponse 요청 반환")
	void getStatTableList_ReturnsValidResponse_WhenRequestIsValid() throws Exception {

		// Given: 모킹에 필요한 결과값 및 searchByKeyword() 결과값
		List<StatTableListResponse> mockContent = List.of(
			new StatTableListResponse("mockTitle", new StatSurveyInfoDto("mockOrg", "mockTitle", "mockStatLink"),
				"2024", "2024", "mockLink")
		);
		Page<StatTableListResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(0, 10), mockContent.size());

		when(elasticSearchService.searchByKeyword(anyString(), anyInt(), anyInt())).thenReturn(mockPage);

		// When & Then: MockMvc로 요청 보내기 및 결과 검증
		mockMvc.perform(get("/api/v1/stats?keyword=test&page=0&size=10&ctg=A")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("검색이 성공했습니다."))
			.andExpect(jsonPath("$.data").exists());

		// 서비스 호출 확인
		verify(elasticSearchService).searchByKeyword("test", 0, 10);
	}

	@Test
	@DisplayName("통계표 검색 결과 불러오기 - 잘못된 요청에 대해 적절한 ApiResponse 요청 반환")
	void getStatTableList_ThrowsCustomExceptioin_WhenRequestIsInValid() throws Exception {

		// Given: 모킹에 필요한 예외 설정
		when(elasticSearchService.searchByKeyword(anyString(), eq(500), anyInt()))
			.thenThrow(new CustomException(ErrorCode.STAT_ILL_REQUEST));

		// When & Then: MockMvc로 요청 보내기 및 결과 검증
		mockMvc.perform(get("/api/v1/stats?keyword=test&page=500&size=10&ctg=A")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value(ErrorCode.STAT_ILL_REQUEST.getCode())) // 예외 발생 시 예상되는 상태 코드
			.andExpect(jsonPath("$.message").value(ErrorCode.STAT_ILL_REQUEST.getMessage())) // 에러 메시지 확인
			.andExpect(jsonPath("$.data").doesNotExist());

		// 서비스 호출 확인
		verify(elasticSearchService).searchByKeyword("test", 500, 10);
	}

	@Test
	@DisplayName("카테고리 검색 결과 불러오기 - 타당한 요청에 대해 적절한 ApiResponse 요청 반환")
	void getCategoriesByKeyword_ReturnsValidResponse_WhenRequestIsValid() throws Exception {

		// Given: 모킹에 필요한 결과값 및 getCate() 결과값

		String keyword = "인구";

		TableByDto byThemeDto = new TableByDto("인구", 3);
		TableByDto bySurveyDto = new TableByDto("인구총조사", 5);
		CategoryListResponse mockContent = new CategoryListResponse(List.of(byThemeDto), List.of(bySurveyDto), 3, 5);

		when(elasticSearchService.getCategoriesByKeyword(keyword)).thenReturn(mockContent);

		// When & Then: MockMvc로 요청 보내기 및 결과 검증
		mockMvc.perform(get("/api/v1/stats/categories?keyword=" + keyword)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("카테고리 검색에 성공했습니다."))
			.andExpect(jsonPath("$.data").exists());

		// 서비스 호출 확인
		verify(elasticSearchService).getCategoriesByKeyword(keyword);
	}

	@Test
	@DisplayName("카테고리 검색 결과 불러오기 - 잘못된 요청에 대해 적절한 ApiResponse 요청 반환")
	void getCategoriesByKeyword_ThrowsCustomExceptioin_WhenRequestIsInValid() throws Exception {

		// Given: 모킹에 필요한 결과값 및 getCate() 결과값

		String keyword = "인구";

		TableByDto byThemeDto = new TableByDto("인구", 3);
		TableByDto bySurveyDto = new TableByDto("인구총조사", 5);
		CategoryListResponse mockContent = new CategoryListResponse(List.of(byThemeDto), List.of(bySurveyDto), 3, 5);

		when(elasticSearchService.getCategoriesByKeyword(keyword)).thenReturn(mockContent);

		// When & Then: MockMvc로 요청 보내기 및 결과 검증
		mockMvc.perform(get("/api/v1/stats/categories?keyword=" + keyword)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value(ErrorCode.STAT_ILL_REQUEST.getCode())) // 예외 발생 시 예상되는 상태 코드
			.andExpect(jsonPath("$.message").value(ErrorCode.STAT_ILL_REQUEST.getMessage())) // 에러 메시지 확인
			.andExpect(jsonPath("$.data").doesNotExist());

		// 서비스 호출 확인
		verify(elasticSearchService).getCategoriesByKeyword(keyword);
	}
}
