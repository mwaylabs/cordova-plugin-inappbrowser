//
//  PDFViewController.m
//  SEW
//
//  Created by Marcus Koehler on 12.02.14.
//
//

#import "WebViewController.h"

@interface WebViewController (private) <UIWebViewDelegate>
@end

@implementation WebViewController

- (id)initWithUrl: (NSString*) _url name: (NSString*) name mimeType: (NSString*) _mimeType
{
    if ((self = [super init]) != nil)
    {
        url = _url;
        mimeType = _mimeType;

        self.title = name !=nil ? name : [url lastPathComponent];

        UIView *navigationBar = [[UIView alloc] initWithFrame:CGRectMake(0,[[[UIDevice currentDevice] systemVersion] floatValue] >= 7 ? 20 : 0, self.view.frame.size.width, 44)];
        navigationBar.autoresizingMask = UIViewAutoresizingFlexibleWidth;
        navigationBar.backgroundColor = RGBCOLOR(0x2f2f2f);
        navigationBar.tintColor = [UIColor redColor];
        [self.view addSubview:navigationBar];

        UILabel *navTitleLabel= [[UILabel alloc] initWithFrame:CGRectMake(100, 0,navigationBar.frame.size.width-200, navigationBar.frame.size.height)];
        navTitleLabel.textColor = RGBCOLOR(0xFFFFFF);
        navTitleLabel.backgroundColor = [UIColor clearColor];
        navTitleLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth;
        navTitleLabel.font = [UIFont fontWithName:@"HelveticaNeue" size:18];
        navTitleLabel.textAlignment = NSTextAlignmentCenter;
        navTitleLabel.text = name !=nil ? name : [url lastPathComponent];
        [navigationBar addSubview:navTitleLabel];

        UIButton *backButton = [[UIButton alloc] initWithFrame:CGRectMake(20,12,10,20)];
        [backButton setBackgroundImage:[UIImage imageNamed:@"ic_title_back@2x.png"] forState:UIControlStateNormal];
        [backButton addTarget:self action:@selector(onClose) forControlEvents:UIControlEventTouchUpInside];
        [navigationBar addSubview:backButton];
    }
    return self;
}

- (void)loadView
{
    [super loadView];

    self.view.backgroundColor = RGBCOLOR(0x2f2f2f);

    int yOffset = [[[UIDevice currentDevice] systemVersion] floatValue] >= 7 ? 20 : 0;

    webView = [[UIWebView alloc] initWithFrame:CGRectMake(0, yOffset +44, self.view.frame.size.width, self.view.frame.size.height-44-yOffset)];
    webView.autoresizesSubviews = true;
    webView.autoresizingMask = (UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth);
    webView.scalesPageToFit = true;
    webView.backgroundColor = [UIColor whiteColor];
    webView.delegate = self;
    [self.view addSubview:webView];

    ai = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    ai.center = CGPointMake(webView.bounds.size.width/2, (webView.bounds.size.height/2));
    ai.autoresizingMask = (UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleTopMargin);
    [webView addSubview:ai];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:url]];

    if (mimeType == nil)
        [webView loadRequest:request];
    else
    {
        [ai startAnimating];

        [NSURLConnection sendAsynchronousRequest:request queue:[NSOperationQueue currentQueue] completionHandler:

         ^(NSURLResponse *response, NSData *data, NSError *connectionError) {
             [webView loadData:data MIMEType:mimeType textEncodingName:@"UTF-8" baseURL:nil];
             [ai stopAnimating];
         }];
    }
}

- (void) onClose
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark -
#pragma mark Webview Delegate

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    [ai startAnimating];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    [ai stopAnimating];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    [ai stopAnimating];
}

@end
