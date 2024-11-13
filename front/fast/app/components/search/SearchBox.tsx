'use client'
import React from 'react'

interface SearchBoxProps {
    value: string;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onFocus: React.FocusEventHandler<HTMLInputElement>;
    onBlur: React.FocusEventHandler<HTMLInputElement>;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
}
export default function SearchBox({ value, onChange, onFocus, onBlur, onSubmit}: SearchBoxProps) {
    return (
        <form role='search' style={{ margin: 0 }} onSubmit={onSubmit}>
            <input
                type="search"
                placeholder="키워드를 입력하세요."
                value={value}
                onChange={onChange}
                onFocus={onFocus}
                onBlur={onBlur}
            />
            <input type="submit" value="검색" />
        </form>
    );
}
