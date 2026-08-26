package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限工具类
 * 封装权限申请逻辑，简化权限处理流程
 * <p>
 * 适用场景：
 * - 存储路径：/sdcard/AppData/
 * - 蓝牙功能（需要位置权限用于蓝牙搜索）
 * - 摄像头拍照功能
 * - 不上架应用市场（可使用 MANAGE_EXTERNAL_STORAGE）
 * - 兼容 Android 9+ (API 28+)，targetSdk 35
 */
public final class PermissionUtils {

    private PermissionUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 权限授权回调接口
     */
    public interface GrantedCallback {
        void onGranted();
    }

    /**
     * 权限提示信息
     */
    private static final String PERMISSION_RATIONALE = "应用需要以下权限才能正常运行：\n\n" + "• 位置信息：用于蓝牙设备搜索和连接\n" + "• 存储空间：用于保存数据到 /sdcard/AppData/ 目录\n" + "• 相机：用于拍照功能\n" + "• 蓝牙：用于蓝牙设备连接";

    /**
     * 获取所有需要申请的权限（根据系统版本动态适配）
     * <p>
     * Android 版本权限说明：
     * - Android 9-10 (API 28-29): READ/WRITE_EXTERNAL_STORAGE
     * - Android 11 (API 30): MANAGE_EXTERNAL_STORAGE (特殊权限，需跳转设置)
     * - Android 12 (API 31-32): MANAGE_EXTERNAL_STORAGE + BLUETOOTH_SCAN/CONNECT
     * - Android 13+ (API 33+): MANAGE_EXTERNAL_STORAGE (不再需要 READ_MEDIA_*)
     */
    private static String[] getAllPermissions() {
        List<String> permissions = new ArrayList<>();

        // 位置权限（蓝牙搜索需要，Android 12 以下必须）
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        // 相机权限
        permissions.add(Manifest.permission.CAMERA);

        // 存储权限：Android 11+ 使用 MANAGE_EXTERNAL_STORAGE（单独处理）
        // Android 9-10 需要传统存储权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // 蓝牙权限：Android 12+ 需要新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        return permissions.toArray(new String[0]);
    }

    /**
     * 检查所有权限是否已授权
     */
    private static boolean isAllPermissionsGranted(Context context) {
        // Android 11+ 需要检查 MANAGE_EXTERNAL_STORAGE 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                return false;
            }
        }
        return com.blankj.utilcode.util.PermissionUtils.isGranted(getAllPermissions());
    }

    /**
     * 申请所有权限（位置、存储、相机、蓝牙）
     *
     * @param context         上下文
     * @param grantedCallback 授权成功回调
     */
    public static void initPermission(Context context, GrantedCallback grantedCallback) {
        if (isAllPermissionsGranted(context)) {
            if (grantedCallback != null) {
                grantedCallback.onGranted();
            }
            return;
        }

        // Android 11+ 需要先检查 MANAGE_EXTERNAL_STORAGE 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showManageStorageDialog(context, grantedCallback);
                return;
            }
        }

        doRequestPermissions(context, grantedCallback);
    }

    /**
     * 显示所有文件访问权限对话框（Android 11+）
     * 用于访问 /sdcard/AppData/ 目录
     */
    @android.annotation.SuppressLint("InlinedApi")
    private static void showManageStorageDialog(Context context, GrantedCallback grantedCallback) {
        StyleAlertDialog dialog = new StyleAlertDialog(context);
        dialog.setContent("应用需要访问所有文件的权限来保存数据到 /sdcard/AppData/ 目录，请在设置中开启\"允许访问管理所有文件\"权限。").setLeftButton("取消", v -> {
            dialog.dismiss();
        }).setRightButton("去设置", v -> {
            dialog.dismiss();
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                Logger.e("跳转管理文件权限失败: " + e.getMessage());
                // 如果无法跳转到具体应用设置，则跳转到通用设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                context.startActivity(intent);
            }
            // 用户从设置返回后，检查权限并执行回调
            if (grantedCallback != null) {
                grantedCallback.onGranted();
            }
        }).show();
    }

    /**
     * 执行权限申请
     */
    private static void doRequestPermissions(Context context, GrantedCallback grantedCallback) {
        com.blankj.utilcode.util.PermissionUtils.permission(getAllPermissions()).rationale((activity, shouldRequest) -> showRationaleDialog(context, shouldRequest)).callback(new com.blankj.utilcode.util.PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> permissionsGranted) {
                Logger.d("权限申请成功: " + permissionsGranted);
                if (grantedCallback != null) {
                    grantedCallback.onGranted();
                }
            }

            @Override
            public void onDenied(@NonNull List<String> permissionsDeniedForever, @NonNull List<String> permissionsDenied) {
                Logger.e("权限被拒绝: " + permissionsDeniedForever + ", " + permissionsDenied);
                if (!permissionsDeniedForever.isEmpty()) {
                    showSettingsDialog(context);
                }
            }
        }).request();
    }

    /**
     * 显示权限说明对话框
     */
    private static void showRationaleDialog(Context context, com.blankj.utilcode.util.PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest) {
        StyleAlertDialog dialog = new StyleAlertDialog(context);
        dialog.setContent(PERMISSION_RATIONALE).setLeftButton("取消", v -> {
            dialog.dismiss();
            shouldRequest.again(false);
        }).setRightButton("继续", v -> {
            dialog.dismiss();
            shouldRequest.again(true);
        }).show();
    }

    /**
     * 显示前往设置对话框
     */
    private static void showSettingsDialog(Context context) {
        StyleAlertDialog dialog = new StyleAlertDialog(context);
        dialog.setContent("部分权限被永久拒绝，请前往设置页面手动开启。").setLeftButton("取消", v -> dialog.dismiss()).setRightButton("去设置", v -> {
            dialog.dismiss();
            com.blankj.utilcode.util.PermissionUtils.launchAppDetailsSettings();
        }).show();
    }
}
