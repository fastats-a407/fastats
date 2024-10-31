package org.sixbacks.fastats.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sixbacks.fastats.statistics.entity.CollInfo;
import org.sixbacks.fastats.statistics.entity.Sector;
import org.sixbacks.fastats.statistics.entity.StatSurvey;
import org.sixbacks.fastats.statistics.entity.StatTable;
import org.sixbacks.fastats.statistics.repository.CollInfoRepository;
import org.sixbacks.fastats.statistics.repository.SectorRepository;
import org.sixbacks.fastats.statistics.repository.StatSurveyRepository;
import org.sixbacks.fastats.statistics.repository.StatTableRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

//TODO : 나중에 statistic 도메인 안의 배치/스케쥴러 모듈 안으로 들어가야 함.
@Slf4j
@Component
public class XLSParser {
	static final String SUBJECT = "1";

	private final SectorRepository sectorRepository;
	private final CollInfoRepository collInfoRepository;
	private final StatSurveyRepository statSurveyRepository;
	private final StatTableRepository statTableRepository;
	Set<String> tableIds = new HashSet<>();

	public XLSParser(
		@Qualifier("sectorJdbcRepository") SectorRepository sectorRepository,
		@Qualifier("collInfoJdbcRepository") CollInfoRepository collInfoRepository,
		@Qualifier("statSurveyJdbcRepository") StatSurveyRepository statSurveyRepository,
		@Qualifier("statTableJdbcRepository") StatTableRepository statTableRepository) {
		this.sectorRepository = sectorRepository;
		this.collInfoRepository = collInfoRepository;
		this.statSurveyRepository = statSurveyRepository;
		this.statTableRepository = statTableRepository;
	}

	private String parseTableId(Row row) {
		String tableId = row.getCell(Column.STATS_ID.value).getStringCellValue();
		if (tableId == null || tableId.isEmpty()) {
			log.warn("통계표 아이디가 존재하지 않습니다.");
			return null;
		} else {
			return tableId.trim();
		}
	}

	private String parseTableLink(Row row) {
		Hyperlink link = row.getCell(Column.STATS_LINK.value).getHyperlink();
		if (link == null) {
			log.warn("통계표 링크가 존재하지 않습니다.");
			return null;
		} else {
			return link.getAddress();
		}
	}

	private String parseTableName(Row row) {
		String tableName = row.getCell(Column.STATS_NAME.value)
			.getStringCellValue()
			.trim();
		if (Strings.isEmpty(tableName)) {
			log.warn("통계표 명이 비었습니다. : Row {}", row.getRowNum());
			return null;
		}
		return tableName;
	}

	private String[][] parseStatDate(Row row) {
		String term = row.getCell(Column.STATS_TERM.getValue()).getStringCellValue();
		String[] terms = term.trim().split(",");
		String[][] infos = new String[terms.length][3];
		for (int i = 0; i < terms.length; i++) {
			term = terms[i].trim();
			if (Strings.isEmpty(term)) {
				continue;
			}
			String[] splitTerm = term.split(" ");
			if (splitTerm.length != 4) {
				log.warn("형식이 다른 시기 존재 : TEXT {} Row {}, Sheet {}", term, row.getRowNum(),
					row.getSheet().getSheetName());
			}

			infos[i][0] = parseStatStartDate(splitTerm[1]);
			infos[i][1] = parseStatEndDate(splitTerm[3]);
			infos[i][2] = parseStatPeriod(splitTerm[0]);
		}
		return infos;
	}

	private String parseStatEndDate(String term) {
		String endDate = term.substring(0, term.length() - 1);
		if (!term.endsWith(")") || !isInteger(endDate)) {
			log.warn("종료 시기가 형식에 맞지 않는 경우 존재 : {}", term);
			return null;
		}
		return endDate;
	}

	private String parseStatStartDate(String term) {
		String startDate = term.substring(1);
		if (!term.startsWith("(") || !isInteger(startDate)) {
			log.warn("시작 시기가 형식에 맞지 않는 경우 존재 : {}", term);
			return null;
		}
		return startDate;
	}

	private String parseStatPeriod(String term) {
		StringBuilder termBuilder = new StringBuilder();
		/// 수록 주기 parsing
		if (term.endsWith("년")) {
			if (term.length() == 1) {
				termBuilder.append(1);
			} else {
				termBuilder.append(term, 0, term.length() - 1); //"년" 빼고 파싱;
			}
			termBuilder.append(Term.YEAR.value);
		} else if (term.endsWith("분기") && term.length() == 2) {
			termBuilder.append(Term.QUATER.value);
		} else if (term.endsWith("월") && term.length() == 1) {
			termBuilder.append(Term.MONTH.value);
		} else if (term.endsWith("반기") && term.length() == 2) {
			termBuilder.append(Term.SEMIANNUAL.value);
		} else if (term.endsWith("일") && term.length() == 1) {
			termBuilder.append(Term.DAY.value);
		} else if (term.endsWith("부정기") || term.endsWith("IR")) {
			termBuilder.append(Term.IR.value);
		} else {
			log.warn("비정기 주기에 예외 존재 : {}", term);
			return null;
		}

		return termBuilder.toString();
	}

