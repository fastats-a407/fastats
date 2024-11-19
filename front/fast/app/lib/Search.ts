import { ApiResponse, SearchCategoryResponse, SearchParams, SearchResponse, SuggestionKeyword } from "@/app/lib/type";



const baseUrl = process.env.NEXT_PUBLIC_API_URL;
export async function fetchAutoComplete(query: string): Promise<SuggestionKeyword[]> {
    const url = `${baseUrl}/search/autocompletes?keyword=${encodeURIComponent(query)}`;
    const response = await fetch(url)
    const apiResponse: ApiResponse = await response.json()
    if (apiResponse.code == 200) {
        let rank = 0;
        return apiResponse.data.map((data: { [x: string]: any; }) => ({
            id: rank++,
            keyword: data["keyword"],
        }))
    }

    return new Promise(resolve => setTimeout(resolve, 500))
}


export async function fetchStats(params: SearchParams): Promise<SearchResponse> {
    const url = `${baseUrl}/stats?` + new URLSearchParams({
        keyword: params.keyword,
        page: params.page.toString(),
        size: params.size.toString(),
        ctg: params.ctg,
        ctgContent: params.ctgContent,
        orderType: params.orderType,
    }).toString();

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    const apiResponse: ApiResponse = await response.json()
    if (apiResponse.code == 200) {
        return apiResponse.data as SearchResponse;
    }

    return new Promise(resolve => setTimeout(resolve, 500))
}
/**
 * 연관 키워드 검색
 * @param query 검색 키워드
 * @returns 연관 키워드 배열
 */
export async function fetchRelatedKeywords(query: string): Promise<string[]> {
    const url = `${baseUrl}/stats/suggestions?keyword=${encodeURIComponent(query)}`;
    const response = await fetch(url)
    const apiResponse: ApiResponse = await response.json()
    if (apiResponse.code == 200) {
        return apiResponse.data as string[]
    }

    return new Promise(resolve => setTimeout(resolve, 500))

}

/**
 * 카테고리별 검색 결과 수
 * @param keyword 검색 키워드
 * @returns 카테고리 검색 결과
 */
export async function fetchCategories(keyword: string): Promise<SearchCategoryResponse> {
    const url = `${baseUrl}/stats/categories?keyword=${encodeURIComponent(keyword)}`;
    const response = await fetch(url);
    const apiResponse: ApiResponse = await response.json();
    if (apiResponse.code == 200) {
        console.log("카테고리 검색 결과", apiResponse.data)
        return apiResponse.data as SearchCategoryResponse
    }
    return new Promise((resolve) => setTimeout(resolve, 500))
}
