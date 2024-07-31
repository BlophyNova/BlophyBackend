package net.blophy.lib.config

import java.io.File
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

object SSLConfig {
    const val useSsl = false
    const val keyStorePath = "keystore.jks"
    const val keyStorePassword = "wdnmdpassword"
}

fun createSslContext(config: SSLConfig = SSLConfig): SSLContext? {
    return if (File(config.keyStorePath).exists()) {
        val keystore = KeyStore.getInstance("JKS").apply {
            load(File(config.keyStorePath).inputStream(), config.keyStorePassword.toCharArray())
        }

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keystore, config.keyStorePassword.toCharArray())
        }

        SSLContext.getInstance("TLS").apply {
            init(keyManagerFactory.keyManagers, null, null)
        }
    } else {
        null
    }
}