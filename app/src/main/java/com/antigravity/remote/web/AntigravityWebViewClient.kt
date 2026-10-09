package com.antigravity.remote.web

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class AntigravityWebViewClient(
    private val swipeRefreshLayout: SwipeRefreshLayout?,
    private val onPageFinishedCallback: ((url: String) -> Unit)? = null
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        if (url.contains("antigravity.google.com") || url.contains("google.com") || url.startsWith("file://")) {
            return false
        }
        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        swipeRefreshLayout?.isRefreshing = true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        swipeRefreshLayout?.isRefreshing = false

        // Inject JS bridge observer script for Antigravity web events
        view?.evaluateJavascript(
            """
            (function() {
                if (window.AntigravityNativeObserverInjected) return;
                window.AntigravityNativeObserverInjected = true;

                // Monitor DOM for subagent completion badges or toast notifications
                const observer = new MutationObserver(function(mutations) {
                    mutations.forEach(function(m) {
                        m.addedNodes.forEach(function(node) {
                            if (node.nodeType === 1) {
                                const text = node.innerText || "";
                                if (text.includes("Task completed") || text.includes("Subagent finished") || text.includes("Goal achieved")) {
                                    if (window.AntigravityNative) {
                                        window.AntigravityNative.notifyTaskFinished("Subagent Task Completed", text.substring(0, 100), window.location.href);
                                    }
                                }
                            }
                        });
                    });
                });
                observer.observe(document.body, { childList: true, subtree: true });
            })();
            """.trimIndent(), null
        )

        if (url != null) {
            onPageFinishedCallback?.invoke(url)
        }
    }
}
