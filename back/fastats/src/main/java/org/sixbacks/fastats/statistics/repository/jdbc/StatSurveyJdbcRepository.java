package org.sixbacks.fastats.statistics.repository.jdbc;

import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.sixbacks.fastats.statistics.repository.StatSurveyRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface StatSurveyJdbcRepository extends StatSurveyRepository, ListCrudRepository<StatSurvey, Long> {

}
