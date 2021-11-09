/* eslint-disable */
const path = require('path')

module.exports = {
  entry: './src/js/components/common/grid/index.js',
  output: {
    path: __dirname + '/build/grid/js',
    filename: 'Grid.js',
    libraryTarget: 'var',
    library: 'NgUiGrid'
  },

  mode: 'development',
  devtool: 'source-map',

  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader'
        }
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

  externals: {
    react: 'React',
    'react-dom': 'ReactDOM'
  }
}
