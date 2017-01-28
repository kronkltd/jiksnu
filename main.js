window.jQuery = require('jquery');
window.$ = window.jQuery;

var requires = {
  angular: require('angular'),
  angularClipboard: require('angular-clipboard'),
  angularFileUpload: require('angular-file-upload'),
  angularGeolocation: require('angularjs-geolocation'),
  angularHighlightjs: require('angular-highlightjs'),
  angularHotkeys: require('angular-hotkeys'),
  angularMarkdownDirective: require('angular-markdown-directive'),
  angularMaterial: require('angular-material'),
  angularMaterialIcons: require('angular-material-icons'),
  angularMoment: require('angular-moment'),
  angularSanitize: require('angular-sanitize'),
  angularUiRouter: require('angular-ui-router'),
  angularWebsocket: require('angular-websocket'),
  highlight: require('highlight.js'),
  lfNgMdFileInput: require('lf-ng-md-file-input'),
  jquery: require('jquery'),
  jsDataAngular: require('js-data-angular'),
  uiSelect: require('ui-select')
};

// These are replaced via envify.
window.sentryDSNClient = process.env.JIKSNU_SENTRY_DSN_CLIENT;

if (typeof window.sentryDSNClient != undefined) {
  requires.raven = window.Raven = require('raven-js');
  window.Raven.Plugins = {};
  requires.ravenAngular = window.Raven.Plugins.Angular = require('raven-js/dist/plugins/angular.js');
}

module.exports = requires;
