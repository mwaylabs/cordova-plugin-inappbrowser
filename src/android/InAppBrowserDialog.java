package com.mwaysolutions.cordova.webviewplugin;

import android.app.Dialog;
import android.content.Context;


public class InAppBrowserDialog extends Dialog {

    Context       context;
    WebViewPlugin inAppBrowser = null;

    public InAppBrowserDialog(final Context context, final int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setInAppBroswer(final WebViewPlugin browser) {
        this.inAppBrowser = browser;
    }

    @Override
    public void onBackPressed() {
        if (this.inAppBrowser == null) {
            this.dismiss();
        } else {
            // better to go through the in inAppBrowser
            // because it does a clean up
            this.inAppBrowser.closeDialog();
        }
    }
}
