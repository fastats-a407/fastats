'use client'
import Image from "next/image";
import styles from "./page.module.css";
import '@picocss/pico'
import React, { useState } from 'react';
import Search from './components/Search'

export default function Home() {
  const [query, setQuery] = useState('');

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
