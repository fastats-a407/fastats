package org.sixbacks.fastats.statistics.repository;

import java.util.Optional;

import org.sixbacks.fastats.statistics.entity.StatOrganization;
import org.springframework.stereotype.Repository;

@Repository
public interface StatOrganizationRepository {
	StatOrganization save(StatOrganization organization);

	Optional<StatOrganization> findByName(String name);
}
