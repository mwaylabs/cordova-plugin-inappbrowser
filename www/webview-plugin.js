'use strict';
/*globals require, module*/
var exec = require('cordova/exec');
var cordova = require('cordova');

var webView = {
  openWebView: function (successCallback, errorCallback, options) {
    exec(successCallback, errorCallback, 'WebViewPlugin', 'openWebView', [options]);
  }
};

module.exports = webView;
