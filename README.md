# SAS SDK for Android Examples

This repository contains Android Studio projects illustrating the use of the SAS SDK for Android which enables Android developers to build customized apps that include content from [SAS® Visual Analytics](https://www.sas.com/en_us/software/visual-analytics.html). The SAS SDK lets you build mobile apps that you can personalize, preconfigure, customize, and manage to meet your exact requirements.

### SDK Setup


* Access and download the maven repository (mavenrepo.zip) from the [developer.sas.com Mobile SDK site] (https://developer.sas.com/guides/mobile-sdk.html).
* Unzip mavenrepo.zip to a location such as: [/tmp/mavenrepo](file:///tmp/mavenrepo/).
* Define an environment variable that points to the location:

		export MOBILEBI_MAVEN_REPO_URL=/tmp/mavenrepo

* Please view the [SAS SDK release notes](https://developer.sas.com/sdk/mobile/android/doc/current/release_notes.html) for a rundown on new features and changes.

## Build and Run Example Apps

With Android Studio: "Open An Existing Android Studio Project" or "File" > "Open", then navigate to and select project's build.gradle file.

You should be able to build and run the example projects on an emulator or device right away.

## Scenarios addressed through the Example Apps

### Personalize and Pre-configure:

The first step app developers using the SAS SDK may want to take is to personalize the existing SAS Visual Analytics app by building a mobile app that uses their app name and icon. This can be done by creating a project with just resource files for your app name, icon and other assets. You can also pre-configure the connections to the SAS Visual Analytics server and the reports that you would like to be downloaded in a connections.json file under the assets folder.

No code is necessary for these use cases.

Example: [PersonalizedApp](PersonalizedApp)

### Customize:

Creating a fully customized mobile app lets you include both SAS Visual Analytics reports and any other content and capabilities that tie into your organizational goals, processes, and projects. An example of a custom mobile app built with the SAS SDK is [GatherIQ](https://gatheriq.analytics/), a free app that is part of the **SAS Data for Good** program. This app is available in the App Store and Google Play.

http://developer.sas.com/sdk/mobile/android/doc/current/toolkit/alltypes/index.html

Examples:

* [SimpleCustomApp](SimpleCustomApp)
* [CustomApp](CustomApp)
* [Covid19App](Covid19App)

### Manage
If you manage and secure your mobile devices with a Mobile Device Management (MDM) solution, you can integrate your mobile apps with your MDM solution by using the SAS SDK. This is done by providing custom implementations of the various interfaces in [SASManagerContext](https://developer.sas.com/sdk/mobile/android/doc/current/toolkit/com.sas.android.visualanalytics.sdk/-s-a-s-manager-context/index.html), namely:

* [FileHandler](https://developer.sas.com/sdk/mobile/android/doc/current/toolkit/com.sas.android.visualanalytics.sdk/-file-handler/index.html)
* [HttpHandler](https://developer.sas.com/sdk/mobile/android/doc/current/toolkit/com.sas.android.visualanalytics.sdk/-http-handler/index.html)
* [ShareHandler](https://developer.sas.com/sdk/mobile/android/doc/current/toolkit/com.sas.android.visualanalytics.sdk/-share-handler/index.html)
* [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences)

## A note about security

Connections to SAS Visual Analytics servers are subject to the [network security
configuration](https://developer.android.com/training/articles/security-config) defined by the
application. Namely, starting with Android 9 (API level 28), cleartext (HTTP) connections are
disabled (**by Android**) by default.

It is recommended that all SAS Visual Analytics servers be set up to allow only secure (HTTPS)
connections. However, in situations where your app must connect to a server via HTTP, a less
restrictive network security configuration can be put in place:

```xml
<network-security-config
    xmlns:tools="http://schemas.android.com/tools"
>
    <!--
     * Allow HTTP connections
     * Use <domain-config> for a more precise policy
     -->
    <base-config
        cleartextTrafficPermitted="true"
    />
</network-security-config>
```

This file must be [referenced from AndroidManifest.xml](https://developer.android.com/training/articles/security-config#manifest).

**Note that such a configuration reduces the overall security throughout the app, and should be
avoided if possible.**

## License
This project is licensed under this commercial [license](LICENSE.txt).

## Support
For information, advice, and questions on the use of SAS Mobile SDKs please start with the [SAS Visual Analytics online community](https://communities.sas.com/Visual-Analytics).
