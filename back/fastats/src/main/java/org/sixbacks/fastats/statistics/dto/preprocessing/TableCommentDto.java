package org.sixbacks.fastats.statistics.dto.preprocessing;

// JAVA 16에서 도입된 레코드 클래스
// 기본적으로 불변 클래스를 만들기 위함.
//  게터, 생성자, equals(), hashCode(), toString() 메서드를 기본으로 제공
public record TableCommentDto(String comment, String content) {
}
