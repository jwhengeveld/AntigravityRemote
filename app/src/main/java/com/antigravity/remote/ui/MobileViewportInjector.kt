package com.antigravity.remote.ui

import android.webkit.WebView

object MobileViewportInjector {

    fun injectMobileCSS(webView: WebView) {
        val mobileCssScript = """
            (function() {
                // 1. Inject Mobile CSS Stylesheet
                const styleId = 'antigravity-native-mobile-style';
                let style = document.getElementById(styleId);
                if (!style) {
                    style = document.createElement('style');
                    style.id = styleId;
                    document.head.appendChild(style);
                }

                style.innerHTML = `
                    /* HIDE WEB APP TOP HEADERS (prevents double topbar) */
                    header, 
                    nav, 
                    [role="banner"], 
                    div[class*="header"], 
                    div[class*="Header"], 
                    div[class*="top-bar"], 
                    div[class*="topbar"],
                    div[class*="app-bar"],
                    div[class*="AppHeader"] {
                        display: none !important;
                        height: 0 !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }

                    /* HIDE WEB APP PROMPT INPUT CONTAINER (prevents double input box) */
                    form, 
                    footer,
                    div[class*="input-container"], 
                    div[class*="InputContainer"], 
                    div[class*="prompt-box"], 
                    div[class*="PromptBox"],
                    div[class*="composer"], 
                    div[class*="Composer"],
                    div[class*="chat-input"],
                    div[class*="ChatInput"],
                    div[class*="bottom-bar"],
                    div[class*="BottomBar"],
                    div[class*="input-area"],
                    div[class*="InputArea"] {
                        display: none !important;
                        height: 0 !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }

                    /* FULL SCREEN CANVAS & BODY OPTIMIZATIONS */
                    html, body {
                        margin: 0 !important;
                        padding: 0 !important;
                        width: 100% !important;
                        height: 100% !important;
                        background-color: #121212 !important;
                        overflow-x: hidden !important;
                        -webkit-tap-highlight-color: transparent !important;
                    }

                    main, 
                    .chat-canvas, 
                    .editor-container, 
                    div[class*="content"], 
                    div[class*="canvas"],
                    div[class*="ChatCanvas"] {
                        width: 100% !important;
                        max-width: 100% !important;
                        height: 100% !important;
                        padding-top: 4px !important;
                        padding-bottom: 8dp !important;
                        margin: 0 !important;
                    }

                    /* TYPOGRAPHY */
                    .message-bubble, div[class*="message"], div[class*="Message"] {
                        font-size: 15px !important;
                        line-height: 1.5 !important;
                    }

                    ::-webkit-scrollbar {
                        width: 3px !important;
                        height: 3px !important;
                    }
                    ::-webkit-scrollbar-thumb {
                        background: rgba(255, 255, 255, 0.25) !important;
                        border-radius: 3px !important;
                    }
                `;

                // 2. Define AntigravitySendPrompt JS Function
                window.AntigravitySendPrompt = function(text) {
                    const inputs = Array.from(document.querySelectorAll('textarea, [contenteditable="true"], input[type="text"]'));
                    const promptEl = inputs.find(el => el.getAttribute('placeholder')?.toLowerCase().includes('ask') || el.tagName === 'TEXTAREA') || inputs[0];

                    if (promptEl) {
                        if (promptEl.tagName === 'TEXTAREA' || promptEl.tagName === 'INPUT') {
                            promptEl.value = text;
                        } else {
                            promptEl.innerText = text;
                        }

                        promptEl.dispatchEvent(new Event('input', { bubbles: true }));
                        promptEl.dispatchEvent(new Event('change', { bubbles: true }));

                        setTimeout(function() {
                            const buttons = Array.from(document.querySelectorAll('button'));
                            const sendBtn = buttons.find(b => 
                                b.getAttribute('type') === 'submit' ||
                                b.getAttribute('aria-label')?.toLowerCase().includes('send') ||
                                b.innerText.toLowerCase().includes('send')
                            ) || buttons[buttons.length - 1];

                            if (sendBtn) {
                                sendBtn.click();
                            }
                        }, 150);
                    }
                };

                // 3. Extract Title & DOM State Periodically
                if (!window.AntigravityStateInterval) {
                    window.AntigravityStateInterval = setInterval(function() {
                        let title = "";
                        const h1 = document.querySelector('h1, h2, div[class*="title"], div[class*="Title"]');
                        if (h1 && h1.innerText.trim().length > 0) {
                            title = h1.innerText.trim();
                        } else {
                            title = document.title || "Antigravity Session";
                        }

                        let modelName = "";
                        const modelEl = document.querySelector('button[aria-label*="model"], div[class*="model"], span[class*="model"]');
                        if (modelEl) {
                            modelName = modelEl.innerText.trim();
                        }

                        const sessions = [];
                        const links = Array.from(document.querySelectorAll('a[href*="/c/"], div[class*="conversation-item"]'));
                        links.forEach(link => {
                            const sessTitle = link.innerText.trim();
                            const url = link.href || window.location.href;
                            if (sessTitle.length > 0) {
                                sessions.push({ title: sessTitle, url: url });
                            }
                        });

                        if (window.AntigravityNative) {
                            window.AntigravityNative.updateDOMState(title, modelName, JSON.stringify(sessions));
                        }
                    }, 2000);
                }

            })();
        """.trimIndent()

        webView.evaluateJavascript(mobileCssScript, null)
    }
}
