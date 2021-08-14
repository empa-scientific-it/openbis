var require = (function () {
  var getBaseUrl = function () {
    // To find where the V3 API has been loaded from we scan the script tags until finding this file
    // This way is possible to load the V3 API from a separate domain
    var scripts = document.getElementsByTagName('script')
    var baseUrl = null
    for (var sIdx = 0; sIdx < scripts.length; sIdx++) {
      var src = scripts[sIdx].src
      var substringEnd = -1
      if ((substringEnd = src.indexOf('/api/v3/config.bundle.js')) > -1) {
        baseUrl = src.substring(0, substringEnd + '/api/v3'.length)
      }
    }
    return baseUrl
  }

  return {
    baseUrl: getBaseUrl(),
    bundles: {
      'openbis.bundle': [
        'jquery',
        'stjs',
        'underscore',
        'moment',
        'openbis',
        //
        __FILES__
        //
      ]
    }
  }
})()
