package org.sixbacks.fastats.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:.env")
public class TableCommentServiceTest {

	@Autowired
	private TableCommentService tableCommentService;

	@Test
	public void instanceTest() {
		Assertions.assertNotNull(tableCommentService, "tableCommentService is null");
	}

	@Test
	public void getTableCommentTest() {
		Assertions.assertDoesNotThrow(() -> tableCommentService.fillCommentAndContent());
	}

}
