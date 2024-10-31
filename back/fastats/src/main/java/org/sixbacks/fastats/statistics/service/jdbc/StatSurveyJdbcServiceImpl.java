package org.sixbacks.fastats.statistics.service.jdbc;

import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.sixbacks.fastats.statistics.service.StatSurveyService;

public class StatSurveyJdbcServiceImpl implements StatSurveyService {

	private final StatSurveyJdbcRepository statSurveyJdbcRepository;

	public StatSurveyJdbcServiceImpl(StatSurveyJdbcRepository statSurveyJdbcRepository) {
		this.statSurveyJdbcRepository = statSurveyJdbcRepository;
	}
}
