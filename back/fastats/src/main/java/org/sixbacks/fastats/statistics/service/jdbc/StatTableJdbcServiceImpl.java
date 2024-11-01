package org.sixbacks.fastats.statistics.service.jdbc;

import org.sixbacks.fastats.statistics.repository.jdbc.StatTableJdbcRepository;
import org.sixbacks.fastats.statistics.service.StatTableService;
import org.springframework.stereotype.Service;

@Service
public class StatTableJdbcServiceImpl implements StatTableService {

	private final StatTableJdbcRepository statTableJdbcRepository;

	public StatTableJdbcServiceImpl(StatTableJdbcRepository statTableJdbcRepository) {
		this.statTableJdbcRepository = statTableJdbcRepository;
	}
}
