package org.sixbacks.fastats.statistics.repository;

import java.util.List;
import java.util.Optional;

import org.sixbacks.fastats.statistics.entity.StatTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface StatTableRepository {
	StatTable save(StatTable statTable);

	Optional<StatTable> findByKosisTbId(String tableId);

	List<StatTable> findTop500ByCommentAndContent(String comment, String content);

	Page<StatTable> findByCommentAndContent(String comment, String content, Pageable pageable);

	<S extends StatTable> List<S> saveAll(Iterable<S> statTables);

	int updateContent(String content, String comment, Long id);
}

/*
	NOTE: 기능을 정의하기 위해 이용
 */


