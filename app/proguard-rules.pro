# ProGuard rules for DimPulse

# Preserve Kotlinx Serialization models
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.dimpulse.app.data.model.** { *; }

# Preserve Compose runtime
-keep class androidx.compose.runtime.** { *; }
