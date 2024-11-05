package org.sixbacks.fastats.statistics.repository.jdbc;

import java.util.List;

import org.sixbacks.fastats.statistics.entity.StatTable;
import org.sixbacks.fastats.statistics.repository.StatTableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatTableJdbcRepository extends StatTableRepository, ListCrudRepository<StatTable, Long>,
	ListPagingAndSortingRepository<StatTable, Long> {

	List<StatTable> findTop500ByCommentAndContent(String comment, String content);

	Page<StatTable> findByCommentAndContent(String comment,
		String content, Pageable pageable);

	@Modifying
	@Query("UPDATE StatTable SET content = :content , comment = :comment where id = :id")
	int updateContent(@Param("content") String content, @Param("comment") String comment, @Param("id") Long id);

}
