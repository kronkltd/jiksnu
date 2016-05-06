var config = require('cukefarm').config;

config.specs = 'features/*.feature';
config.baseUrl = 'http://jiksnu-integration.docker/';
config.capabilities.browserName = 'chrome';
// config.capabilities.browserName = 'phantomjs';
config.cucumberOpts.format = "pretty";
// config.cucumberOpts.format = "summary";
config.cucumberOpts.require.push('target/protractor-tests.js');

config.seleniumAddress = 'http://selenium:24444/wd/hub';

config.plugins = [({package: 'protractor-console',
                    logLevels: ['debug', 'info', 'warning', 'severe', 'log']})];

exports.config = config;
