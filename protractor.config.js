var config = require('cukefarm').config;

config.specs = 'features/*.feature';
config.capabilities.browserName = 'chrome';
config.cucumberOpts.require.push('target/protractor-tests.js');

config.seleniumAddress = 'http://localhost:4444/wd/hub';

exports.config = config;

// exports.config = {
//   // Uncomment this line if running with a separate webdriver server
//   seleniumAddress: "http://localhost:4444/wd/hub",
//   capabilities: {
//     browserName: 'phantomjs',
//     version: '',
//     platform: 'ANY'
//   },
//   framework: 'custom',
//   frameworkPath: require.resolve('protractor-cucumber-framework'),
//   baseUrl: 'http://localhost:8080/',
//   allScriptsTimeout: 11000,
//   getPageTimeout: 10000,
//   specs: [
//     'features/*.feature'
//   ],
//   cucumberOpts: {
//     require: 'target/protractor-tests.js',
//     format: 'summary'
//   }
// };
