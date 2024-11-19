package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.entity.Sector;
import org.sixbacks.fastats.statistics.repository.SectorRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface SectorJdbcRepository extends SectorRepository, ListCrudRepository<Sector, Long> {

}
