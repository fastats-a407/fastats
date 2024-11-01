package org.sixbacks.fastats.statistics.service.jdbc;

import org.sixbacks.fastats.statistics.repository.jdbc.SectorJdbcRepository;
import org.sixbacks.fastats.statistics.service.SectorService;
import org.springframework.stereotype.Service;

@Service
public class SectorJdbcServiceImpl implements SectorService {

	private final SectorJdbcRepository sectorJdbcRepository;

	public SectorJdbcServiceImpl(SectorJdbcRepository sectorJdbcRepository) {
		this.sectorJdbcRepository = sectorJdbcRepository;
	}
}
