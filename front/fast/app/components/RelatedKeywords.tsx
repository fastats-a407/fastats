"use client";

import { useRouter } from "next/navigation";

interface RelatedKeywordsProps {
  keywords: string[];
}

export default function RelatedKeywords({ keywords }: RelatedKeywordsProps) {
  const router = useRouter();

  const handleKeywordClick = (item: string) => {
    router.push(`/search/${item}`);
  };

  return (
    <div className="search-relate">
      <div className="related-block">
        <div className="related-bold">연관 검색어</div>
        <div className="related-normal">
          {keywords.slice(0, 4).map((item, index) => (
            <div
              key={index}
              className="related-item"
              onClick={() => handleKeywordClick(item)}
              style={{ cursor: "pointer" }}
            >
              {item}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}