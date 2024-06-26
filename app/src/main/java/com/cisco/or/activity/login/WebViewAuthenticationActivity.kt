package com.cisco.or.activity.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cisco.or.databinding.FragmentWebviewBinding
import com.cisco.or.fragments.WebViewFragment

open class WebViewAuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: FragmentWebviewBinding


    companion object {
        private val TAG = WebViewAuthenticationActivity::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.supportActionBar?.hide()

        val url = intent.getStringExtra("url")

        if (!url.isNullOrBlank()) {

            binding.webView.webViewClient = WebViewFragment.CustomWebViewClient(binding.progressBar)
            binding.webView.loadUrl(url)
        }
    }
}