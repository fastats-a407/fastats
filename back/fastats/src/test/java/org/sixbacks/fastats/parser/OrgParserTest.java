package org.sixbacks.fastats.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.sixbacks.fastats.statistics.repository.jdbc.ElasticSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:.env")
public class OrgParserTest {

	@Autowired
	private OrgParser orgParser;

	@MockBean
	private ElasticSearchRepository elasticSearchRepository;

	@Test
	public void parseTest() {
		assertDoesNotThrow(() -> orgParser.parse());
	}
}
