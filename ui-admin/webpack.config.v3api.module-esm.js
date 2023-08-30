/* eslint-disable */
const Webpack = require('webpack')
const path = require('path')

module.exports = {
  entry: './webpack.config.v3api.entry.js',

  experiments: {
    outputModule: true
  },
  output: {
    path: __dirname + '/build/v3api/js',
    filename: 'V3API.esm.js',
    library: {
      type: 'module'
    },
    chunkFormat: 'module',

  },

  mode: 'development',
  devtool: 'source-map',

  module: {
    rules: [
     {
          test: /\.(?:js|mjs|cjs)$/,
          exclude: /node_modules/,
          use: {
            loader: 'babel-loader',
            options: {
              presets: [
                ['@babel/preset-env', { targets: "defaults" }]
              ],
              plugins: ["./removeJQuery.js"]
            }
          }
        }]
  },




  resolve: {
    alias: {
      'as/dto': path.resolve(__dirname, 'srcV3/as/dto/'),
      'dss/dto': path.resolve(__dirname, 'srcV3/dss/dto/'),
      jquery: path.resolve(__dirname, 'srcV3/lib/jquery/js/jquery.js'),
      moment: path.resolve(__dirname, 'srcV3/lib/moment/js/moment.js'),
      stjs: path.resolve(__dirname, 'srcV3/lib/stjs/js/stjs.js'),
      underscore: path.resolve(
        __dirname,
        'srcV3/lib/underscore/js/underscore.js'
      ),
      'util/DateFormat': path.resolve(__dirname, 'srcV3/util/DateFormat.js'),
      'util/Exceptions': path.resolve(__dirname, 'srcV3/util/Exceptions.js'),
      'util/Json': path.resolve(__dirname, 'srcV3/util/Json.js')
    },
    extensions: ['.js', '.d.ts'],
  },

  plugins: [
   new Webpack.IgnorePlugin({resourceRegExp: /^\.\/locale$/,
                              contextRegExp: /moment\/js$/},
                               new Webpack.ProvidePlugin({
                                    $: 'jquery',
                                    jQuery: 'jquery',
                                  }),
)],

  externals: {}
}
