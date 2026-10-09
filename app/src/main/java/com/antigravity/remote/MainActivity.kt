package com.antigravity.remote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.antigravity.remote.auth.GoogleAuthHelper
import com.antigravity.remote.data.AccountManager
import com.antigravity.remote.data.SessionManager
import com.antigravity.remote.databinding.ActivityMainBinding
import com.antigravity.remote.model.AntigravityAccount
import com.antigravity.remote.notification.NotificationHelper
import com.antigravity.remote.ui.AccountDialog
import com.antigravity.remote.ui.HapticHelper
import com.antigravity.remote.ui.MobileViewportInjector
import com.antigravity.remote.ui.PromptBottomSheet
import com.antigravity.remote.ui.SessionSwitcherBottomSheet
import com.antigravity.remote.ui.SubagentTaskItem
import com.antigravity.remote.ui.SubagentsBottomSheet
import com.antigravity.remote.ui.UsageBottomSheet
import com.antigravity.remote.web.AntigravityJSBridge
import com.antigravity.remote.web.AntigravityWebChromeClient
import com.antigravity.remote.web.AntigravityWebViewClient
import com.antigravity.remote.worker.InstanceMonitorWorker
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var accountManager: AccountManager
    private lateinit var sessionManager: SessionManager
    private lateinit var googleAuthHelper: GoogleAuthHelper
    private lateinit var notificationHelper: NotificationHelper

    private val defaultUrl = "https://antigravity.google.com/"
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val account = googleAuthHelper.handleSignInResult(result.data)
            if (account != null) {
                val newAcc = AntigravityAccount(
                    name = account.displayName ?: "Google User",
                    email = account.email ?: "user@gmail.com"
                )
                accountManager.saveAccount(newAcc)
                accountManager.setActiveAccount(newAcc.id)
                accountManager.applyCookiesForAccount(newAcc)
                updateHeaderUI()
                loadUrlForActiveAccount(defaultUrl)
                Toast.makeText(this, "Signed in as ${newAcc.email}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (filePathCallback == null) return@registerForActivityResult
            val results: Array<Uri>? = if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data?.data != null) {
                    arrayOf(data.data!!)
                } else if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                } else null
            } else null

            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    binding.inputNativePrompt.setText(spokenText)
                }
            }
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Notification permission granted
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accountManager = AccountManager(this)
        sessionManager = SessionManager(this)
        googleAuthHelper = GoogleAuthHelper(this)
        notificationHelper = NotificationHelper(this)

        checkNotificationPermission()
        setupDefaultAccountIfNeeded()
        setupWebView()
        setupSwipeRefresh()
        setupButtonsAndNavigation()
        setupNativeChatBar()
        setupDrawerNavigation()
        setupBackgroundSync()

        updateHeaderUI()
        val targetUrl = intent.getStringExtra("TARGET_URL") ?: defaultUrl
        loadUrlForActiveAccount(targetUrl)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupDefaultAccountIfNeeded() {
        val accounts = accountManager.getAccounts()
        if (accounts.isEmpty()) {
            val defaultAcc = AntigravityAccount(
                name = "Primary Account",
                email = "user@gmail.com"
            )
            accountManager.saveAccount(defaultAcc)
            accountManager.setActiveAccount(defaultAcc.id)
        }
    }

    private fun updateHeaderUI() {
        val activeAccount = accountManager.getActiveAccount()
        val activeSession = sessionManager.getActiveSession(activeAccount?.id ?: "")

        binding.txtActiveSessionTitle.text = activeSession.title
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.allowFileAccess = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mediaPlaybackRequiresUserGesture = false

        settings.cacheMode = WebSettings.LOAD_DEFAULT

        val customUserAgent = settings.userAgentString + " AntigravityRemoteMobile/4.0 (Android Native App)"
        settings.userAgentString = customUserAgent

        val jsBridge = AntigravityJSBridge(
            context = this,
            accountManager = accountManager,
            notificationHelper = notificationHelper
        )
        binding.webView.addJavascriptInterface(jsBridge, "AntigravityNative")

        binding.webView.webViewClient = AntigravityWebViewClient(
            binding.swipeRefreshLayout
        ) { url ->
            accountManager.saveCurrentCookiesForActiveAccount()
            MobileViewportInjector.injectMobileCSS(binding.webView)
        }

        binding.webView.webChromeClient = AntigravityWebChromeClient(
            progressBar = binding.progressBar,
            onShowFileChooserCallback = { callback, _ ->
                if (filePathCallback != null) {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = null
                }
                filePathCallback = callback

                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                filePickerLauncher.launch(Intent.createChooser(intent, "Select Attachment"))
                true
            }
        )
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary, R.color.accent
        )
    }

    private fun setupNativeChatBar() {
        binding.btnNativeSend.setOnClickListener { view ->
            HapticHelper.performClick(view)
            val text = binding.inputNativePrompt.text.toString().trim()
            if (text.isNotEmpty()) {
                sendPromptToWeb(text)
                binding.inputNativePrompt.setText("")
            }
        }

        binding.btnNativeAttach.setOnClickListener { view ->
            HapticHelper.performClick(view)
            openFilePickerForWeb()
        }

        binding.btnNativeVoice.setOnClickListener { view ->
            HapticHelper.performClick(view)
            startVoiceDictation()
        }
    }

    private fun setupDrawerNavigation() {
        binding.btnNavDrawer.setOnClickListener {
            HapticHelper.performClick(it)
            binding.drawerLayout.openDrawer(Gravity.START)
        }

        binding.navigationDrawer.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(Gravity.START)
            when (item.itemId) {
                R.id.drawer_new_chat -> {
                    loadUrlForActiveAccount(defaultUrl)
                    true
                }
                R.id.drawer_projects -> {
                    openSessionSwitcher()
                    true
                }
                R.id.drawer_subagents -> {
                    showSubagentsMonitorSheet()
                    true
                }
                R.id.drawer_usage -> {
                    UsageBottomSheet.showUsageAnalytics(this, accountManager)
                    true
                }
                R.id.drawer_accounts -> {
                    AccountDialog.showAccountSwitcher(this, accountManager) { selected ->
                        updateHeaderUI()
                        loadUrlForActiveAccount(selected.customUrl)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtonsAndNavigation() {
        binding.txtActiveSessionTitle.setOnClickListener {
            openSessionSwitcher()
        }

        binding.btnGoogleAuth.setOnClickListener {
            val googleAccount = googleAuthHelper.getSignedInAccount()
            if (googleAccount != null) {
                AccountDialog.showAccountSwitcher(this, accountManager) { selected ->
                    updateHeaderUI()
                    loadUrlForActiveAccount(selected.customUrl)
                }
            } else {
                googleSignInLauncher.launch(googleAuthHelper.getSignInIntent())
            }
        }

        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadUrlForActiveAccount(defaultUrl)
                    true
                }
                R.id.nav_sessions -> {
                    openSessionSwitcher()
                    true
                }
                R.id.nav_usage -> {
                    UsageBottomSheet.showUsageAnalytics(this, accountManager)
                    true
                }
                R.id.nav_accounts -> {
                    AccountDialog.showAccountSwitcher(this, accountManager) { selectedAccount ->
                        updateHeaderUI()
                        loadUrlForActiveAccount(selectedAccount.customUrl)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun showSubagentsMonitorSheet() {
        val activeSubagents = listOf(
            SubagentTaskItem(
                id = "sub-1",
                role = "Codebase Researcher",
                typeName = "research",
                state = "running",
                description = "Analyzing project files & dependencies"
            ),
            SubagentTaskItem(
                id = "sub-2",
                role = "Gradle Build Runner",
                typeName = "build",
                state = "idle",
                description = "Waiting for next compile trigger"
            )
        )

        SubagentsBottomSheet.showSubagentsMonitor(this, activeSubagents) { id ->
            Toast.makeText(this, "Subagent $id stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSessionSwitcher() {
        SessionSwitcherBottomSheet.showSessionSwitcher(
            context = this,
            accountManager = accountManager,
            sessionManager = sessionManager
        ) { selectedSession ->
            updateHeaderUI()
            loadUrlForActiveAccount(selectedSession.url)
        }
    }

    private fun sendPromptToWeb(promptText: String) {
        val escaped = promptText.replace("'", "\\'").replace("\n", "\\n")
        val jsScript = """
            (function() {
                const el = document.querySelector('textarea') || document.querySelector('[contenteditable="true"]');
                if (el) {
                    if (el.tagName === 'TEXTAREA') {
                        el.value = '$escaped';
                    } else {
                        el.innerText = '$escaped';
                    }
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.focus();
                    
                    const sendBtn = document.querySelector('button[type="submit"]') || document.querySelector('button[aria-label*="Send"]');
                    if (sendBtn) {
                        setTimeout(() => sendBtn.click(), 100);
                    }
                }
            })();
        """.trimIndent()

        binding.webView.evaluateJavascript(jsScript, null)
        HapticHelper.vibrateSuccess(this)
        Toast.makeText(this, "Prompt sent", Toast.LENGTH_SHORT).show()
    }

    private fun openFilePickerForWeb() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Attach Media/File to Agent"))
    }

    private fun startVoiceDictation() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak prompt for Antigravity Agent...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUrlForActiveAccount(url: String) {
        val activeAccount = accountManager.getActiveAccount()
        if (activeAccount != null) {
            accountManager.applyCookiesForAccount(activeAccount, binding.webView)
        }
        binding.webView.loadUrl(url)
    }

    private fun setupBackgroundSync() {
        val syncWorkRequest = PeriodicWorkRequestBuilder<InstanceMonitorWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AntigravityInstanceMonitor",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawer(Gravity.START)
        } else if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
