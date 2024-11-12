package org.sixbacks.fastats.search.controller;

import org.sixbacks.fastats.global.response.ApiResponse;
import org.sixbacks.fastats.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/search")
@Slf4j
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/autocompletes")
	public ResponseEntity<?> search(@RequestParam String keyword) {
		log.info("search keyword: {}", keyword);
		var list = searchService.searchAutoComplete(keyword);
		return ResponseEntity.ok(ApiResponse.success("자동완성 불러오기에 성공했습니다.", list));
	}
}
