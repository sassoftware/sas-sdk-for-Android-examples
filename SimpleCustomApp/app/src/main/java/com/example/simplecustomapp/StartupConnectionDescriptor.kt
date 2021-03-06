package com.example.simplecustomapp

import com.sas.android.visualanalytics.sdk.ConnectionDescriptor
import com.sas.android.visualanalytics.sdk.ReportDescriptor

/**
 * Sample ConnectionCreateDescriptor implementation that defines the connection to the
 * VA Try server and paths to four sample reports.
 */
internal class StartupConnectionDescriptor : ConnectionDescriptor {
    override val description = "VA Try Before you Buy"
    override val hostName = "tbub.sas.com"
    override val password = null
    override val port = 443
    override val useGuestMode = true
    override val userId = null
    override val useSsl = true
    override val useStdAuth = false
    val reports = listOf(ReportDescriptor.forFileSystemPath(
            "/Public/Utilities/Water Consumption and Monitoring"))
}
