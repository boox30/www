# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/murthy/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontshrink

-dontwarn com.onyx.android.sdk.ui.dialog.DialogReaderMenu
-dontwarn com.onyx.android.sdk.scribble.request.shape.PenStateChangeRequest

-dontwarn butterknife.internal.ButterKnifeProcessor

-keepnames class * {
    native <methods>;
}

-keepnames class org.apache.lucene.analysis.** { *; }

-keepnames class com.onyx.android.cropimage.data.** { *; }

-keepnames class com.onyx.android.sdk.data.PageInfo { *; }
-keepnames class com.onyx.android.sdk.utils.StringUtils { *; }

-keepnames class com.onyx.kreader.utils.GObject { *; }
-keepnames class com.onyx.kreader.reflow.ImageReflowManager { *; }
-keepnames class com.onyx.kreader.reflow.ImageReflowSettings { *; }
-keepnames class com.onyx.kreader.plugins.neopdf.NeoPdfSelection { *; }

-keepnames class com.onyx.kreader.api.ReaderTextSplitter { *; }
-keepnames class * implements com.onyx.kreader.api.ReaderTextSplitter { *; }
-keepnames class com.onyx.kreader.api.ReaderDocumentTableOfContent { *; }
-keepnames class com.onyx.kreader.api.ReaderDocumentTableOfContentEntry { *; }

-keepnames class com.onyx.kreader.host.navigation.NavigationArgs { *; }
-keepnames class com.onyx.kreader.host.navigation.NavigationList { *; }

-keepnames class com.onyx.kreader.api.ReaderSentence { *; }
-keepnames class com.onyx.kreader.utils.DeviceConfig { *; }

-keepnames class com.onyx.android.sdk.scribble.utils.** { *; }

# keep classes used by TypeConverter of dbflow
-keep class com.onyx.kreader.note.data.ReaderNotePageNameMap { *; }
-keep class com.onyx.android.sdk.scribble.data.PageNameList { *; }
-keep class com.onyx.android.sdk.scribble.data.TouchPointList { *; }
