package org.sixbacks.fastats.statistics.repository.jdbc;

import java.util.List;

import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;
import org.sixbacks.fastats.statistics.entity.document.StatNgramDataDocument;
import org.sixbacks.fastats.statistics.repository.StatSurveyRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface StatSurveyJdbcRepository extends StatSurveyRepository, ListCrudRepository<StatSurvey, String> {

	@Query("""
		    SELECT
		      	sc.description AS sector_name,
		        ss.name AS stat_survey_name,
		        so.name AS stat_org_name,
		        st.name AS stat_table_name,
		        st.content AS stat_table_content,
		        st.comment AS stat_table_comment,
		        st.kosis_view_link AS stat_table_kosis_view_link,
		        CASE 
		            WHEN LENGTH(ci.start_date) = 4 THEN CONCAT(ci.start_date, '0101')
		            WHEN LENGTH(ci.start_date) = 6 THEN CONCAT(ci.start_date, '01')
		            ELSE ci.start_date
		        END AS coll_info_start_date,
		        CASE 
		            WHEN LENGTH(ci.end_date) = 4 THEN CONCAT(ci.end_date, '0101')
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
		  		JOIN
						sector sc ON ss.sector_id = sc.id
		""")
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

	@Query("""
		    SELECT 
		        st.name AS title,
		        so.name AS org_name,
		        ss.name AS stat_title,
		        st.kosis_view_link AS stat_link,
		        ci.start_date AS coll_start_date,
		        ci.end_date AS coll_end_date,
		        st.kosis_view_link AS table_link,
				(CASE WHEN ss.name LIKE CONCAT('%', :keyword, '%') THEN 5 ELSE 0 END +
				 CASE WHEN st.content LIKE CONCAT('%', :keyword, '%') THEN 4 ELSE 0 END +
				 CASE WHEN st.comment LIKE CONCAT('%', :keyword, '%') THEN 3 ELSE 0 END +
				 CASE WHEN st.name LIKE CONCAT('%', :keyword, '%') THEN 2 ELSE 0 END +
				 CASE WHEN so.name LIKE CONCAT('%', :keyword, '%') THEN 1 ELSE 0 END) AS relevance_score
		    FROM 
		        stat_table st
		    JOIN 
		        stat_survey ss ON st.survey_id = ss.id
		    JOIN 
		        stat_org so ON ss.org_id = so.id
		    LEFT JOIN 
		        coll_info ci ON ci.stat_table_id = st.id
		    WHERE 
		        (ss.name LIKE CONCAT('%', :keyword, '%') OR
				 st.content LIKE CONCAT('%', :keyword, '%') OR
				 st.comment LIKE CONCAT('%', :keyword, '%') OR
				 st.name LIKE CONCAT('%', :keyword, '%') OR
				 so.name LIKE CONCAT('%', :keyword, '%'))
		 	ORDER BY
		      	relevance_score DESC,
		      	st.name                
		    LIMIT :limit OFFSET :offset
		""")
	List<StatTableListResponse> findStatTableListResponsesByKeywordThroughLike(
			@Param("keyword") String keyword,
			@Param("limit") int limit,
			@Param("offset") int offset);

	/*
		NOTE: MySQL에서 ALTER TABLE을 통한 테이블 별 FULL TEXT INDEX 생성 필요
	 */
	@Query(value = """
		SELECT 
		    st.name AS title,
		    so.name AS org_name,
		    ss.name AS stat_title,
		    st.kosis_view_link AS stat_link,
		    ci.start_date AS coll_start_date,
		    ci.end_date AS coll_end_date,
		    st.kosis_view_link AS table_link,
		    -- 각 테이블의 MATCH 결과에 가중치를 부여
		    (
		      (5 * MATCH(ss.name) AGAINST(:keyword IN BOOLEAN MODE)) +
		      (4 * MATCH(st.name, st.content, st.comment) AGAINST(:keyword IN BOOLEAN MODE)) +
		      (1 * MATCH(so.name) AGAINST(:keyword IN BOOLEAN MODE))
		    ) AS relevance_score
		FROM 
		    stat_table st
		JOIN 
		    stat_survey ss ON st.survey_id = ss.id
		JOIN 
		    stat_org so ON ss.org_id = so.id
		LEFT JOIN 
		    coll_info ci ON ci.stat_table_id = st.id
		WHERE 
		    (
		      MATCH(ss.name) AGAINST(:keyword IN BOOLEAN MODE) OR
		      MATCH(st.name, st.content, st.comment) AGAINST(:keyword IN BOOLEAN MODE) OR
		      MATCH(so.name) AGAINST(:keyword IN BOOLEAN MODE)
		    )
		ORDER BY
		    relevance_score DESC,
		    st.name
		LIMIT :limit OFFSET :offset
		""")
	List<StatTableListResponse> findStatTableListResponsesByKeywordWithFullText(
			@Param("keyword") String keyword,
			@Param("limit") int limit,
			@Param("offset") int offset);


	@Query("""
		    SELECT
		      	sc.description AS sector_name,
		        ss.name AS stat_survey_name,
		        so.name AS stat_org_name,
		        st.name AS stat_table_name,
		        st.content AS stat_table_content,
		        st.comment AS stat_table_comment,
		        st.kosis_view_link AS stat_table_kosis_view_link,
		        CASE 
		            WHEN LENGTH(ci.start_date) = 4 THEN CONCAT(ci.start_date, '0101')
		            WHEN LENGTH(ci.start_date) = 6 THEN CONCAT(ci.start_date, '01')
		            ELSE ci.start_date
		        END AS coll_info_start_date,
		        CASE 
		            WHEN LENGTH(ci.end_date) = 4 THEN CONCAT(ci.end_date, '0101')
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
		  		JOIN
						sector sc ON ss.sector_id = sc.id
		""")
	List<StatNgramDataDocument> findAllStatNgramData();
}
