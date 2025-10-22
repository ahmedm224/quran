# Add project specific ProGuard rules here.

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclassmembers class * extends dagger.hilt.android.lifecycle.HiltViewModel {
    <init>(...);
}

# Keep MediaBrowserService
-keep class * extends androidx.media.MediaBrowserServiceCompat { *; }
-keep class androidx.media.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Protobuf
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Keep app data models
-keep class com.quranmedia.player.data.api.model.** { *; }
-keep class com.quranmedia.player.domain.model.** { *; }
-keep class com.quranmedia.player.data.database.entity.** { *; }

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
