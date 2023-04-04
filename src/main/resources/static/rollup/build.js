const node = require('rollup-plugin-node-resolve');
const pkg = require('../package.json');

const capitalize = (string) => [...string].reduce((acc, curr) => acc.length ? acc + curr : curr.toUpperCase(), '');
const {main, name: fullName} = pkg;
const [first, ...rest] = fullName.split('-');

const name = [first, rest.map(capitalize).join('')].join('');


module.exports = {
    external: [
        'smart-table-core',
    ],
    input: 'index.js',
    output: {
        file: main,
        format: 'umd',
        sourcemap: true,
        name
    },
    plugins: [node()],
};
