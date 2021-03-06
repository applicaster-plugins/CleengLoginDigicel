# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

## Android Coroutines specific rules ##
# ServiceLoader support
-keepnames class kotlinx.coroutines.experimental.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.experimental.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.experimental.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.experimental.android.AndroidDispatcherFactory {}
# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

## GSON specific rules ##
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
#-keepattributes *Annotation*
-keepattributes EnclosingMethod
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

## OkHttp specific rules ##
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class com.applicaster.cleeng.CleengLoginPlugin { *; }

## Retrofit 2.X specific rules ##
# https://square.github.io/retrofit/
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okio.**
-keepattributes *Annotation*,Signature

-keepclasseswithmembers, allowobfuscation class * {
    @retrofit2.http.* <methods>;
}
-repackageclasses com.applicaster.cleeng