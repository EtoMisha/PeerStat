import {filterDirective} from 'smart-table-core';
import {debounce} from './helpers';

export const filter = ({table, el, delay = 400, conf = {}}) => {
    const pointer = conf.pointer || el.getAttribute('data-st-filter');
    const operator = conf.operator || el.getAttribute('data-st-filter-operator') || 'includes';
    const elType = el.hasAttribute('type') ? el.getAttribute('type') : 'string';
    let type = conf.type || el.getAttribute('data-st-filter-type');
    if (!type) {
        type = ['date', 'number'].includes(elType) ? elType : 'string';
    }
    const component = filterDirective({table, pointer, type, operator});
    const eventListener = debounce(ev => component.filter(el.value), delay);
    el.addEventListener('input', eventListener);
    if (el.tagName === 'SELECT') {
        el.addEventListener('change', eventListener);
    }
    return component;
};
