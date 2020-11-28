/* eslint-disable-next-line no-undef */
module.exports = {
  testURL: 'http://localhost/#/',
  reporters: [
    'default',
    [
      'jest-junit',
      {
        suiteNameTemplate: '{filename}',
        classNameTemplate: '{filename}',
        titleTemplate: '{title}'
      }
    ]
  ],
  setupFiles: ['<rootDir>/srcTest/js/setupUnderscore.js'],
  setupFilesAfterEnv: [
    '<rootDir>/srcTest/js/setupEnzyme.js',
    '<rootDir>/srcTest/js/setupJestTimeout.js'
  ],
  moduleDirectories: [
    '<rootDir>/src/js',
    '<rootDir>/srcTest/js',
    '<rootDir>/srcV3',
    '<rootDir>/node_modules'
  ],
  moduleNameMapper: {
    jquery: '<rootDir>/srcV3/lib/jquery/js/jquery.js',
    moment: '<rootDir>/srcV3/lib/moment/js/moment.js',
    stjs: '<rootDir>/srcV3/lib/stjs/js/stjs.js',
    underscore: '<rootDir>/srcV3/lib/underscore/js/underscore.js',
    '\\.css$': '<rootDir>/srcTest/js/mockStyles.js',
    'openbis.js': '<rootDir>/srcTest/js/services/openbis.js',
    '^@src/(.*)$': '<rootDir>/src/$1',
    '^@srcTest/(.*)$': '<rootDir>/srcTest/$1',
    '^@srcV3/(.*)$': '<rootDir>/srcV3/$1'
  },
  slowTestThreshold: 30
}
