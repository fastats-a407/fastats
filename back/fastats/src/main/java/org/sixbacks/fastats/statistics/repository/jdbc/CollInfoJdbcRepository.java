package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.entity.CollInfo;
import org.sixbacks.fastats.statistics.repository.CollInfoRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface CollInfoJdbcRepository extends ListCrudRepository<CollInfo, Long>, CollInfoRepository {

}
