import {debounce} from '../../index';

export default function rangSizeInput({minEl, maxEl, table}) {

    let ltValue;
    let gtValue;

    const commit = () => {
        const clauses = [];
        if (ltValue) {
            clauses.push({value: ltValue, operator: 'lte', type: 'number'});
        }
        if (gtValue) {
            clauses.push({value: gtValue, operator: 'gte', type: 'number'});
        }
        table.filter({
            size: clauses
        });
    };

    minEl.addEventListener('input', debounce((ev) => {
        gtValue = minEl.value;
        commit();
    }, 400));

    maxEl.addEventListener('input', debounce((ev) => {
        ltValue = maxEl.value;
        commit();
    }, 400));
}
