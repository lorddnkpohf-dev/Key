package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MoonSkyApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonSkyApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var webView: WebView? by remember { mutableStateOf(null) }
    var linkInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "Moon Sky Menu",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = linkInput,
                        onValueChange = { linkInput = it },
                        label = { Text("Вставить ссылку") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val uri = android.net.Uri.parse(linkInput)
                            val key = uri.getQueryParameter("id") ?: "Ключ не найден"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MoonKey", key)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Ключ скопирован: $key", Toast.LENGTH_SHORT).show()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Получить ключ")
                    }
                }
                
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Refresh") },
                    selected = false,
                    onClick = {
                        webView?.reload()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Moon Sky") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            MoonSkyWebView(
                modifier = Modifier.padding(innerPadding),
                onWebViewCreated = { webView = it }
            )
        }
    }
}

@Composable
fun MoonSkyWebView(modifier: Modifier = Modifier, onWebViewCreated: (WebView) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url.toString()
                        if (url.contains("loot-link.com") || url.contains("gateway.platoboost.com")) {
                            val rParam = request?.url?.getQueryParameter("r")
                            if (!rParam.isNullOrBlank()) {
                                try {
                                    val decodedBytes = Base64.decode(rParam, Base64.DEFAULT)
                                    val decodedUrl = String(decodedBytes)
                                    view?.loadUrl(decodedUrl)
                                    return true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        return false
                    }
                }
                onWebViewCreated(this)
                loadUrl("https://loot-link.com/s?r=aHR0cHM6Ly9nb29nbGUuY29t")
            }
        },
        modifier = modifier
    )
}
