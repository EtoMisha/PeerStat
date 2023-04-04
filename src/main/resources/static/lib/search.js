import {searchDirective} from 'smart-table-core';
import {debounce} from './helpers';

export const search = ({el, table, delay = 400, conf = {}}) => {
    const scope = conf.scope || (el.getAttribute('data-st-search') || '')
        .split(',')
        .map(s => s.trim());
    const flags = conf.flags || el.getAttribute('data-st-search-flags') || '';
    const component = searchDirective({table, scope});
    const eventListener = debounce(() => {
        component.search(el.value, {flags});
    }, delay);
    el.addEventListener('input', eventListener);
};
