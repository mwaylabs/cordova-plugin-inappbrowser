/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var app = {
    initialize: function () {
        document.getElementById('buttonRemote').addEventListener("click", this.openRemote);
        document.getElementById('buttonLocal').addEventListener("click", this.openLocal);
        document.getElementById('buttonPDF').addEventListener("click", this.openPDF);
    },

    openRemote: function () {
        app.openWebView('https://mwaysolutions.com', false);
    },
    openLocal: function () {
        app.openWebView('file:///www/local.html', false);
    },
    openPDF: function () {
        window.webview.openWebView(null, null, {
            iconColor: '#ffff00',
            isPDF: true,
            url: 'file:///www/sample.pdf',
        });
    },
    openWebView: function (url, navigationAtTop) {
        console.log(`opening ${url}`);
        window.webview.openWebView(null, null, {
            iconColor: '#ffff00',
            backgroundColor: '#f00000',
            isPDF: false,
            url: url,
            urlEncoding: false,
            visibleAddress: false,
            editableAddress: false,
            navigationAtTop: navigationAtTop,
            icons: {
                backward: true,
                forward: true,
                refresh: true
            },
        });
    }
};

app.initialize();
