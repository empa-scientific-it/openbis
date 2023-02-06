/* eslint-disable-next-line no-undef */
module.exports = function (api) {
  api.cache(true)

  const presets = ['@babel/preset-env', '@babel/preset-react']

  const plugins = [
    '@babel/plugin-transform-runtime',
    '@babel/plugin-proposal-object-rest-spread',
    '@babel/plugin-proposal-class-properties',
    'babel-plugin-transform-amd-to-commonjs'
  ]

  return {
    presets,
    plugins,
    sourceType: 'unambiguous'
  }
}
