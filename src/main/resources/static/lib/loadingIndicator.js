import {workingIndicatorDirective} from 'smart-table-core';

export const loadingIndicator = ({table, el}) => {
    const component = workingIndicatorDirective({table});
    component.onExecutionChange(function ({working}) {
        el.classList.remove('st-working');
        if (working === true) {
            el.classList.add('st-working');
        }
    });
    return component;
};
