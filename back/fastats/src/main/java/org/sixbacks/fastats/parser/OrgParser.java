package org.sixbacks.fastats.parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sixbacks.fastats.statistics.entity.StatOrganization;
import org.sixbacks.fastats.statistics.repository.StatOrganizationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrgParser {
	private final StatOrganizationRepository organizationRepository;

	public OrgParser(
		@Qualifier("statOrganizationJdbcRepository") StatOrganizationRepository organizationRepository) {
		this.organizationRepository = organizationRepository;
	}

	@Transactional
	public void parse() {
		try (InputStream fis = OrgParser.class.getClassLoader().getResourceAsStream("통계표목록/작성기관.xlsx")) {
			if (fis == null) {
				log.error("리소스를 찾을 수 없습니다.");
				return;
			}
			try (Workbook workbook = new XSSFWorkbook(fis)) {

				Sheet sheet = workbook.getSheetAt(0);

				for (Row row : sheet) {
					if (row.getCell(0).getCellType() == CellType.STRING) {
						continue;
					}
					StatOrganization org = StatOrganization.from(
						(int)row.getCell(0).getNumericCellValue(),
						row.getCell(1).getStringCellValue()
					);
					log.info("기관 저장 : {}", org);
					organizationRepository.save(org);
				}
			}
		} catch (IOException e) {
			log.error("기관 코드 저장시 오류 발생 {}", e.getMessage(), e.getCause());
		}
	}
}


