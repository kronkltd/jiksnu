var loaders = require("./loaders");
var CopyWebpackPlugin = require('copy-webpack-plugin');
var webpack = require('webpack');
var path = require('path');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var pathUtil = require('path');

module.exports = {
  entry: {
    app: [
      'webpack/hot/dev-server',
      'webpack-hot-middleware/client?noInfo=true',
      './sb/index.js'
    ],
    stories: [
      'webpack/hot/dev-server',
      'webpack-hot-middleware/client?noInfo=true',
      './sb/stories'
    ]
  },
  output: {
    filename: '[name].js',
    path: path.join(__dirname, 'sb-dist'),
    publicPath: '/'
  },
  resolve: {
    modulesDirectories: ['node_modules'],
    root: __dirname,
    extensions: ['', '.js', '.json'],
    alias: {
      'npm': __dirname + '/../node_modules'
    }
  },
  resolveLoader: {
    root: pathUtil.join(__dirname, 'node_modules'),
    modulesDirectories: ["../node_modules"]
  },
  // devtool: "source-map",
  plugins: [
    new CopyWebpackPlugin([
      { from: './sb/.static/preview.html', to: 'preview.html' },
      { from: './sb/.static/index.html', to: 'index.html' }
    ]),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.ProvidePlugin({
      goog: 'goog/base'
    })
  ],
  closureLoader: {
    paths: [
      __dirname + '../../node_modules/google-closure-library/closure/goog',
      __dirname + '/target/resources/public/cljs-none'
    ],
    es6mode: true,
    watch: true
  },
  module: {
    loaders: loaders
  },
  devServer: {
    contentBase: './build',
    noInfo: true,
    inline: true,
    historyApiFallback: true
  },
  devtool: 'source-map'
};
