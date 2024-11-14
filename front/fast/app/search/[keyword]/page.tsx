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
    const [expandedSection, setExpandedSection] = useState<string | null>(null);

  const toggleExpand = (section: string) => {
    setExpandedSection(expandedSection === section ? null : section);
  };

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

    const searchResults = [
        { title: "1사망원인(104항목)/성/교육정도별 (15~64세) 사망자수", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "2사망원인(104항목)/성/시도별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "3사망원인(104항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "4사망원인(237항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1995~2023" },
        { title: "5시군구/사망원인(50항목)/성/사망자수, 사망률, 연령표준화 사망률(1998~)", source: "통계청, 사망원인통계, 1998~2023" },
        { title: "6사망원인(104항목)/성/교육정도별 (15~64세) 사망자수", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "7사망원인(104항목)/성/시도별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "8사망원인(104항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "9사망원인(237항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1995~2023" },
        { title: "0시군구/사망원인(50항목)/성/사망자수, 사망률, 연령표준화 사망률(1998~)", source: "통계청, 사망원인통계, 1998~2023" },
        { title: "1사망원인(104항목)/성/교육정도별 (15~64세) 사망자수", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "2사망원인(104항목)/성/시도별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "3사망원인(104항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "4사망원인(237항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1995~2023" },
        { title: "5시군구/사망원인(50항목)/성/사망자수, 사망률, 연령표준화 사망률(1998~)", source: "통계청, 사망원인통계, 1998~2023" },
        { title: "6사망원인(104항목)/성/교육정도별 (15~64세) 사망자수", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "7사망원인(104항목)/성/시도별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "8사망원인(104항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1983~2023" },
        { title: "9사망원인(237항목)/성/연령(5세)별 사망자수, 사망률", source: "통계청, 사망원인통계, 1995~2023" },
        { title: "0시군구/사망원인(50항목)/성/사망자수, 사망률, 연령표준화 사망률(1998~)", source: "통계청, 사망원인통계, 1998~2023" },
        // ... 다른 데이터 추가
    ];

    const categories = [
        "가스사고통계(2)",
        "강원도강릉시기본통계(1)",
        "강원도고성군기본통계(1)",
        "강원도동해시기본통계(1)",
        "강원도삼척시기본통계(1)",
        "강원도양구군기본통계(1)",
        "강원도양양군기본통계(1)",
        "강원도영월군기본통계(1)",
        "강원도원주시기본통계(1)",
        "강원도태백시기본통계(1)",
        "강원도고성군기본통계(1)",
        "강원도동해시기본통계(1)",
        "강원도삼척시기본통계(1)",
        "강원도양구군기본통계(1)",
        "강원도양양군기본통계(1)",
        "강원도영월군기본통계(1)",
        "강원도원주시기본통계(1)",
        "강원도태백시기본통계(1)",
        // 추가 항목...
    ];
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
          <div className="accordion-item">
            <button className="accordion-header" onClick={() => toggleExpand("주제별")}>
              주제별(355)
            </button>
            <div className={`accordion-content ${expandedSection === "주제별" ? "expanded" : ""}`}>
              <ul className="category-list">
                {categories.map((item, index) => (
                  <li key={index} className="category-item">
                    {item}
                  </li>
                ))}
              </ul>
            </div>
          </div>
          <div className="accordion-item">
            <button className="accordion-header" onClick={() => toggleExpand("통계별")}>
              통계별(355)
            </button>
            <div className={`accordion-content ${expandedSection === "통계별" ? "expanded" : ""}`}>
              <ul className="category-list">
                {categories.map((item, index) => (
                  <li key={index} className="category-item">
                    {item}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
        <div className="right">
          {searchResults.map((result, index) => (
            <div key={index} className="search-result">
              <div className="search-title">{result.title}</div>
              <div className="search-source">{result.source}</div>
            </div>
          ))}
          {/* 여기는 좌와 우만 남기고 데이터 들어오면 바꿀 것 */}
        </div>
      </div>
    </>
  );
}