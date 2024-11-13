'use client'

import { useParams, useRouter } from "next/navigation";
import Header from "@/app/components/Header";
import RelatedKeywords from "@/app/components/RelatedKeywords";
import { useEffect, useState } from "react";
import { fetchRelatedKeywords, fetchStats } from "@/app/lib/Search";
import { SearchParams, SurveyData } from "@/app/lib/type";

export default function KeywordPage() {
  const params = useParams();
  // const keyword = params.keyword;
  const keyword = Array.isArray(params.keyword) ? params.keyword[0] : params.keyword || "";
  const decodedKeyword = keyword ? decodeURIComponent(keyword) : "";

  const [totalPages, setTotalPages] = useState(0);
  const [curPage, setCurPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [statistics, setStatistics] = useState<SurveyData[]>([])
  const [keywords, setKeywords] = useState<string[]>([])


  useEffect(() => {
    const newSearchKeyword: SearchParams = {
      page: curPage,
      keyword: decodedKeyword,
      size: pageSize,
      ctg: "",
      ctgContent: "",
    }
    fetchStats(newSearchKeyword)
      .then((result) => {
        setTotalPages(result.totalPages)
        setPageSize(result.size)
        setStatistics(result.content)
        console.log(result.content)
      })
      .catch((err) => {
        console.log("검색 실패 ", err)
      });
    fetchRelatedKeywords(decodedKeyword)
      .then((result) => {
        setKeywords(result)
      })
  }, [params.keyword])

  return (
    <>
      <Header keyword={decodedKeyword} />
      <RelatedKeywords keywords={keywords} />
      <div className="result-count"><b>"{decodedKeyword}"</b>에 대한 검색 결과는 <b>00,000건</b>입니다.</div>
      <div className="result-body">
        <div className="upper-bar">
          <div className="select-stats">
            국내 통계
          </div>
          <div className="select-opt">
            <div>
              <select name="정렬순서" defaultValue={1}>
                <option value="1">정확도순</option>
                <option value="2">최신순</option>
              </select>
            </div>
            <div>
              <select name="통계" defaultValue={pageSize}>
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="30">30</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <div className="stats-body">
        <div className="left">

        </div>
        <div className="right">

        </div>
      </div>
    </>
  );
}