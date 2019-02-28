# SAS SDK Release Notes

### Version 8.33
##### Notes:
- Kotlin version has been updated to 1.3.0
- Migrated to AndroidX:
    - For complete instructions on the migration visit: https://developer.android.com/jetpack/androidx/migrate
    - Example:<br/>
        // In the build.gradle file <br/>
        Replace: `implementation com.android.support:appcompat-v7:28.0.0`<br/>
        With: `implementation androidx.appcompat:appcompat:1.0.2`<br/>

### Version 8.32
##### Notes:
- minSdkVersion has been updated to 23
- compileSDKVersion and build tools versions have been updated to 28
- ReportViewContoller:
	- AddFullScreenListener/RemoveFullScreenListener has been removed in favor of ReportEvents and addReportEventListener
- ConnectionCreateDescriptor has been removed and its password property absorbed into ConnectionDescriptor. The reports property containing a list of ReportDescriptors has been removed. These ReportDescriptors are now passed in to SASManager.create().
- SASManager:
    - create()
        - Now takes ConnectionDescriptor instead of ConnectionCreateDescriptor
        - Now takes a list of ReportDescriptors. These were previously contained in ConnectionCreateDescriptor.

##### Update Instructions:
- ConnectionCreateDescriptor:
    - Replace any usage of ConnectionCreateDescriptor with a ConnectionDescriptor, including the same password property that was previously used in ConnectionCreateDescriptor.
    - Pass the list of reports that used to be contained by ConnectionCreateDescriptor to the method SASManager.create() when establishing a connection.
- ReportViewController: 
   - If you are using the addFullScreenListener/removeFullScreenListener, you'll need to replace that implementation with a ReportEventListener and check for the FullScreenRequest ReportEvent type.
        - Example:
            ```
                it.addReportEventListener { reportEvent ->
                     when (reportEvent){
                         is FullScreenRequest -> supportActionBar?.run {
                            if (reportEvent.fullScreen) {
                                hide()
                            } else {
                                show()
                            }
                        }
                    }
                }
            ```
