//
//  WebViewController.h
//  Cordova WebView Plugin
//
//  Created by Marcus Koehler on 27.10.14.
//
//
//

#import <UIKit/UIKit.h>

@interface WebViewController : UIViewController
{
    UIWebView *webView;
    UIActivityIndicatorView *ai;
    NSDictionary *options;
    
    UIButton *forwardButton;
    UIButton *backButton;
    UIButton *refreshButton;
}

- (id)initWithOptions: (NSDictionary*) _options;

@end