	@Transactional
	protected long parseSector(Row row) {

		// TODO : 주제 코드 및 주세 설명 DB 삽입. 후 pk 값 가져오기
		String code = parseSectorCode(row);
		String desc = parseSectorDesc(row);
		Sector newSector = Sector.from(code, desc);
		log.info("code : {}, desc : {}", code, desc);
		Sector existSector = sectorRepository.findByCode(code).orElse(null);
		if (existSector == null) {
			return sectorRepository.save(newSector).getId();
		} else {
			return existSector.getId();
		}
	}

	private String parseSectorCode(Row row) {
		String code = row.getCell(Column.SECTOR_CODE.value).getStringCellValue();
		code = code.split(" ")[2];
		return code;
	}

	private String parseSectorDesc(Row row) {
		return row.getCell(Column.STATS_NAME.value).getStringCellValue().trim();
	}

	private String[] parseOrgName(Row row) {
		String origin = row.getCell(Column.STATS_ORIGIN.getValue()).getStringCellValue();
		String[] names = new String[2];
		if (Strings.isEmpty(origin)) {
			// TODO: 통계 출처 칸이 비어 있는 경우, 현재 2개의 예외만 존재. 모두 "통계청, 국가자산통계" 에 속하므로, 일단 하드 코딩 해둠. 나중에 바꿔야함.
			log.warn("통계 출처가 비었습니다.");
			names[0] = "통계청";
			names[1] = "국가자산통계";
		} else {

			names[0] = origin.split(",")[0];
			names[1] = origin.split(",")[1];
			names[1] = names[1].trim().substring(1, names[1].length() - 1);
		}
		return names;
	}

	private boolean isInteger(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}

		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public void parse() {
		// 리소스 파일을 InputStream 으로 불러오기

		try (InputStream is = XLSParser.class.getClassLoader().getResourceAsStream("통계표목록/주제별통계.xls")) {
			if (is == null) {
				log.error("리소스를 찾을 수 없습니다.");
				return;
			}

			// InputStream 을 사용하여 HSSFWorkbook 생성
			HSSFWorkbook workbook = new HSSFWorkbook(is);

			Iterator<Sheet> iterator = workbook.sheetIterator();
			long sectorIdx = 1;

			while (iterator.hasNext()) {
				Sheet curSheet = iterator.next();
				for (int rowIndex = 3; rowIndex <= curSheet.getLastRowNum(); rowIndex++) {
					Row row = curSheet.getRow(rowIndex);
					if (row == null) {
						continue;
					}

					Cell rowCell = row.getCell(Column.LEVEL.value);
					if (rowCell == null || !isInteger(rowCell.getStringCellValue())) {
						continue;
					}

					// 현재 row 의 level 컬럼이 1
					// 새로운 주제분류 시작. idx 1 증가
					if (SUBJECT.equals(rowCell.getStringCellValue())) {
						sectorIdx = parseSector(row);
					}

					//현재 row 가 통계표 명을 포함한 열이 아닐 경우 pass
					if ((row.getCell(Column.STATS_LINK.getValue()) == null) || (Strings.isEmpty(
						row.getCell(Column.STATS_LINK.getValue()).getStringCellValue()))) {
						continue;
					}

					// 기관 , 통계명 parsing
					String[] names = parseOrgName(row);
					String orgName = names[0];
					String name = names[1];

					//수록 시작 시기, 주기 ,종료시기 parsing
					String[][] terms = parseStatDate(row);

					/// 통계표명 파싱
					String tableName = parseTableName(row);

					/// 통계표 링크 파싱
					String tableLink = parseTableLink(row);

					// 통계표 아이디 파싱
					String tableId = parseTableId(row);

					// Save StatSurvey
					StatSurvey statSurvey = StatSurvey.from(
						sectorIdx,
						101, // TODO : 기관 코드 어디서 가져와야함
						orgName,
						name
					);
					long statSurveyId = statSurveyRepository.findByOrgNameAndName(orgName, name)
						.orElseGet(() -> statSurveyRepository.save(statSurvey)).getId();

					// Save statTable
					StatTable statTable = StatTable.from(
						statSurveyId,
						tableName,
						null,
						null,
						tableId,
						tableLink
					);
					long statTableId = statTableRepository.findByKosisTbId(tableId)
						.map(StatTable::getId)
						.orElseGet(
							() -> {
								log.warn("새로운 테이블 삽입. : TableID {}", tableId);
								return statTableRepository.save(statTable).getId();
							}
						);

					// Save CollInfo
					for (String[] term : terms) {
						CollInfo collInfo = CollInfo.from(
							statTableId,
							term[0],
							term[1],
							term[2]
						);
						log.info("Saving CollInfo: {}", collInfo);
						collInfoRepository.save(collInfo);
					}
				}

			}

			// 워크북 닫기
			workbook.close();
		} catch (IOException e) {
			log.error("리소스 열기 실패", e.getCause());
		}
	}

	@Getter
	public enum Column {
		LEVEL(0), STATS_NAME(1), STATS_LINK(2), STATS_ORIGIN(3), STATS_TERM(4), SECTOR_CODE(6), STATS_ID(7);
		private final int value;

		Column(int value) {
			this.value = value;
		}

	}

	@Getter
	public enum Term {
		YEAR("Y"), MONTH("1M"), DAY("1D"), SEMIANNUAL("1H"), QUATER("1Q"), IR("IR");
		private final String value;

		Term(String value) {
			this.value = value;
		}

	}
}
