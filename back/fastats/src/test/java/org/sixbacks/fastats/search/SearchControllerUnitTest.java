package org.sixbacks.fastats.search;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.sixbacks.fastats.search.controller.SearchController;
import org.sixbacks.fastats.search.dto.SearchAutoCompleteResponseDto;
import org.sixbacks.fastats.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SearchController.class)
public class SearchControllerUnitTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SearchService searchService;

	@Test
	public void testSearch() throws Exception {
		List<SearchAutoCompleteResponseDto> expected = Arrays.asList(
				SearchAutoCompleteResponseDto.builder()
						.keyword("대한민국1")
						.build(),
				SearchAutoCompleteResponseDto.builder()
						.keyword("대한민국2")
						.build()
		);

	}
}