package org.sixbacks.fastats.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import lombok.Getter;

public class XLSParser {
	static final String SUBJECT = "1";

	public static void main(String[] args) throws IOException {
		// 리소스 파일을 InputStream 으로 불러오기

		try (InputStream is = XLSParser.class.getClassLoader().getResourceAsStream("통계표목록/주제별통계.xls")) {
			if (is == null) {
				System.out.println("리소스를 찾을 수 없습니다.");
				return;
			}

			// InputStream 을 사용하여 HSSFWorkbook 생성
			HSSFWorkbook workbook = new HSSFWorkbook(is);

			Iterator<Sheet> iterator = workbook.sheetIterator();
			int subjectIdx = 1;

			while (iterator.hasNext()) {
				Sheet curSheet = iterator.next();
				for (int rowIndex = 4; rowIndex <= curSheet.getLastRowNum(); rowIndex++) {
					Row row = curSheet.getRow(rowIndex);

					if (row != null) {
						// 현재 row 의 level 컬럼이 1
						// 새로운 주제분류 시작. idx 1 증가
						Cell rowCell = row.getCell(Column.LEVEL.getValue());
						if (rowCell != null && SUBJECT.equals(rowCell.getStringCellValue())) {
							// TODO : 주제 코드 및 주세 설명 DB 삽입. 후 pk 값 가져오기
							subjectIdx++;
						}

						//현재 row 가 통계표 명을 포함한 열이 아닐 경우 pass
						if ((row.getCell(Column.STATS_LINK.getValue()) == null) || (Strings.isEmpty(
							row.getCell(Column.STATS_LINK.getValue()).getStringCellValue()))) {
							continue;
						}

						// 기관 , 통계명 parsing
						String origin = row.getCell(Column.STATS_ORIGIN.getValue()).getStringCellValue();
						if (Strings.isEmpty(origin)) {
							// TODO: 통계 출처 칸이 비어 있는 경우, 현재 2개의 예외만 존재. 모두 "통계청, 국가자산통계" 에 속하므로, 일단 하드 코딩 해둠. 나중에 바꿔야함.
							// System.out.println("통계 출처 비었음.");
							// System.out.println("Sheet : " + curSheet.getSheetName());
							// System.out.println("Row : " + rowIndex);
							String agency = "통계청";
							String statsName = "국가자산통계";
						} else {

							String agency = origin.split(",")[0];
							String statsName = origin.split(",")[1];
							statsName = statsName.trim().substring(1, statsName.length() - 1);
						}
						//수록 시작 시기, 주기 ,종료시기 parsing
						String term = row.getCell(Column.STATS_TERM.getValue()).getStringCellValue();
						if (Strings.isEmpty(term)) {
							// TODO: 파일의 경우에도 알맞은 수록 시기 파싱
							// String tableName = row.getCell(Column.STATS_NAME.getValue())
							// 	.getStringCellValue()
							// 	.trim();
							// // (파일) 로 시작하는 것을 확인했다.
							// // 시작 년도 == 끝나는 년도 (파일) {년도} 파일명
							// if (!tableName.startsWith("(파일)")) {
							// 	System.out.println("There is Empty Term!");
							// 	System.out.println("Sheet : " + curSheet.getSheetName());
							// 	System.out.println("Row : " + rowIndex);
							// 	System.out.println("Table Name : " + tableName);
							// 	System.out.println("This table doesn't starts with (파일)");
							// } else {
							// 	// (파일) 로 시작하는 통계명.
							// 	// 날짜를 파싱하고 싶은데, 예외가 있네?
							// 	// 기본적으로 (파일) {년도}
							//
							// 	// 아닌 경우 (파일)수출입물류통계 <- 얘는 특이하다. 기간을 내가 어떻게 알지?
							// 	if (tableName.split(" ").length < 2) {
							// 		System.out.println(tableName);
							// 	} else if (!tableName.split(" ")[1].trim().substring(0, 4).matches("-?\\d+")) {
							// 		System.out.println("new Exception with no YEAR");
							// 		System.out.println("Sheet : " + curSheet.getSheetName());
							// 		System.out.println("Row : " + rowIndex);
							// 		System.out.println("Table Name : " + tableName);
							// 	}
							// }

						} else {
							String[] terms = term.split(" ");
							if (terms[0].endsWith("년")) {

							} else if (terms[0].endsWith("분기")) {

							} else if (terms[0].endsWith("월")) {

							} else if (terms[0].endsWith("반기")) {

							} else if (terms[0].endsWith("일")) {

							} else {
								if (terms[0].endsWith("부정기") || terms[0].endsWith("IR")) {

								} else {
									System.out.println(terms[0]);
								}
							}
						}
					}

				}

			}

			// 워크북 닫기
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Getter
	public enum Column {
		LEVEL(0), STATS_NAME(1), STATS_LINK(2), STATS_ORIGIN(3), STATS_TERM(4), STATS_ID(7);
		private final int value;

		Column(int value) {
			this.value = value;
		}

	}
}
