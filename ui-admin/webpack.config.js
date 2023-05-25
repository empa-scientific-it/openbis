/* eslint-disable */
const Webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const path = require('path')

module.exports = {
  entry: './src/js/index.js',
  output: {
    path: __dirname + '/build/js',
    filename: 'bundle.[fullhash].js'
  },

  mode: 'production',
  devtool: 'source-map',

  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: ['babel-loader']
      },
      {
        test: /\.(css)$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif|ico)$/i,
        type: 'asset'
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000
        }
      }
    ]
  },

  resolve: {
    alias: {
      '@src': path.resolve(__dirname, 'src/'),
      '@srcTest': path.resolve(__dirname, 'srcTest/'),
      '@srcV3': path.resolve(__dirname, 'srcV3/')
    },
    fallback: {
      stream: require.resolve('stream-browserify'),
      buffer: require.resolve('buffer')
    }
  },

  plugins: [
    new HtmlWebpackPlugin({
      inject: 'body',
      filename: './index.html',
      template: './index.html'
    }),
    new Webpack.ProvidePlugin({
      Buffer: ['buffer', 'Buffer']
    }),
    new Webpack.ProvidePlugin({
      process: 'process/browser'
    }),
    new CopyWebpackPlugin({
      patterns: [{ from: 'src/resources' }]
    })
  ]
}
