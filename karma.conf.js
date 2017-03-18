// Karma configuration
// Generated on Wed Oct 30 2013 09:32:06 GMT+1100 (EST)

module.exports = function(config) {
  var vendorBase = "node_modules";

  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '',

    // frameworks to use
    frameworks: [ 'jasmine', 'browserify' ],

    // list of files / patterns to load in the browser
    files: [
      vendorBase + "/es6-promise/dist/es6-promise.js",
      vendorBase + "/jasmine-promise-matchers/dist/jasmine-promise-matchers.js",
      "test.js",
      {pattern: vendorBase + "/**/*", included: false},
      {pattern: "target/karma-cljs/goog/base.js", watched: true},
      {pattern: "target/karma-cljs/karma-test.js", included: true, watched: true},
      {pattern: "target/karma-cljs/**/*", included: false, watched: true},
      {pattern: "main.js", included: false, watched: true},
      "test-runner.js"
    ],

    // list of files to exclude
    exclude: [],

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: [
      // 'progress',
      'live-html',
      'nested',
      "junit",
      'notify-send'
    ],

    junitReporter: {
      outputDir: "target/karma"
    },

    plugins: [
      'karma-browserify',
      // 'karma-coverage',
      'karma-jasmine',                      // Required plugin
      // 'karma-html-detailed-reporter',
      'karma-html-live-reporter',
      'karma-phantomjs-launcher',           // Launches PhantomJS
      'karma-jasmine-html-reporter',
      'karma-junit-reporter',
      'karma-nested-reporter',
      'karma-notify-send-reporter'
    ],

    preprocessors: {
      "test.js": [ 'browserify']
    },

    browserify: {
      debug: true
    },

    htmlLiveReporter: {
      colorScheme: 'jasmine', // light 'jasmine' or dark 'earthborn' scheme
      defaultTab: 'summary', // 'summary' or 'failures': a tab to start with

      // only show one suite and fail log at a time, with keyboard navigation
      focusMode: true
    },

    // coverageReporter: {
    //     type: "html",
    //     dir: "target/coverage/"
    // },

    autoWatchBatchDelay: 5000,

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera (has to be installed with `npm install karma-opera-launcher`)
    // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
    // - PhantomJS
    // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
    browsers: [ 'PhantomJS' ],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
