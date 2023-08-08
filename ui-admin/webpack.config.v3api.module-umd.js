/* eslint-disable */
const Webpack = require('webpack')
const path = require('path')

module.exports = {
  entry: './webpack.config.v3api.entry.js',
  output: {
    path: __dirname + '/build/v3api/js',
    filename: 'V3API.umd.js',
    libraryTarget: 'umd',
    libraryExport: 'default',
    library: 'V3API'
  },


  mode: 'development',
  devtool: 'source-map',

  module: {
    rules: []
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
    fallback: {}
  },

  plugins: [],

  externals: {}
}
