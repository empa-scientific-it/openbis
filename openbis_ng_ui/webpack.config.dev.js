/* eslint-disable */
const Webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const path = require('path')

module.exports = {
  entry: './src/js/index.js',
  output: {
    path: __dirname + '/build/npm-build/',
    filename: 'bundle.js'
  },

  devServer: {
    host: '0.0.0.0',
    port: 8124,
    inline: true,
    contentBase: './src/js',
    https: false,
    watchOptions: {
      aggregateTimeout: 300,
      poll: 1000
    },
    publicPath: '/ng-ui-path/',
    proxy: {
      '/openbis': {
        target: 'http://localhost:8888',
        pathRewrite: { '^/openbis/resources': '/openbis-test/resources' },
        changeOrigin: true,
        secure: false
      }
    }
  },

  devtool: 'source-map',

  mode: 'development',

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
    }
  },

  plugins: [
    new HtmlWebpackPlugin({
      inject: 'body',
      filename: './index.html',
      template: './index.html'
    }),
    //    new Webpack.WatchIgnorePlugin(['/home/vagrant/openbis/openbis_ng_ui/react/node_modules/', '/home/vagrant/openbis/openbis_ng_ui/react/node/'])
    new Webpack.WatchIgnorePlugin([
      new RegExp('/node_modules/'),
      new RegExp('/node/')
    ])
  ]
}
