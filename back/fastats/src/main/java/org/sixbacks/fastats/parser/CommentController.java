package org.sixbacks.fastats.parser;

import java.util.Collections;

import org.sixbacks.fastats.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

@Controller
@RequestMapping("/api/v1/comment")
public class CommentController {
	private final TableCommentService tableCommentService;

	public CommentController(TableCommentService tableCommentService) {
		this.tableCommentService = tableCommentService;
	}

	@GetMapping("/update")
	public ResponseEntity<ApiResponse<String>> updateComment() {

		tableCommentService.fillCommentAndContent();
		RestClient mmClient = RestClient.create();
		mmClient.post()
			.uri("https://meeting.ssafy.com/hooks/h8jzg1cj1bnmiysnya8yb55xro")
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Collections.singletonMap("text", "코멘트 저장 끝"))
			.retrieve();
		return ResponseEntity.ok(ApiResponse.success("Saved", "Saved"));
	}

}
