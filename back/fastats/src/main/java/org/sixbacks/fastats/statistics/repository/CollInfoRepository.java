package org.sixbacks.fastats.statistics.repository;

import org.sixbacks.fastats.statistics.entity.CollInfo;
import org.springframework.stereotype.Repository;

/*
	NOTE: 기능을 정의하기 위해 이용
 */
@Repository
public interface CollInfoRepository {
	CollInfo save(CollInfo collInfo);

}
