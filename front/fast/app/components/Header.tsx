"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

interface HeaderProps {
    keyword: string;
}

export default function Header({ keyword }: HeaderProps) {
    const router = useRouter();
    const [searchTerm, setSearchTerm] = useState(keyword);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지
        if (searchTerm.trim()) {
            router.push(`/search/${searchTerm}`);
        }
    };



    return (
        <header className="search-header">
            <div className="logo-container">
                <img src="/fastats.png" alt="Logo" className="logo-icon" />
            </div>
            <div className="search-container">
                <img src="/search.png" alt="Search Icon" className="search-icon" />
                <form onSubmit={handleSubmit} className="search-form">
                    <input
                        type="text"
                        className="search-input"
                        placeholder={keyword ? `${keyword}` : "Search..."}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </form>
            </div>
            <div></div>
        </header>
    );
}