package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.entity.StatOrganization;
import org.sixbacks.fastats.statistics.repository.StatOrganizationRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface StatOrganizationJdbcRepository extends ListCrudRepository<StatOrganization, Long>,
	StatOrganizationRepository {
}
