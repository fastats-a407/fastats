package org.sixbacks.fastats.statistics.repository.jdbc;

import java.util.List;
import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.sixbacks.fastats.statistics.repository.StatSurveyRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface StatSurveyJdbcRepository extends StatSurveyRepository, ListCrudRepository<StatSurvey, String> {

	@Query("""
    SELECT
        ss.name AS stat_survey_name,
        so.name AS stat_org_name,
        st.name AS stat_table_name,
        st.content AS stat_table_content,
        st.comment AS stat_table_comment,
        st.kosis_view_link AS stat_table_kosis_view_link,
        CASE 
            WHEN LENGTH(ci.start_date) = 6 THEN CONCAT(ci.start_date, '01')
            ELSE ci.start_date
        END AS coll_info_start_date,
        CASE 
            WHEN LENGTH(ci.end_date) = 6 THEN CONCAT(ci.end_date, '01')
            ELSE ci.end_date
        END AS coll_info_end_date
    FROM
        stat_survey ss
    JOIN
        stat_table st ON ss.id = st.survey_id
    JOIN
        coll_info ci ON st.id = ci.stat_table_id
    JOIN
        stat_org so ON ss.org_id = so.id
    """
	)
	List<StatDataDocument> findAllStatData();


	@Query("""
    SELECT
        COUNT(*)
    FROM
        coll_info AS ci
    INNER JOIN
        stat_table AS st ON ci.stat_table_id = st.id
    INNER JOIN
        stat_survey AS ss ON st.survey_id = ss.id
    INNER JOIN
        stat_org AS so ON ss.org_id = so.id
    """)
	Integer countAllStatData();

}
