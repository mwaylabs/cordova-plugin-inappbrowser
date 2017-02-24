# cordova-plugin-inappbrowser

This plugin adds a customizable in app browser to your cordova application.

```
 $window.webview.openWebView(success, failure, {
      iconColor: ' #ffff00',
      backgroundColor: '#f00000',
      isPDF: false,
      url: 'http://mwaysolutions.com',
      urlEncoding: false,
      visibleAddress: false,
      editableAddress: false,
      navigationAtTop: false,
      icons:{
        backward: true,
        forward: true,
        refresh: true
      }
    });
```

## Installation
```
  $ cordova plugin add https://github.com/mwaylabs/cordova-plugin-inappbrowser.git
```

To use the "bold" icons option please use the branch: 'inappbrowser-bold':
```
  <plugin name="com.mwaysolutions.cordova.webviewplugin" spec="https://github.com/mwaylabs/cordova-plugin-inappbrowser.git#inappbrowser-bold" />
```
## Options
- All parameters are required but navigationAtTop(when not specified the navigation will show at the bottom) and urlEncoding(when not specified the url will not be encoded).

- iconColor: Defines the color of the icons expectes a string with a colorcode e.g. '#000000'
- backgroundColor: Defines the backgroundcolor of the bar at the bottom of the webview, expectes a string with a colorcode e.g. '#FFFFFF'
- isPDF: Defines whether to show or hide the navigation buttons (next, back, reload) expects a boolean
- url: The url of the resource to show. expects a string with a valid url
- urlEncoding: Defines whether to encode or not the url (not encoding by default). Expects a boolean
- visibleAddress: (Only for Android) Defines whether if you can see the address url you are visiting or not. Expects a boolean.
- editableAddress: (Only for Android) Defines whether if you can edit the address url you are visiting or not. Expects a boolean.
- navigationAtTop: Defines whether to position the navigation at the top or at the bottom of screen (bottom by default). Expects a boolean
- icons: Expects an object { (ignored when 'isPDF' is true)
  - backward: Defines whether the backward button is shown or not. Expects a boolean.
  - forward: Defines whether the forward button is shown or not. Expects a boolean.
  - refresh: Defines whether the refresh button is shown or not. Expects a boolean.
}

### Supported Platforms
- iOS 7+
- Android 4.4+

### License
Code licensed under MIT. Docs under Apache 2. PhoneGap is a trademark of Adobe.
