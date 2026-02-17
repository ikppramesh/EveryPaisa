# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.everypaisa.tracker.data.entity.** { *; }

# Keep parser-core models
-keep class com.everypaisa.parser.core.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
