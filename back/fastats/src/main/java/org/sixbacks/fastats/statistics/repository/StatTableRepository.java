package org.sixbacks.fastats.statistics.repository;

import java.util.Optional;

import org.sixbacks.fastats.statistics.entity.StatTable;
import org.springframework.stereotype.Repository;

/*
	NOTE: 기능을 정의하기 위해 이용
 */
@Repository
public interface StatTableRepository {
	StatTable save(StatTable statTable);

	Optional<StatTable> findByKosisTbId(String tableId);
}
