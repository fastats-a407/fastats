package org.sixbacks.fastats.statistics.annotation;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.sixbacks.fastats.statistics.dto.request.SearchCriteria;

public class SearchCriteriaArgumentResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.getParameter().getType().equals(SearchCriteria.class)
			&& extensionContext.getTestMethod().isPresent()
			&& extensionContext.getTestMethod().get().isAnnotationPresent(WithSearchCriteria.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		WithSearchCriteria annotation = extensionContext.getTestMethod().get().getAnnotation(WithSearchCriteria.class);

		SearchCriteria criteria = SearchCriteria.builder()
			.keyword(annotation.keyword())
			.page(annotation.page())
			.size(annotation.size())
			.ctg(annotation.ctg())
			.ctgContent(annotation.ctgContent())
			.orderType(annotation.orderType())
			.build();

		return criteria;
	}
}
