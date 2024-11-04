package org.sixbacks.fastats.statistics.repository.jdbc;

import java.util.List;

import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.sixbacks.fastats.statistics.repository.StatSurveyRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface StatSurveyJdbcRepository extends StatSurveyRepository, ListCrudRepository<StatSurvey, Long> {
	@Query("""
		SELECT
		                                           ss.name AS stat_survey_name,
		                                           ss.org_name AS stat_org_name,
		                                           st.name AS stat_table_name,
		                                           st.content AS stat_table_content,
		                                           st.comment AS stat_table_comment,
		                                           st.kosis_view_link AS stat_table_kosis_view_link,
		                                           ci.start_date AS coll_info_start_date,
		                                           ci.end_date AS coll_info_end_date
		                                       FROM
		                                           stat_survey ss
		                                       JOIN
		                                           stat_table st ON ss.id = st.survey_id
		                                       JOIN
		                                           coll_info ci ON st.id = ci.stat_table_id
		""")
	List<StatDataDocument> findAllStatData();
}
