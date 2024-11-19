package org.sixbacks.fastats.parser;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sixbacks.fastats.statistics.entity.StatTable;
import org.sixbacks.fastats.statistics.repository.StatTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:.env")
public class StatTableRepositoryTest {

	@Qualifier("statTableJdbcRepository")
	@Autowired
	private StatTableRepository repository;

	@Test
	public void instanceTest() {
		Assertions.assertNotNull(repository, "repository is null");
	}

	@Test
	public void getTop500Test() {
		List<StatTable> tableList = repository.findTop500ByCommentAndContent(null, null);
		Assertions.assertNotNull(tableList, "tableList is null");
		Assertions.assertEquals(500, tableList.size(), "tableList is less than 500");
	}

	@Test
	public void getWithLimitAndOffsetTest() {
		Pageable page = PageRequest.of(0, 500);
		Page<StatTable> tableList = repository.findByCommentAndContent(null, null, page);
		Assertions.assertNotNull(tableList, "tableList is null");
		System.out.println(tableList.getTotalElements());
		System.out.println(tableList.getTotalPages());
		var resultList = tableList.getContent();
		Assertions.assertEquals(500, resultList.size(), "tableList is less than 500");

	}

	@Test
	public void getTop500Test2() {
		long id = 0;
		List<StatTable> tableList = repository.findTop500ByIdGreaterThanAndCommentIsNullAndContentIsNull(id);

		Assertions.assertNotNull(tableList, "tableList is null");
		Assertions.assertEquals(500, tableList.size(), "tableList is less than 500");

		id = 300000;
		tableList = repository.findTop500ByIdGreaterThanAndCommentIsNullAndContentIsNull(id);

		Assertions.assertNotNull(tableList, "tableList is null");
		Assertions.assertEquals(0, tableList.size(), "tableList size is not 0");

	}

}
