# ========================================
# ChirdSdk ProGuard Rules
# ========================================

# 保持所有 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持 ChirdSdk 所有公共 API
-keep class ChirdSdk.** { *; }
-keep interface ChirdSdk.** { *; }

# 保持回调接口
-keep class * implements ChirdSdk.ClientCallBack { *; }

# 保持 native 回调类
-keepclassmembers class * {
    public void *(...);
}

# 保持 Parcelable 序列化
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# 保持 Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 移除日志（Release 构建）
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}