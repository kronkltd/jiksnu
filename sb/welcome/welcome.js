import './welcome.css';

let module = angular.module('welcome', []);

module.component('sbWelcome', {
  template: `<div class="welcome__card">
                Hello there and welcome to SB!<br/>
                This demo component we build for you with Angular and SB.<br/>
                You can change it <span>'sb/stories/welcome.js'</span> file.<br/>
                For more information visit <a href="https://github.com/ui-storybook/sb" target="_blank">this page</a><br/>
                And happy codding! 
            </div>`
});

export default module;