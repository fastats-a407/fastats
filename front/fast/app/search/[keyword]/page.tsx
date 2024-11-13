"use client";

import { useParams } from "next/navigation";
import Header from "@/app/components/Header";
import RelatedKeywords from "@/app/components/RelatedKeywords";

export default function KeywordPage() {
  const params = useParams();
  // const keyword = params.keyword;
  const keyword = Array.isArray(params.keyword) ? params.keyword[0] : params.keyword || "";
  const relatedKeywords = ["keyword1", "keyword2", "keyword3", "keyword4", "keyword5", "keyword6"];
  const decodedKeyword = keyword ? decodeURIComponent(keyword) : "";
  
  return (
    <>
      <Header keyword={decodedKeyword} />
      <RelatedKeywords keywords={relatedKeywords} />
      <div className="result-count"><b>"{decodedKeyword}"</b>에 대한 검색 결과는 <b>00,000건</b>입니다.</div>
      <div className="result-body">
        <div className="upper-bar">
          <div className="select-stats">
            국내 통계
          </div>
          <div className="select-opt">
            <div>
              <select name="정렬순서">
                <option value="1" selected>정확도순</option>
                <option value="2">최신순</option>
              </select>
            </div>
            <div>
              <select name="통계">
                <option value="10">10</option>
                <option value="20" selected>20</option>
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