import {table as tableComponentFactory} from '../index';
import {smartTable as table} from 'smart-table-core';
import row from '../components/row';
import summary from '../components/summary';
import pagination from '../components/pagination';
import rangeSizeInput from '../components/rangeSizeInput';


const el = document.getElementById('table-container');
const tbody = el.querySelector('tbody');
const summaryEl = el.querySelector('[data-st-summary]');

const initialState = {sort: {}, filter: {}, slice: {page: 1, size: 20}, search: {}};
const t = table({data, tableState: initialState});
const tableComponent = tableComponentFactory({el, table: t});

summary({table: t, el: summaryEl});
rangeSizeInput({
    table: t,
    minEl: document.getElementById('min-size'),
    maxEl: document.getElementById('max-size')
});

const paginationContainer = el.querySelector('[data-st-pagination]');
pagination({table: t, el: paginationContainer});

tableComponent.onDisplayChange(displayed => {
    tbody.innerHTML = '';
    for (let r of displayed) {
        const newChild = row((r.value), r.index, t);
        tbody.appendChild(newChild);
    }
});

tableComponent.exec();
