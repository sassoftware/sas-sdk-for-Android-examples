# Covid19App Example

The Covid19App example illustrates how to use the SAS SDK for Android to independently extract and display individual objects
from a report.

### Overview

[Android Architecture Components](https://developer.android.com/topic/libraries/architecture) are
used heavily in this example.

1. [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)s manage the download and display of the report.

1. [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) monitors changes in the ViewModel.

1. [Data Binding](https://developer.android.com/topic/libraries/data-binding) declaratively displays
and monitors changes in the model.

### Key classes

The [ReportRepository](app/src/main/java/com/sas/android/covid19/ReportRepository.kt) ensures the
report is downloaded and up-to-date.

[MainActivity](app/src/main/java/com/sas/android/covid19/MainActivity.kt) calls the
getReportObjectProvider() method on the application's SASManager instance. The returned
ReportObjectProvider provides a finer-level of control over which parts of the report to display.

The [VisualLoader](app/src/main/java/com/sas/android/covid19/util/VisualLoader.kt) uses the
ReportObjectProvider to extract the text, a Bitmap snapshot, or a live View of each visual (object)
within the report.
