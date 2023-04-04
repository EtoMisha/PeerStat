const node = require('rollup-plugin-node-resolve');
const {terser} = require('rollup-plugin-terser');

module.exports = {
    input: 'example/index.js',
    output: {
        file: 'example/bundle.js',
        format: 'iife',
        name: 'tableExample',
        sourcemap: true,
    },
    plugins: [
        node({}),
        terser()
    ],
};
