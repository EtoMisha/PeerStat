import {sortDirective} from 'smart-table-core';

export const sort = ({el, table, conf = {}}) => {
    const pointer = conf.pointer || el.getAttribute('data-st-sort');
    const cycle = conf.cycle || el.hasAttribute('data-st-sort-cycle');
    const component = sortDirective({pointer, table, cycle});
    component.onSortToggle(({pointer: currentPointer, direction}) => {
        el.classList.remove('st-sort-asc', 'st-sort-desc');
        if (pointer === currentPointer && direction !== 'none') {
            const className = direction === 'asc' ? 'st-sort-asc' : 'st-sort-desc';
            el.classList.add(className);
        }
    });
    const eventListener = () => component.toggle();
    el.addEventListener('click', eventListener);
    return component;
};
