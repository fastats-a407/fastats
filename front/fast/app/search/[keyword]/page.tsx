'use client'

import { useParams } from "next/navigation";
import Header from "@/app/components/Header";
import RelatedKeywords from "@/app/components/RelatedKeywords";
import { use, useEffect, useState } from "react";
import { fetchCategories, fetchRelatedKeywords, fetchStats } from "@/app/lib/Search";
import { SearchCategory, SearchParams, SearchResponse, SurveyData } from "@/app/lib/type";

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
  const [ctgType, setCtgType] = useState<string | "">("");
  const [ctgName, setCtgName] = useState<string | "">("");

  const [categoriesByTheme, setCategoriesByTheme] = useState<SearchCategory[]>([]);
  const [categoriesBySurvey, setCategoriesBySurvey] = useState<SearchCategory[]>([]);
  const [totalByTheme, setTotalByTheme] = useState(0);
  const [totalBySurvey, setTotalBySurvey] = useState(0);
  const [totalResult, setTotalResult] = useState("");
  const [orderType, setOrderType] = useState("rel")


  const paginationSize = 10;
  const currentRangeStart = Math.floor(curPage / paginationSize) * paginationSize;
  const currentRangeEnd = Math.min(currentRangeStart + paginationSize, totalPages);

  const toggleExpand = (section: string) => {
    setCtgName("");
    setCtgType("");
    const newSearchKeyword: SearchParams = {
      page: 0,
      keyword: decodedKeyword,
      size: pageSize,
      ctg: "",
      ctgContent: "",
      orderType: orderType,
    };
    search(newSearchKeyword);
    setCurPage(0);

    setExpandedSection(expandedSection === section ? null : section);
  };

  let initial = false;
  const search = (params: SearchParams) => {
    fetchStats(params)
      .then((result: SearchResponse) => {
        setTotalPages(result.totalPages)
        setPageSize(result.size)
        setStatistics(result.content)
        if (initial) {
          let Tresult = Intl.NumberFormat('ko-KR').format(result.totalCounts)
          setTotalResult(Tresult);
          initial = false
        }
      })
      .catch((err) => {
        console.log("검색 실패 ", err)
      });
  }


  useEffect(() => {
    initial = true
    const newSearchKeyword: SearchParams = {
      page: curPage,
      keyword: decodedKeyword,
      size: pageSize,
      ctg: "",
      ctgContent: "",
      orderType: orderType,
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


  // 페이지 이동

  // 정렬 순서 변경

  const handleChangeOrder = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newOrder = event.target.value;
    const newSearchKeyword: SearchParams = {
      page: 0,
      keyword: decodedKeyword,
      size: pageSize,
      ctg: ctgType,
      ctgContent: ctgName,
      orderType: newOrder,
    }
    search(newSearchKeyword);
    setCurPage(0);
    setOrderType(newOrder);
  }


  // 페이지 크기 변경
  const handleChangePageSize = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newPageSize = parseInt(event.target.value, 10);
    const newSearchKeyword: SearchParams = {
      page: 0,
      keyword: decodedKeyword,
      size: newPageSize,
      ctg: ctgType,
      ctgContent: ctgName,
      orderType: orderType,
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
      orderType: orderType,
    };
    search(newSearchKeyword);

    setCtgType(type);
    setCtgName(name);
    setCurPage(0)// 초기 페이지로 이동
  }

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      const newSearchKeyword: SearchParams = {
        page: newPage,
        keyword: decodedKeyword,
        size: pageSize,
        ctg: ctgType,
        ctgContent: ctgName,
        orderType: orderType,
      };

      search(newSearchKeyword);
      setCurPage(newPage);
    }
  };


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
              <select name="정렬순서" defaultValue={orderType} onChange={handleChangeOrder}>
                <option value="rel">정확도순</option>
                <option value="time">최신순</option>
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
                    {result.statSurveyInfo.orgName}, {result.statSurveyInfo.statTitle}
                    {result.collStartDate ? `, ${result.collStartDate.substring(0, 4)}` : ""}
                    {result.collEndDate ? `~${result.collEndDate.substring(0, 4)}` : ""}
                  </div>
                </a>
              ) : (
                <div className="search-source">
                  {result.statSurveyInfo.orgName}, {result.statSurveyInfo.statTitle}
                  {result.collStartDate ? `, ${result.collStartDate.substring(0, 4)}` : ""}
                  {result.collEndDate ? `~${result.collEndDate.substring(0, 4)}` : ""}
                </div>
              )}
            </div>
          ))}
          {/* 여기는 좌와 우만 남기고 데이터 들어오면 바꿀 것 */}
        </div>
      </div>
      <div className="pagenation-space">
        <div className="pagenation">
          <button
            onClick={() => handlePageChange(0)}
            disabled={curPage === 0}
          >
            {"<<"}
          </button>

          <button
            onClick={() => handlePageChange(currentRangeStart - paginationSize)}
            disabled={currentRangeStart === 0}
          >
            {"<"}
          </button>

          {Array.from({ length: currentRangeEnd - currentRangeStart }, (_, i) => {
            const pageNumber = currentRangeStart + i;
            return (
              <button
                key={pageNumber}
                onClick={() => handlePageChange(pageNumber)}
                className={pageNumber === curPage ? "active" : ""}
              >
                {pageNumber + 1}
              </button>
            );
          })}

          <button
            onClick={() => handlePageChange(currentRangeStart + paginationSize)}
            disabled={currentRangeEnd >= totalPages}
          >
            {">"}
          </button>

          <button
            onClick={() => handlePageChange(totalPages - 1)}
            disabled={curPage === totalPages - 1}
          >
            {">>"}
          </button>
        </div>
      </div>
    </>
  );
}