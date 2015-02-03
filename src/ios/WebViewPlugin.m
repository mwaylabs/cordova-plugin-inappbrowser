//
//  WebViewPlugin.m
//  Cordova WebView Plugin
//
//  Created by Marcus Koehler on 27.10.14.
//
//

#import "WebViewPlugin.h"
#import "WebViewController.h"

@implementation WebViewPlugin

- (void)openWebView:(CDVInvokedUrlCommand*)command;
{
     [self.commandDelegate runInBackground:^{
         NSMutableDictionary* options = [command.arguments objectAtIndex:0];

         CDVCommandStatus status = CDVCommandStatus_OK;

         callbackId = command.callbackId;

         if (options != nil)
         {
             dispatch_async(dispatch_get_main_queue(), ^{
                 WebViewController *viewController = [[WebViewController alloc] initWithOptions: options];
                 [self.viewController presentViewController:viewController animated:YES completion:nil];
             });
         }
         else
             status = CDVCommandStatus_MALFORMED_URL_EXCEPTION;

         CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:status messageAsString:nil];
         [self.commandDelegate sendPluginResult:commandResult callbackId:callbackId];
     }];
  }


@end
