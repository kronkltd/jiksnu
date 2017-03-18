var config = require('cukefarm').config;

config.specs = 'features/*.feature';
config.baseUrl = 'http://jiksnu-integration.docker/';
// config.capabilities.browserName = 'chrome';
config.capabilities.browserName = 'phantomjs';
config.capabilities['phantomjs.binary.path'] = require('phantomjs-prebuilt').path;
config.cucumberOpts.format = "pretty";
// config.cucumberOpts.format = "summary";
config.cucumberOpts.require.push('target/protractor-tests.js');

// config.seleniumAddress = 'http://webdriver:24444/wd/hub';

config.plugins = [];
// config.plugins.push(
//   {package: 'protractor-console',
//    logLevels: ['debug', 'info', 'warning', 'severe', 'log']});

exports.config = config;
