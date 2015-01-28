//
//  WebViewPlugin.h
//  Cordova WebView Plugin
//
//  Created by Marcus Koehler on 27.10.14.
//
//

#import <Cordova/CDVPlugin.h>

@interface WebViewPlugin : CDVPlugin
{
    NSString *callbackId;
}
- (void)openWebView:(CDVInvokedUrlCommand*)command;
@end
