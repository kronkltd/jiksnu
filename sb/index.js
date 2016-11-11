import * as angular from 'angular';
import 'ui-storybook/helpers/ng';

// Demo module. Remove this.
import welcome from './welcome/welcome';
import '../target/resources/public/cljs-none/goog/base.js';
import '../target/resources/public/cljs-none/jiksnu.js';

// Support for hot module reload
// Remove this to turn off auto reload
if (module.hot) {
  module.hot.accept();
}

// Import your app here and then add it to the module below
const mainModule = angular.module('sb', [
  'helper',
  'welcome'
  // ,
  // 'jiksnu'
]);

// If we have old app remove it
let oldApp = document.getElementsByTagName('preview-helper')[0];

if (oldApp) {
  oldApp.remove();
}

let preview = document.createElement('preview-helper');
document.body.appendChild(preview);
angular.element(preview)
  .ready(() => {
    angular.bootstrap(preview, [mainModule.name], { strictDi: false });
  });
