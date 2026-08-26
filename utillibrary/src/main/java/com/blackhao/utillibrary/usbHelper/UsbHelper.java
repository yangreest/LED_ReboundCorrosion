package com.blackhao.utillibrary.usbHelper;

import static com.blackhao.utillibrary.usbHelper.USBBroadCastReceiver.ACTION_USB_PERMISSION;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.text.TextUtils;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.github.mjdev.libaums.partition.Partition;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * USB 操作工具类
 */
public class UsbHelper {
    //上下文对象（弱引用防止内存泄漏）
    private WeakReference<Context> contextRef;
    //USB 设备列表
    private UsbMassStorageDevice[] storageDevices;
    //当前使用的设备
    private UsbMassStorageDevice currentDevice;
    //USB 广播
    private USBBroadCastReceiver mUsbReceiver;
    //回调
    private USBBroadCastReceiver.UsbListener usbListener;
    //根目录
    public UsbFile rootFolder = null;
    //是否已注册广播
    private boolean isReceiverRegistered = false;

    public UsbHelper(Context context, USBBroadCastReceiver.UsbListener usbListener) {
        this.contextRef = new WeakReference<>(context);
        this.usbListener = usbListener;
        registerReceiver();  //注册广播
    }

    /**
     * 注册 USB 监听广播
     */
    private void registerReceiver() {
        Context context = getContext();
        if (context == null || isReceiverRegistered) {
            return;
        }
        mUsbReceiver = new USBBroadCastReceiver();
        mUsbReceiver.setUsbListener(usbListener);
        //监听otg插入 拔出
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);
        isReceiverRegistered = true;
    }

    /**
     * 获取 Context
     */
    private Context getContext() {
        return contextRef != null ? contextRef.get() : null;
    }

    /**
     * 读取 USB设备列表
     *
     * @return USB设备列表
     */
    public UsbMassStorageDevice[] getDeviceList() {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            return null;
        }
        //获取存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(context);
        //创建 PendingIntent，兼容 Android 12+ (API 31)
        int flags;
        if (Build.VERSION.SDK_INT >= 31) {
            flags = PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), flags);
        //可能有几个 一般只有一个 因为大部分手机只有1个otg插口
        for (UsbMassStorageDevice device : storageDevices) {
            //有就直接读取设备是否有权限
            if (!usbManager.hasPermission(device.getUsbDevice())) {
                //没有权限请求权限
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        return storageDevices;
    }

    /**
     * 获取device 根目录文件
     *
     * @param device USB 存储设备
     */
    public void readDevice(UsbMassStorageDevice device) {
        try {
            //初始化
            device.init();
            //记录当前设备，用于后续关闭
            currentDevice = device;
            //获取partition
            Partition partition = device.getPartitions().get(0);
            FileSystem currentFs = partition.getFileSystem();
            //获取根目录
            rootFolder = currentFs.getRootDirectory();
        } catch (Exception e) {
            e.printStackTrace();
            //发生异常时关闭设备
            closeDevice(device);
        }
    }

    /**
     * 关闭指定的 USB 设备
     *
     * @param device USB 存储设备
     */
    public void closeDevice(UsbMassStorageDevice device) {
        if (device != null) {
            try {
                device.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭当前使用的 USB 设备
     */
    public void closeCurrentDevice() {
        if (currentDevice != null) {
            closeDevice(currentDevice);
            currentDevice = null;
            rootFolder = null;
        }
    }

    /**
     * 保存数据到文件
     *
     * @param fileName：文件名
     * @param data：数据
     * @return ：文件地址
     */
    public String saveDataToUsb(String fileName, byte[] data) {
        String[] strArray = fileName.split("/");
        UsbFile usbFile = null;
        if (strArray.length == 1) {
            usbFile = rootFolder;
        } else {
            if (rootFolder != null) {
                for (int i = 0; i < strArray.length - 1; i++) {
                    if (i == 0) {
                        usbFile = getSubdirectory(rootFolder, strArray[i]);
                    } else {
                        usbFile = getSubdirectory(usbFile, strArray[i]);
                    }
                }
            }
        }
        String filePath = null;
        if (usbFile != null) {
            deleteFolder(usbFile, strArray[strArray.length - 1]);
            UsbFileOutputStream uos = null;
            try {
                UsbFile fileUsbFile = usbFile.createFile(strArray[strArray.length - 1]);
                uos = new UsbFileOutputStream(fileUsbFile);
                uos.write(data);
                uos.flush();
                filePath = fileUsbFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (uos != null) {
                    try {
                        uos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filePath;
    }

    /**
     * 退出 UsbHelper
     */
    public void finishUsbHelper() {
        //关闭当前设备
        closeCurrentDevice();
        //取消注册广播
        if (isReceiverRegistered) {
            Context context = getContext();
            if (context != null && mUsbReceiver != null) {
                context.unregisterReceiver(mUsbReceiver);
            }
            isReceiverRegistered = false;
        }
        usbListener = null;
        contextRef = null;
        storageDevices = null;
        rootFolder = null;
        mUsbReceiver = null;
        currentDevice = null;
    }

    private void deleteFolder(UsbFile usbFile, String folderName) {
        try {
            List<UsbFile> usbRootFiles = new ArrayList<>();
            Collections.addAll(usbRootFiles, usbFile.listFiles());
            for (UsbFile file : usbRootFiles) {
                if (TextUtils.equals(folderName, file.getName())) {
                    file.delete();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UsbFile getSubdirectory(UsbFile usbFile, String folderName) {
        UsbFile mUsbFile = null;
        try {
            List<UsbFile> usbRootFiles = new ArrayList<>();
            Collections.addAll(usbRootFiles, usbFile.listFiles());
            for (UsbFile file : usbRootFiles) {
                if (file.isDirectory() && TextUtils.equals(folderName, file.getName())) {
                    mUsbFile = file;
                    break;
                }
            }
            if (mUsbFile == null) {
                mUsbFile = usbFile.createDirectory(folderName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mUsbFile;
    }
}
