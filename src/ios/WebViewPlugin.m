//
//  CordovaPushPlugin.m
//  PushLibrary
//
//  Created by Marcus Koehler on 27.01.14.
//  Copyright (c) 2014 M-Way Solutions GmbH. All rights reserved.
//

#import "WebViewPlugin.h"
#import "DocumentViewController.h"

@implementation WebViewPlugin

- (void)openDocument:(CDVInvokedUrlCommand*)command;
{
     [self.commandDelegate runInBackground:^{
         NSMutableDictionary* options = [command.arguments objectAtIndex:0];

         CDVCommandStatus status = CDVCommandStatus_OK;

         if (options != nil)
         {
             dispatch_async(dispatch_get_main_queue(), ^{
                 DocumentViewController *viewController = [[DocumentViewController alloc] initWithUrl:[options objectForKey:@"url"] name: [options objectForKey:@"name"] mimeType:[options objectForKey:@"mimeType"]];
                 [self.viewController presentViewController:viewController animated:YES completion:nil];
             });
         }
         else
             status = CDVCommandStatus_MALFORMED_URL_EXCEPTION;

         CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:status messageAsString:nil];
         [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
     }];
  }

@end
