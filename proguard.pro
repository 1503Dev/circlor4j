# ============================================================
# Circlor4J ProGuard Rules
# Fabric Mixin Mod - Obfuscation Configuration
# ============================================================

-injars 'D:\1\projects\circlor4j\build\libs\circlor4j-26.2.0.2.jar'
-outjars 'D:\1\projects\circlor4j\build\libs\circlor4j-26.2.0.2-proguarded.jar'

-libraryjars 'D:\1\projects\circlor4j\libs\minecraft.jar'
-libraryjars 'C:\Program Files\Java\jdk-25.0.4.7-hotspot\lib\jrt-fs.jar'
-libraryjars 'C:\Program Files\Java\jdk-21\jmods\java.base.jmod'
-printmapping 'D:\1\projects\circlor4j\mapping.txt'
-dontshrink
-dontoptimize

-keep class !dev1503.** { *; }

# Keep the Fabric entry point class with its interface hierarchy
-keep class dev1503.circlor4j.Circlor4J {
    *;
}
-keep class net.fabricmc.** {
    *;
}
-keep interface net.fabricmc.** {
    *;
}

# Keep all Mixin classes - they are referenced by name in JSON configs
-keep @org.spongepowered.asm.mixin.Mixin class * {
    @org.spongepowered.asm.mixin.Shadow <methods>;
    @org.spongepowered.asm.mixin.Shadow <fields>;
    @org.spongepowered.asm.mixin.gen.Accessor <methods>;
    @org.spongepowered.asm.mixin.gen.Invoker <methods>;
    @org.spongepowered.asm.mixin.injection.Inject <methods>;
    @org.spongepowered.asm.mixin.injection.ModifyArg <methods>;
    @org.spongepowered.asm.mixin.injection.ModifyArgs <methods>;
    @org.spongepowered.asm.mixin.injection.ModifyVariable <methods>;
    @org.spongepowered.asm.mixin.injection.ModifyConstant <methods>;
    @org.spongepowered.asm.mixin.injection.Redirect <methods>;
    @org.spongepowered.asm.mixin.Overwrite <methods>;
    @org.spongepowered.asm.mixin.Final <fields>;
    @org.spongepowered.asm.mixin.Unique <fields>;
    @org.spongepowered.asm.mixin.Unique <methods>;
    <init>(...);
}

# Keep Mixin interfaces (Accessors)
-keep @org.spongepowered.asm.mixin.Mixin interface * {
    @org.spongepowered.asm.mixin.gen.Accessor <methods>;
    @org.spongepowered.asm.mixin.gen.Invoker <methods>;
    <init>(...);
}

# Keep all classes in the mixin packages
-keep class dev1503.circlor4j.mixin.** { *; }
-keep class dev1503.circlor4j.client.mixin.** { *; }

# Keep Mixin annotation classes themselves
-keep class org.spongepowered.asm.mixin.** { *; }
-keep class org.spongepowered.asm.mixin.gen.** { *; }
-keep class org.spongepowered.asm.mixin.injection.** { *; }
-keep class org.spongepowered.asm.mixin.injection.callback.** { *; }
-keep class org.spongepowered.asm.mixin.injection.invoke.arg.** { *; }
-keep class org.spongepowered.asm.mixin.injection.points.** { *; }
-keep class org.spongepowered.asm.mixin.injection.struct.** { *; }
-keep class org.spongepowered.asm.mixin.transformer.** { *; }
-keep class org.spongepowered.asm.mixin.throwables.** { *; }
-keep class org.spongepowered.asm.util.** { *; }
-keep class org.spongepowered.asm.util.asm.** { *; }
-keep class org.spongepowered.asm.util.perf.** { *; }
-keep class org.spongepowered.tools.obfuscation.** { *; }

# Keep all annotations on Mixin classes (critical for Mixin processor)
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations,AnnotationDefault,EnclosingMethod,InnerClasses,Signature,Exceptions,MethodParameters

# Keep classes referenced by fabric.mod.json
-keep class dev1503.circlor4j.Circlor4J {
    *;
}
-keepclassmembers public class dev1503.circlor4j.Circlor4J {
    public *;
    protected *;
}

# Obfuscate members of non-mixin classes (keep only public API surface)
-keepclassmembers class dev1503.circlor4j.client.module.Module {
    public *;
}
-keepclassmembers class dev1503.circlor4j.client.module.Module$* {
    public *;
}
-keepclassmembers class dev1503.circlor4j.client.module.ModuleCategory {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class dev1503.circlor4j.client.module.ModuleManager {
    public *;
}

-keepclassmembers class dev1503.circlor4j.client.module.modules.** {
    public <init>(...);
    public static *;
}

-keep class dev1503.circlor4j.client.Circlor4jClient {
    public *;
}

-keepclassmembers class dev1503.circlor4j.ui.StatusManager {
    public *;
}
-keepclassmembers class dev1503.circlor4j.ui.StatusManager$Listener {
    public *;
}

-keepclassmembers class dev1503.circlor4j.client.config.** {
    public *;
}

-keepclassmembers class dev1503.circlor4j.client.keybind.** {
    public *;
}

-keepclassmembers class dev1503.circlor4j.i18n.** {
    public *;
}

-keepclassmembers class dev1503.circlor4j.ui.** {
    public *;
}

-keepclassmembers class dev1503.circlor4j.client.render.** {
    public *;
}

-keepclassmembers class dev1503.circlor4j.client.util.** {
    public *;
}

-keepclassmembers class dev1503.circlor4j.Icon {
    public *;
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

# Keep classes with @SubscribeEvent or similar annotations
-keepclassmembers class * {
    @net.fabricmc.fabric.api.event.Event <fields>;
}

# Keep all public/protected methods that could be Mixin targets
-keepclassmembers class * {
    @org.spongepowered.asm.mixin.injection.* <methods>;
    @org.spongepowered.asm.mixin.gen.* <methods>;
    @org.spongepowered.asm.mixin.Shadow <fields>;
    @org.spongepowered.asm.mixin.Shadow <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Fabric interfaces and their implementations
-keep class net.fabricmc.** {
    *;
}
-keep interface net.fabricmc.** {
    *;
}
-keep class * implements net.fabricmc.api.** {
    *;
}

# Keep CallbackInfo and CallbackInfoReturnable
-keep class org.spongepowered.asm.mixin.injection.callback.CallbackInfo { *; }
-keep class org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable { *; }
-keep class org.spongepowered.asm.mixin.injection.callback.CallbackInfoCancelable { *; }

# Don't warn about missing Mixin dependencies
-dontwarn org.spongepowered.asm.**
-dontwarn org.spongepowered.tools.**

# Don't warn about Minecraft/Fabric classes (they're external)
-dontwarn net.minecraft.**
-dontwarn net.fabricmc.**

# Optimizations
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''

-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn java.lang.invoke.MethodHandle
-dontwarn java.lang.invoke.MethodHandles
-dontwarn java.lang.invoke.MethodHandles$Lookup
-dontwarn java.lang.Class
-dontwarn java.lang.Object
-dontwarn java.lang.String
-dontwarn java.lang.Enum
-dontwarn java.lang.Record
-dontwarn java.lang.**
-dontwarn java.lang.AutoCloseable
-dontwarn java.**
-dontwarn com.mojang.blaze3d.**
-dontwarn org.**
-dontwarn com.google.**
-dontwarn it.unimi.dsi.fastutil.**
-dontwarn dev1503.circlor4j.**
-dontnote **

-keepattributes *Annotation*
-keep class com.google.gson.annotations.SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}