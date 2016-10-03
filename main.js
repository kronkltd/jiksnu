window.jQuery = require('jquery');
window.$ = window.jQuery;

window.jQuery.fn.DataTable = require('datatables.net');

var requires = {
  angular: require('angular'),
  angularClipboard: require('angular-clipboard'),
  angularDatatables: require('angular-datatables'),
  angularFileUpload: require('angular-file-upload'),
  angularGeolocation: require('angularjs-geolocation'),
  angularHighlightjs: require('angular-highlightjs'),
  angularHotkeys: require('angular-hotkeys'),
  angularMarkdownDirective: require('angular-markdown-directive'),
  angularMoment: require('angular-moment'),
  angularSanitize: require('angular-sanitize'),
  angularUiRouter: require('angular-ui-router'),
  angularWebsocket: require('angular-websocket'),
  highlight: require('highlight.js'),
  lfNgMdFileInput: require('lf-ng-md-file-input'),
  jquery: require('jquery'),
  jsDataAngular: require('js-data-angular'),
  raven: require('raven-js'),
  ravenAngular: require('raven-js/dist/plugins/angular.js'),
  showdown: require('showdown'),
  uiBootstrap: require('angular-ui-bootstrap'),
  uiNotification: require('angular-ui-notification'),
  uiSelect: require('ui-select'),
  underscore: require('underscore')
};

window.Showdown = requires.showdown;
window.Raven = requires.raven;
window.Raven.Plugins = {};
window.Raven.Plugins.Angular = requires.ravenAngular;
window.requires = requires;

module.exports = requires;
