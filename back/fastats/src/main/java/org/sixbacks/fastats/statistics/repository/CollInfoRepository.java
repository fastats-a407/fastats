package org.sixbacks.fastats.statistics.repository;

import org.sixbacks.fastats.statistics.entity.Sector;
import org.springframework.data.repository.ListCrudRepository;

public interface CollInfoRepository extends ListCrudRepository<Sector, Long> {

}
