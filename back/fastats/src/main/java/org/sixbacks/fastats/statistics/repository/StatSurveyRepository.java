package org.sixbacks.fastats.statistics.repository;

import java.util.Optional;

import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.springframework.stereotype.Repository;

/*
	NOTE: 기능을 정의하기 위해 이용
 */
@Repository
public interface StatSurveyRepository {
	StatSurvey save(StatSurvey survey);

	Optional<StatSurvey> findByOrgNameAndName(String orgName, String name);
}
