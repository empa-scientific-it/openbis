How to run the example:

- enter srcV3Example folder
- run: node express.js
- open: http://localhost:3000/ in a browser
- click "Use V3 API" button to trigger V3 API call

How to regenerate V3API.js bundle:

- run: node webpack.config.v3api.generate.entry.js > webpack.config.v3api.entry.js
- run: npm run v3api
- new bundle is stored in build/v3api/js folder