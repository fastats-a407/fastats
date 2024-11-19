package org.sixbacks.fastats.statistics.repository;

import java.util.Optional;

import org.sixbacks.fastats.statistics.entity.StatOrganization;
import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;

/*
	NOTE: 기능을 정의하기 위해 이용
 */
@Repository
public interface StatSurveyRepository {
	StatSurvey save(StatSurvey survey);

	Optional<StatSurvey> findByRefOrgIdAndName(AggregateReference<StatOrganization, Long> refOrgId, String name);
}
