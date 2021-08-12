({
  baseUrl: '../../',
  include: [
    //
    __FILES__
    //
  ],
  paths: {
    jquery: 'lib/jquery/js/jquery',
    stjs: 'lib/stjs/js/stjs',
    underscore: 'lib/underscore/js/underscore',
    moment: 'lib/moment/js/moment'
  },
  shim: {
    stjs: {
      exports: 'stjs',
      deps: ['underscore']
    },
    underscore: {
      exports: '_'
    }
  },
  out: 'openbis.bundle.js',
  optimize: 'none'
})
