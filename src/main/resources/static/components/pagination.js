import {paginationDirective} from 'smart-table-core';

export default function paginationComponent({table, el}) {
    const previousButton = document.createElement('button');
    previousButton.innerHTML = 'Previous';
    const nextButton = document.createElement('button');
    nextButton.innerHTML = 'Next';
    const pageSpan = document.createElement('span');
    pageSpan.innerHTML = '- page 1 -';
    const comp = paginationDirective({table});

    comp.onSummaryChange(({page}) => {
        previousButton.disabled = !comp.isPreviousPageEnabled();
        nextButton.disabled = !comp.isNextPageEnabled();
        pageSpan.innerHTML = `- ${page} -`;
    });

    previousButton.addEventListener('click', () => comp.selectPreviousPage());
    nextButton.addEventListener('click', () => comp.selectNextPage());

    el.appendChild(previousButton);
    el.appendChild(pageSpan);
    el.appendChild(nextButton);

    return comp;
}
