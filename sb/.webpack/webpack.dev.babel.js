var loaders = require("./loaders");
var CopyWebpackPlugin = require('copy-webpack-plugin');
var webpack = require('webpack');
var path = require('path');

console.log("Dirname: ", __dirname);

module.exports = {
  entry: {
    app: [
      './sb/index.js'
    ],
    stories: [
      './sb/stories'
    ]
  },
  output: {
    filename: '[name].js',
    path: path.join(__dirname, 'sb-dist'),
    publicPath: '/'
  },
  resolve: {
    modulesDirectories: ['node_modules', 'target/resources/public/cljs-none'],
    root: __dirname,
    extensions: ['', '.js', '.json']
  },
  resolveLoader: {
    root: pathUtil.join(__dirname, '../..'),
    modulesDirectories: ["node_modules"]
  },
  // devtool: "source-map",
  plugins: [
    new CopyWebpackPlugin([
      { from: './sb/.static/preview.html', to: 'preview.html' },
      { from: './sb/.static/index.html', to: 'index.html' }
    ])
    // ,
    // new webpack.HotModuleReplacementPlugin()
    , new webpack.ProvidePlugin({
      goog: 'goog/base'
    })
  ],
  module: {
    loaders: loaders
  }
};
