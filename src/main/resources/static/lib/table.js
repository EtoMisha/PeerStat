import {loadingIndicator as loading} from './loadingIndicator';
import {sort} from './sort';
import {filter} from './filters';
import {search as searchInput} from './search';

export const table = ({el, table}) => {
    const bootDirective = (factory, selector) => Array.from(el.querySelectorAll(selector)).forEach(el => factory({
        el,
        table
    }));
    // boot
    bootDirective(sort, '[data-st-sort]');
    bootDirective(loading, '[data-st-loading-indicator]');
    bootDirective(searchInput, '[data-st-search]');
    bootDirective(filter, '[data-st-filter]');

    return table;
};
