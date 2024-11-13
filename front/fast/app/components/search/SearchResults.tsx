'use client'
import React from 'react'
import { SuggestionKeyword } from '../../lib/type'


interface SearchResultsProps {
    statistics: SuggestionKeyword[];
    searching: boolean;
    hidden: boolean;
    onMouseOver: React.MouseEventHandler<HTMLLIElement>;
    onMouseLeave: React.MouseEventHandler<HTMLLIElement>;
    onClick: React.MouseEventHandler<HTMLLIElement>;
}


export default function SearchResults({ statistics, searching, hidden, onMouseOver, onMouseLeave, onClick }: SearchResultsProps) {
    if (hidden) return null;
    return (
        <article aria-busy={searching}>
            {searching ? (
                '잠시만 기다려주세요.'
            ) : (
                <>
                    <ul>
                        {statistics.map(({ id, keyword }) => (
                            <li key={id} onMouseLeave={onMouseLeave} onMouseOver={onMouseOver} onClick={onClick}>
                                {keyword}
                            </li>
                        ))}
                    </ul>
                </>
            )}
        </article>
    );
}
