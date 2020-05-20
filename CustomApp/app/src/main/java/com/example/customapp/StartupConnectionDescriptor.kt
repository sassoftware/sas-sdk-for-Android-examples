package com.example.customapp

import com.sas.android.visualanalytics.sdk.ConnectionDescriptor
import com.sas.android.visualanalytics.sdk.ReportDescriptor

/**
 * Sample ConnectionCreateDescriptor implementation that defines the connection to the
 * VA Try server and paths to four sample reports.
 */
internal class StartupConnectionDescriptor : ConnectionDescriptor {
    override val description = "VA Try"
    override val hostName = "tbub.sas.com"
    override val password = null
    override val port = 443
    override val useGuestMode = true
    override val userId = null
    override val useSsl = true
    override val useStdAuth = false
    val reports = listOf(
        ReportDescriptor.forUri("faca01f6-c631-4cbf-b336-6ba186dc632e"),
        ReportDescriptor.forUri("1ccd88c8-38a6-4473-90e0-8bdb447510a4"),
        ReportDescriptor.forUri("03db38a7-ff39-460e-9aca-3ee108c10140"),
        ReportDescriptor.forUri("cd4205df-44a8-448a-a174-765f89abe058"))
}
