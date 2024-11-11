'use client'
import { FocusEventHandler, MouseEventHandler, useEffect, useState } from 'react';
import SearchBox from './SearchBox';
import SearchResults from './SearchResults';
import { SuggestionKeyword } from '@/app/components/type'
import { useSearch } from '../lib/Search'
import { fetchCountries } from '../lib/countries';
import { useDebouncedState } from '../lib/useDebouncedState';

export default function Search() {
    const [query, setQuery] = useState<string>('');
    const debouncedQuery = useDebouncedState(query, 1000)
    const [statistics, setStatistics] = useState<SuggestionKeyword[]>([]);
    const [searching, setSearching] = useState<boolean>(false);
    const [isHidden, setIsHidden] = useState<boolean>(true);
    const [liOver, setLiOver] = useState<boolean>(false);

    useEffect(() => {
        setSearching(true);
        fetchCountries(debouncedQuery).then((statistics) => {
            setStatistics(statistics.slice(0, 5))
            setSearching(false)
        })
        // useSearch().then(() => {
        //     setSearching(false)
        //     setStatistics([
        //         { id: "1", keyword: "first" },
        //         { id: "2", keyword: "second" },
        //         { id: "3", keyword: "third" },
        //         { id: "4", keyword: "네번째" },
        //         { id: "5", keyword: "다섯번째" },
        //         { id: "6", keyword: "여섯번째" },
        //     ])

        // });
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
    };
    return (
        <>
            <SearchBox value={query} onFocus={onFocusIn} onBlur={onFocusOut} onChange={(e) => setQuery(e.target.value)} />
            <SearchResults onClick={onAddResultClick} onMouseLeave={onMouseLeave} onMouseOver={onMouseOver} hidden={isHidden} statistics={statistics} searching={searching} />
        </>
    );
}
