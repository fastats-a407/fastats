'use client'
import { FocusEventHandler, MouseEventHandler, useEffect, useState } from 'react';
import SearchBox from './SearchBox';
import SearchResults from './SearchResults';
import { SuggestionKeyword } from '@/app/lib/type'
import { fetchAutoComplete, fetchStats } from '../../lib/Search'
import { useDebouncedState } from '../../lib/useDebouncedState';
import { useRouter } from 'next/navigation';


export default function Search() {
    const [query, setQuery] = useState<string>('');
    const debouncedQuery = useDebouncedState(query, 100)
    const [statistics, setStatistics] = useState<SuggestionKeyword[]>([]);
    const [searching, setSearching] = useState<boolean>(false);
    const [isHidden, setIsHidden] = useState<boolean>(true);
    const [liOver, setLiOver] = useState<boolean>(false);
    const router = useRouter();

    useEffect(() => {
        setSearching(true)
    }, [query])


    useEffect(() => {
        setSearching(true);
        fetchAutoComplete(debouncedQuery).then(statistics => {
            setStatistics(statistics);
            setSearching(false);
        })
    }, [debouncedQuery]);

    const onFocusIn: FocusEventHandler<HTMLInputElement> = (e) => {
        setIsHidden(false);
    };
    const onFocusOut: FocusEventHandler<HTMLInputElement> = (e) => {
        if (liOver) return;
        setIsHidden(true);
    };
    const onMouseOver: MouseEventHandler<HTMLLIElement> = (e) => {
        setLiOver(true);
        e.currentTarget.style.background = 'gray';
    }
    const onMouseLeave: MouseEventHandler<HTMLLIElement> = (e) => {
        setLiOver(false);
        e.currentTarget.style.background = 'none';
    }
    const onAddResultClick: MouseEventHandler<HTMLLIElement> = (e) => {
        const { textContent } = e.currentTarget;
        setQuery(textContent || "");
        setIsHidden(true);

        const selectedKeyword = statistics.find(stat => stat.keyword === textContent);
        if (selectedKeyword) {
            router.push(`/search/${encodeURIComponent(selectedKeyword.keyword)}`);
        }
    };
    const handleSearchSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        console.log("검색을 시작합니다:", query);
        router.push(`/search/${query}`);
    };
    return (
        <>
            <SearchBox value={query} onFocus={onFocusIn} onBlur={onFocusOut} onChange={(e) => setQuery(e.target.value)} onSubmit={handleSearchSubmit} />
            <SearchResults onClick={onAddResultClick} onMouseLeave={onMouseLeave} onMouseOver={onMouseOver} hidden={isHidden} statistics={statistics} searching={searching} />
        </>
    );
}
