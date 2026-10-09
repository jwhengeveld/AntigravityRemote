package com.antigravity.remote.ui

import android.webkit.WebView

object MobileViewportInjector {

    fun injectMobileCSS(webView: WebView) {
        val mobileCssScript = """
            (function() {
                if (window.AntigravityMobileCSSInjected) return;
                window.AntigravityMobileCSSInjected = true;

                const style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = `
                    /* Mobile Touch Optimization */
                    body, html {
                        -webkit-tap-highlight-color: transparent !important;
                        touch-action: manipulation !important;
                        margin: 0 !important;
                        padding: 0 !important;
                        overflow-x: hidden !important;
                    }

                    /* Hide Web Navigation Clutter (Header, Web Sidebar) */
                    header, nav, .web-header, .top-navigation, .desktop-sidebar, .web-drawer {
                        display: none !important;
                    }

                    /* Expand Chat Canvas to 100% full screen height & width */
                    main, .chat-canvas, .editor-container, .content-panel {
                        width: 100% !important;
                        height: 100% !important;
                        max-width: 100% !important;
                        padding: 8px !important;
                        margin: 0 !important;
                    }

                    /* Optimize Message Bubble text formatting for mobile */
                    .message-bubble, .user-prompt, .agent-response {
                        font-size: 15px !important;
                        line-height: 1.5 !important;
                        padding: 10px 14px !important;
                        border-radius: 14px !important;
                    }

                    /* Custom Scrollbars */
                    ::-webkit-scrollbar {
                        width: 3px !important;
                        height: 3px !important;
                    }
                    ::-webkit-scrollbar-thumb {
                        background: rgba(255, 255, 255, 0.25) !important;
                        border-radius: 3px !important;
                    }
                `;
                document.head.appendChild(style);
            })();
        """.trimIndent()

        webView.evaluateJavascript(mobileCssScript, null)
    }
}
