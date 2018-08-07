package com.example.customapp

import com.sas.android.visualanalytics.sdk.ConnectionCreateDescriptor
import com.sas.android.visualanalytics.sdk.ReportDescriptor

/**
 * Sample ConnectionCreateDescriptor implementation that defines the connection to the
 * VA Try server and paths to four sample reports.
 */
internal class VATryStartupConnectionDescriptor : ConnectionCreateDescriptor {
    override val description = "VA Try"
    override val hostName = "vatry.ondemand.sas.com"
    override val password = null
    override val port = 80

    override val reports = listOf(
            ReportDescriptor.forFileSystemPath("/Industry Samples/Banking/Capital Exposure and Risk Report"),
            ReportDescriptor.forFileSystemPath("/Industry Samples/Casinos/Casino Floor Performance"),
            ReportDescriptor.forFileSystemPath("/Industry Samples/Communications/Wireless call quality analysis report"),
            ReportDescriptor.forFileSystemPath("/Industry Samples/Digital Advertising/Digital Advertising"))

    override val useGuestMode = true
    override val userId = null
    override val useSsl = false
}
