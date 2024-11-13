package org.sixbacks.fastats.statistics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithSearchCriteria {
	String keyword() default "일반가구";

	int page() default 0;

	int size() default 10;

	String ctg() default "sectorName";

	String ctgContent() default "인구";
}