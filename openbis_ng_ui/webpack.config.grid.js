/* eslint-disable */
const path = require('path')

module.exports = {
  entry: './src/js/components/common/grid/index.js',
  output: {
    path:
      __dirname +
      '/../openbis_standard_technologies/dist/core-plugins/eln-lims/1/as/webapps/eln-lims/html/react',
    filename: 'Grid.js',
    libraryTarget: 'var',
    library: 'Grid'
  },

  mode: 'development',

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
  }
  /*
  externals: {
    react: 'react',
    'react-dom': 'react-dom'
  }
*/
}
