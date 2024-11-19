package org.sixbacks.fastats.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sixbacks.fastats.statistics.dto.preprocessing.TableCommentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:.env")
public class TableCommentParserTest {
	@Autowired
	TableCommentParser tableCommentParser;

	@Test
	public void parseTest() {
		// tableCommentParser.parseComment();
		TableCommentDto dto = tableCommentParser.getCommentAndContentsByTableId("DT_1IN1502").join();
		Assertions.assertEquals(
			"행정구역별(읍면동) 여자 내국인-남자 외국인-계 일반가구 주택_다세대주택 내국인-계 내국인-여자 집단가구 외국인-남자 주택_단독주택 총인구 주택_연립주택 남자 주택_아파트 주택_계 주택_비주거용 건물 내 주택 주택 이외의 거처_계 외국인-여자 가구-계 외국인가구 홍제3동 고등동 법전면 명호면 창녕군 방학3동 중계4동 백석1동 옥포2동 진북면 수내2동 서현2동 의정부2동 장암동 안양1동 안양4동 비산1동 관양1동 부천동 원미구 중2동 중4동 역곡3동 성곡동 하안1동 중앙동..."
				+ " "
				+ "주1) 총인구(외국인 포함) 주2) 가구형태(유형) - 일반가구(가구구분 항목 ①~④, " +
				"일반가구 내 외국인도 포함, 가구유형 1)   가족으로 이루어진 가구   가족과 5인 이하의" +
				" 남남이 함께 사는 가구   1인가구   가족이 아닌 남남끼리 함께 사는 5인 이하의 가구 - 집단가구(가구유형 2)   비친족 6인" +
				" 이상 가구 : 가족이 아닌 남남끼리 함께 사는 6인 이상의 가구(가구구분 항목 ⑤)   집단시설가구 : 기숙사나 노인요양시설" +
				" 보육원 등 사회시설에 집단으로 살고 있는 가구(가구유형 4)" +
				" - 외국인가구 : 외국인으로만 구성된 가구(가구유형 3) 주3) 주택 이외의 거처에는 오피스텔, 호텔・여관 등 숙박업소의 객실," +
				" 기숙사 및 특수사회시설, 판잣집・비닐하우스 등이 있음 ② 내국인(귀화) → 내국인(귀화ㆍ인지) ③ 비혈연가구 → 비친족가구 " +
				"④ 비거주용 건물 내 주택 → 비주거용 건물 내 주택 ⑤ 빈집 → 미거주 주택(빈집)  3) (시도 및 시군구의 법정동/행정동 변경)" +
				" - (강원도→강원특별자치도) : 행정동코드(변동없음) - (경상북도 군위군→대구광역시 군위군) : 행정동코드(37510→22520) 4)" +
				" (행정구역분류코드 변경) 구 코드(7자리)에서 개정 코드(8자리)로 변경 - 과거 시점(2015~2022년) 자료도 소급 적용 주4)" +
				" 개인정보 보호와 노출 위험성을 최소화하기 위하여 5미만 자료는 X로 표기함(이하 동일) 주5) 끝자리가 0, 5년인 해(2015," +
				" 2020)는 읍면동, 그 외 연도는 시군구 단위 공표 ※ 2023년 자료 주요변경사항 1) (공표범위 확대) 연령 및 성별 " +
				"인구(DT_1IN1503) 통계표 시군구에서 읍면동으로 공표 확대 2) (용어 정비) ① 귀화자 → 귀화자 등"
			,
			dto.comment() + " " + dto.content());
	}

	@Test
	public void parseTest2() {
		List<CompletableFuture> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(tableCommentParser.asyncDummy(i));
		}
		CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
		for (int i = 0; i < 10; i++) {
			Assertions.assertEquals(
				i,
				list.get(i).join()
			);
		}
	}
}
