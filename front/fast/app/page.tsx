'use client'
import styles from "./page.module.css";
import React, { useEffect, useState } from 'react';
import Search from './components/search/Search'

const apiUrl = process.env.NEXT_PUBLIC_API_URL;

export default function Home() {
  const [query, setQuery] = useState('');

  useEffect(() => {
    // sessionID 쿠키가 있는지 확인하는 함수
    const hasSessionCookie = () => {
      return document.cookie.split(";").some((cookie) => cookie.trim().startsWith("sessionID="));
    };

    if (!hasSessionCookie()) {
      fetch(`${apiUrl}/initialize`, {
        method: 'GET',
        credentials: 'include', // 쿠키 자동 포함 설정
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          return response.json();
        })
        .then((data) => {
          console.log("sessionID 쿠키가 생성되었습니다:", data);
        })
        .catch((error) => {
          console.error("Failed to initialize session:", error);
        });
    }
  }, []); // 빈 배열을 전달하여 컴포넌트가 처음 로드될 때만 실행

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <img src="fastats.png" alt="" />
        <Search />
      </main>
      <footer className={styles.footer}>
        <div>
          Fastats
        </div>
      </footer>
    </div>
  );
}
