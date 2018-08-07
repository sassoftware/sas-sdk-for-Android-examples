package com.example.simplecustomapp

import com.sas.android.visualanalytics.sdk.ConnectionCreateDescriptor
import com.sas.android.visualanalytics.sdk.ReportDescriptor

/**
 * Sample ConnectionCreateDescriptor implementation that defines the connection to the
 * VA Try server and paths to four sample reports.
 */
internal class StartupConnectionDescriptor : ConnectionCreateDescriptor {
    override val description = "VA Try Before you Buy"
    override val hostName = "tbub.sas.com"
    override val password = null
    override val port = 443
    override val reports = listOf(
        ReportDescriptor.forFileSystemPath("/Public/Utilities/Water Consumption and Monitoring"))
    override val useGuestMode = true
    override val userId = null
    override val useSsl = true
}
