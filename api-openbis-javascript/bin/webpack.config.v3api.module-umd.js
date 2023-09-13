/* eslint-disable */
const Webpack = require('webpack')
const path = require('path')

module.exports = {
  entry: './temp/webpack.config.v3api.entry.js',
  output: {
    path: __dirname + '/temp',
    filename: 'openbis.umd.js',
    libraryTarget: 'umd',
    libraryExport: 'default',
    library: 'openbis'
  },


  mode: 'development',
  devtool: 'source-map',

  module: {
    rules: []
  },

  resolve: {
    alias: {
      'as/dto': path.resolve(__dirname, '../src/v3/as/dto/'),
      'dss/dto': path.resolve(__dirname, '../src/v3/dss/dto/'),
      jquery: path.resolve(__dirname, '../src/v3/lib/jquery/js/jquery.js'),
      moment: path.resolve(__dirname, '../src/v3/lib/moment/js/moment.js'),
      stjs: path.resolve(__dirname, '../src/v3/lib/stjs/js/stjs.js'),
      underscore: path.resolve(
        __dirname,
        '../src/v3/lib/underscore/js/underscore.js'
      ),
      'util/DateFormat': path.resolve(__dirname, '../src/v3/util/DateFormat.js'),
      'util/Exceptions': path.resolve(__dirname, '../src/v3/util/Exceptions.js'),
      'util/Json': path.resolve(__dirname, '../src/v3/util/Json.js')
    },
    fallback: {}
  },

  plugins: [],

  externals: {}
}
