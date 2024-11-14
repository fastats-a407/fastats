'use client'

import { useParams } from "next/navigation";
import Header from "@/app/components/Header";
import RelatedKeywords from "@/app/components/RelatedKeywords";
import { useEffect, useState } from "react";
import { fetchCategories, fetchRelatedKeywords, fetchStats } from "@/app/lib/Search";
import { SearchCategory, SearchParams, SurveyData } from "@/app/lib/type";

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

  const [categoriesByTheme, setCategoriesByTheme] = useState<SearchCategory[]>([]);
  const [categoriesBySurvey, setCategoriesBySurvey] = useState<SearchCategory[]>([]);
  const [totalByTheme, setTotalByTheme] = useState(0);
  const [totalBySurvey, setTotalBySurvey] = useState(0);
  const [totalResult, setTotalResult] = useState("");


  const toggleExpand = (section: string) => {
    setExpandedSection(expandedSection === section ? null : section);
  };

  const search = (params: SearchParams) => {
    fetchStats(params)
      .then((result) => {
        setTotalPages(result.totalPages)
        setPageSize(result.size)
        setStatistics(result.content)
        console.log(result.content)
      })
      .catch((err) => {
        console.log("검색 실패 ", err)
      });
  }


  useEffect(() => {
    const newSearchKeyword: SearchParams = {
      page: curPage,
      keyword: decodedKeyword,
      size: pageSize,
      ctg: "",
      ctgContent: "",
    }

    search(newSearchKeyword);

    fetchRelatedKeywords(decodedKeyword)
      .then((result) => {
        setKeywords(result)
      });


    fetchCategories(decodedKeyword)
      .then((result) => {
        setCategoriesBySurvey(result.bySurvey);
        setCategoriesByTheme(result.byTheme);
      })
  }, [params.keyword])

  useEffect(() => {
    let num = 0;
    categoriesBySurvey.forEach((category) => {
      num += category.count;
    })
    setTotalBySurvey(num)
  }, [categoriesBySurvey])

  useEffect(() => {
    let num = 0;
    categoriesByTheme.forEach((category) => {
      num += category.count;
    })
    setTotalByTheme(num)
  }, [categoriesByTheme])

  useEffect(() => {
    let result = Intl.NumberFormat('ko-KR').format(totalBySurvey > totalByTheme ? totalBySurvey : totalByTheme)
    setTotalResult(result);
  }, [totalBySurvey, totalByTheme])

  // 페이지 이동

  // 정렬 순서 변경

  // 페이지 크기 변경
  const handleChangePageSize = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newPageSize = parseInt(event.target.value, 10);
    const newSearchKeyword: SearchParams = {
      page: 0,
      keyword: decodedKeyword,
      size: newPageSize,
      ctg: "",
      ctgContent: "",
    }

    search(newSearchKeyword)
    setPageSize(newPageSize);
    setCurPage(0); // 초기 페이지로 이동
  };


  // 주제, 혹은 설문 선택
  const handleClickFilter = (type: string, name: string) => {
    const newSearchKeyword: SearchParams = {
      page: 0,
      keyword: decodedKeyword,
      size: pageSize,
      ctg: type,
      ctgContent: name,
    };
    search(newSearchKeyword);

    setCurPage(0)// 초기 페이지로 이동
  }

  return (
    <>
      <Header keyword={decodedKeyword} />
      <RelatedKeywords keywords={keywords} />
      <div className="result-count"><b>"{decodedKeyword}"</b>에 대한 검색 결과는 <b>{totalResult}건</b>입니다.</div>
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
              <select name="통계" defaultValue={pageSize} onChange={handleChangePageSize}>
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
              주제별({totalByTheme})
            </button>
            <div className={`accordion-content ${expandedSection === "주제별" ? "expanded" : ""}`}>
              <ul className="category-list">
                {categoriesByTheme.map((item, index) => (
                  <li key={index} className="category-item" onClick={() => { handleClickFilter("sectorName", item.name) }}>
                    {item.name}({item.count})
                  </li>
                ))}
              </ul>
            </div>
          </div>
          <div className="accordion-item">
            <button className="accordion-header" onClick={() => toggleExpand("통계별")}>
              통계별({totalBySurvey})
            </button>
            <div className={`accordion-content ${expandedSection === "통계별" ? "expanded" : ""}`}>
              <ul className="category-list">
                {categoriesBySurvey.map((item, index) => (
                  <li key={index} className="category-item" onClick={() => { handleClickFilter("statSurveyName", item.name) }}>
                    {item.name}({item.count})
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
        <div className="right">
          {statistics.map((result, index) => (
            <div key={index} className="search-result">
              {result.tableLink ? (
                <a href={result.tableLink} target="_blank" rel="noopener noreferrer">
                  <div className="search-title">{result.title}</div>
                </a>
              ) : (
                <div className="search-title">{result.title}</div>
              )}

              {result.statSurveyInfo.statLink ? (
                <a href={result.statSurveyInfo.statLink} target="_blank" rel="noopener noreferrer">
                  <div className="search-source">
                    {result.statSurveyInfo.orgName}, {result.statSurveyInfo.statTitle}, {result.collStartDate.substring(0, 4)}~{result.collEndDate.substring(0, 4)}
                    {/* {result.statSurveyInfo.orgName}, {result.statSurveyInfo.statTitle}, {result.collStartDate}~{result.collEndDate} */}

                  </div>
                </a>
              ) : (
                <div className="search-source">
                  {result.statSurveyInfo.orgName}, {result.statSurveyInfo.statTitle}, {result.collStartDate.substring(0, 4)}~{result.collEndDate.substring(0, 4)}
                  {/* {result.statSurveyInfo.orgName}, {result.statSurveyInfo.statTitle}, {result.collStartDate}~{result.collEndDate}                 */}
                </div>
              )}
            </div>
          ))}
          {/* 여기는 좌와 우만 남기고 데이터 들어오면 바꿀 것 */}
        </div>
      </div>
      <div>

      </div>
    </>
  );
}