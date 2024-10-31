package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.entity.StatTable;
import org.sixbacks.fastats.statistics.repository.StatTableRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface StatTableJdbcRepository extends StatTableRepository, ListCrudRepository<StatTable, Long> {

}
