import {ApiResponse, SuggestionKeyword} from "@/app/lib/type";


export async function fetchAutoComplete(query: string) : Promise<SuggestionKeyword[]> {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL;
    const url = `${baseUrl}/search/autocompletes?keyword=${encodeURIComponent(query)}`;
    const response =  await fetch(url)
    const apiResponse : ApiResponse = await response.json()
    if(apiResponse.code == 200){
        console.info(apiResponse.data)
        let rank = 0;
        return apiResponse.data.map((data: { [x: string]: any; }) => ({
            id : rank++,
            keyword : data["keyword"],
        }))
    }

    return new Promise(resolve => setTimeout(resolve, 500))
}