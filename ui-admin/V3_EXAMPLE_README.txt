How to run the example:

- enter srcV3Example folder
- run: node express.js
- open:
    - AMD example: http://localhost:3000/example-amd-module.html
    - ESM example: http://localhost:3000/example-esm-module.html
    - UMD example: http://localhost:3000/example-umd-module.html
- click "Use XXX V3 API" button to trigger V3 API call

How to regenerate bundle:

- run: node webpack.config.v3api.generate.entry.js > webpack.config.v3api.entry.js
- run:
    - for ESM bundle: npm run v3api.esm
    - for UMD bundle: npm run v3api.umd
- new bundle is stored in build/v3api/js folder