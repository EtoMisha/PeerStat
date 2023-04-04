export function debounce(fn, delay) {
    let timeoutId;
    return (ev) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(function () {
            fn(ev);
        }, delay);
    };
}
