package com.example.simplecustomapp

import com.sas.android.visualanalytics.sdk.ConnectionDescriptor
import com.sas.android.visualanalytics.sdk.ReportDescriptor

/**
 * Sample ConnectionCreateDescriptor implementation that defines the connection to the
 * VA Try server and a path to a sample report.
 */
internal class StartupConnectionDescriptor : ConnectionDescriptor {
    override val description = "VA Try"
    override val hostName = "tbub.sas.com"
    override val password = null
    override val port = 443
    override val useGuestMode = true
    override val userId = null
    override val useSsl = true

    // Force this server to use standard (user ID/password) authentication even if it supports web
    // authentication. This is provided purely as an example; it is not typically necessary since
    // the algorithm will fall back to standard authentication if a server does not support web
    // authentication.
    override val useStdAuth = true

    val reports = listOf(ReportDescriptor.forFileSystemPath(
            "/Public/Utilities/Water Consumption and Monitoring"))
}
