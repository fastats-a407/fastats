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

export interface ApiResponse{
    code : number;
    message : string;
    data : any;
}

