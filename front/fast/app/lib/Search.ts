import { ApiResponse, SearchParams, SearchResponse, SuggestionKeyword, SurveyData } from "@/app/lib/type";


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
        ctgContent: params.ctgContent
    }).toString();

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    const apiResponse: ApiResponse = await response.json()
    if (apiResponse.code == 200) {
        console.info("검색 결과", apiResponse.data)
        return apiResponse.data as SearchResponse;
    }

    return new Promise(resolve => setTimeout(resolve, 500))
}

export async function fetchRelatedKeywords(query: string): Promise<string[]> {
    const url = `${baseUrl}/stats/suggestions?keyword=${encodeURIComponent(query)}`;
    const response = await fetch(url)
    const apiResponse: ApiResponse = await response.json()
    if (apiResponse.code == 200) {
        let rank = 0;
        return apiResponse.data as string[]
    }

    return new Promise(resolve => setTimeout(resolve, 500))

}

