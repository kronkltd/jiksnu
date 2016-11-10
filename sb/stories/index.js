import 'ui-storybook/sb';
import 'ui-storybook/stories';

// Hot reload support
if (module.hot) {
    module.hot.accept();
    window.sb.reload();
}

// Write your stories here

let overview = sb.section('Welcome section');
overview.story('SB demo component')
  .add('Hello text', '<sb-welcome></sb-welcome>');
