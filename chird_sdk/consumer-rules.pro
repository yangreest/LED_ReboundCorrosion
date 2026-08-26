# ChirdSdk Consumer ProGuard Rules
# 使用此库的应用会自动应用这些规则

# 保持 ChirdSdk API
-keep class ChirdSdk.** { *; }
-keep interface ChirdSdk.** { *; }

# 保持 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}
