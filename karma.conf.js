// Karma configuration
// Generated on Wed Oct 30 2013 09:32:06 GMT+1100 (EST)

module.exports = function(config) {
  var vendorBase = "target/resources/public/vendor";

  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '',

    // frameworks to use
    frameworks: [
//    'angular',
    'jasmine'
    ],

    // list of files / patterns to load in the browser
    files: [
      "node_modules/es6-promise/dist/es6-promise.js",
      vendorBase + "/jquery/dist/jquery.min.js",
      vendorBase + "/angular/angular.min.js",
      vendorBase + "/angular-datatables/dist/angular-datatables.min.js",
      vendorBase + "/highlightjs/highlight.pack.js",
      vendorBase + "/moment/min/moment.min.js",
      vendorBase + "/underscore/underscore-min.js",
      vendorBase + "/showdown/src/showdown.js",
      vendorBase + "/datatables/media/js/jquery.dataTables.min.js",
      vendorBase + "/angularjs-geolocation/dist/angularjs-geolocation.min.js",
      vendorBase + "/angular-busy/dist/angular-busy.min.js",
      vendorBase + "/js-data/dist/js-data.min.js",
      vendorBase + "/js-data-angular/dist/js-data-angular.min.js",
      vendorBase + "/angular-file-upload/dist/angular-file-upload.min.js",
      vendorBase + "/angular-moment/angular-moment.js",
      vendorBase + "/angular-cfp-hotkeys/build/hotkeys.min.js",
      vendorBase + "/angular-highlightjs/angular-highlightjs.js",
      vendorBase + "/angular-markdown-directive/markdown.js",
      vendorBase + "/angular-sanitize/angular-sanitize.min.js",
      vendorBase + "/angular-bootstrap/ui-bootstrap-tpls.min.js",
      vendorBase + "/angular-ui-notification/dist/angular-ui-notification.min.js",
      vendorBase + "/angular-ui-router/release/angular-ui-router.min.js",
      vendorBase + "/angular-ui-select/dist/select.min.js",
      vendorBase + "/angular-validator/dist/angular-validator.min.js",
      vendorBase + "/angular-websocket/angular-websocket.min.js",
      vendorBase + "/angular-mocks/angular-mocks.js",
      "target/karma-test.js"
    ],

    // list of files to exclude
    exclude: [],

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', "html",
                // "coverage",
                "junit", 'notify-send'],

    junitReporter: {
      outputDir: "target/karma"
    },

    // preprocessors: {
    //     "target/karma-test.js": ["coverage"]
    // },

    // coverageReporter: {
    //     type: "html",
    //     dir: "target/coverage/"
    // },

    autoWatchBatchDelay: 2000,

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
    browsers: [
    // 'PhantomJS'
    ],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
