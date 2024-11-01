package org.sixbacks.fastats.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
public class OrgParserTest {

	@Autowired
	private OrgParser orgParser;

	@Test
	public void parseTest() {
		assertDoesNotThrow(() -> orgParser.parse());
	}
}
