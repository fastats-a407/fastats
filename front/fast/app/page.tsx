'use client'
import Image from "next/image";
import styles from "./page.module.css";
import '@picocss/pico'
import React, { useEffect, useState } from 'react';
import Search from './components/Search'
import axios from "axios";


export default function Home() {
  const [query, setQuery] = useState('');

  useEffect(() => {
    // sessionID 쿠키가 있는지 확인하는 함수
    const hasSessionCookie = () => {
      return document.cookie.split(";").some((cookie) => cookie.trim().startsWith("sessionID="));
    };

    // sessionID 쿠키가 없을 경우에만 서버에 요청
    if (!hasSessionCookie()) {
      axios.get("http://localhost:8080/api/v1/initialize", {
        withCredentials: true, // 쿠키 자동 포함 설정
      })
        .then((response) => {
          console.log("sessionID 쿠키가 생성되었습니다:", response.data);
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
