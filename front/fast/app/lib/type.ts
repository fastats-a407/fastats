// type.ts

// 1. 통계 타입 정의
export interface StatSurveyInfo {
    orgName: string;
    statTitle: string;
    statLink: string;
}

// 2. 통계표 타입 정의
export interface SurveyData {
    title: string;
    statSurveyInfo: StatSurveyInfo;
    collStartDate: string; // 날짜 형식 문자열 (YYYYMM)
    collEndDate: string;   // 날짜 형식 문자열 (YYYYMM)
    tableLink: string;
}

// 3. 자동완성 타입 정의
export interface SuggestionKeyword {
    id: string;
    keyword: string;
}


// 4. 통합 검색용
export interface SearchParams {
    keyword: string;
    page: number;
    size: number;
    ctg: string;
    ctgContent: string;

}

// 5. 통합 검색 결과
export interface SearchResponse {
    content: SurveyData[];
    size: number;
    totalPages: number;
}

// 6. 카테고리 검색 결과
export interface SearchCategoryResponse {
    byTheme: SearchCategory[];
    bySurvey: SearchCategory[];
}

export interface SearchCategory {
    name: string;
    count: number;
}


export interface ApiResponse {
    code: number;
    message: string;
    data: any;
}

