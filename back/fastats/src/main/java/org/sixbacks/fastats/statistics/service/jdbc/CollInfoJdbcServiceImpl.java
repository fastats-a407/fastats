package org.sixbacks.fastats.statistics.service.jdbc;

import org.sixbacks.fastats.statistics.repository.jdbc.CollInfoJdbcRepository;
import org.sixbacks.fastats.statistics.service.CollInfoService;

public class CollInfoJdbcServiceImpl implements CollInfoService {

	private final CollInfoJdbcRepository jdbcCollInfoRepository;

	public CollInfoJdbcServiceImpl(CollInfoJdbcRepository jdbcCollInfoRepository) {
		this.jdbcCollInfoRepository = jdbcCollInfoRepository;
	}

}
