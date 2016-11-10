var loaders = require("./loaders");
var CopyWebpackPlugin = require('copy-webpack-plugin');
var webpack = require('webpack');
var path = require('path');

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
        root: __dirname,
        extensions: ['', '.js', '.json']
    },
    resolveLoader: {
        modulesDirectories: ["node_modules"]
    },
    // devtool: "source-map",
    plugins: [
        new CopyWebpackPlugin([
            { from: './sb/.static/preview.html', to: 'preview.html' },
            { from: './sb/.static/index.html', to: 'index.html' }
        ]),
        new webpack.HotModuleReplacementPlugin()
    ],
    module: {
        loaders: loaders
    }
};