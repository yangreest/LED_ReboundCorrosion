package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterConstants.Command;
import com.printer.sdk.PrinterInstance;

import java.util.List;

public class BluetoothPrinterUtils {

    public interface PrinterCallBack<T> {
        void onSuccess(T object);

        void onFail(String error);
    }

    public static BluetoothDevice getBluetoothDevice(String mac, boolean isShow) {
        if (TextUtils.isEmpty(mac)) {
            if (isShow) {
                ToastUtils.showShort("请先设置打印机地址");
            }
            return null;
        }
        BluetoothDevice mDevice = null;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            if (isShow) {
                ToastUtils.showShort("请启用蓝牙");
            }
            return null;
        }
        for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
            if (TextUtils.equals(mac, bluetoothDevice.getAddress())) {
                mDevice = bluetoothDevice;
                break;
            }
        }
        if (mDevice == null) {
            if (isShow) {
                ToastUtils.showShort("请去设置--蓝牙--配对 蓝牙打印机");
            }
            return null;
        }
        return mDevice;
    }

    public static void getPrinter(final Activity activity, final BluetoothDevice bthDevice, final PrinterCallBack<PrinterInstance> callBack) {
        new Thread(() -> {
            final PrinterInstance mPrinter = PrinterInstance.getPrinterInstance(bthDevice, null);
            final boolean flag = mPrinter.openConnection();
            activity.runOnUiThread(() -> {
                if (flag) {
                    callBack.onSuccess(mPrinter);
                } else {
                    callBack.onFail("无法连接蓝牙打印机，请重试");
                }
            });
        }).start();
    }

    public static void closePrinter(PrinterInstance mPrinter) {
        if (mPrinter != null) {
            mPrinter.closeConnection();
        }
    }

    public static void printNote(Activity activity, PrinterInstance mPrinter,
                                 ProjectBean projectBean, List<Bitmap> bitmapList, PrinterCallBack<String> callBack) {
        new Thread(() -> {
            mPrinter.initPrinter();
            //空2行
            mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1);
            //居中
            mPrinter.setFont(0, 0, 0, 1, 0);
            mPrinter.setPrinter(Command.ALIGN, Command.ALIGN_CENTER);
            //标题
            mPrinter.printText("全自动一体回弹仪");
            //空2行
            mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2);
            //左对齐
            mPrinter.setFont(0, 0, 0, 0, 0);
            mPrinter.setPrinter(Command.ALIGN, Command.ALIGN_LEFT);
            //设备信息
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("设备型号: ").append(activity.getString(R.string.app_name)).append("\n");
            stringBuffer.append("工程名称: ").append(projectBean.name).append("\n");
//            stringBuffer.append( "施工单位: ").append(projectBean.sgdw).append("\n");
            mPrinter.printText(stringBuffer.toString());  //打印
            stringBuffer.delete(0, stringBuffer.length());
            //画图
            for (Bitmap bitmap : bitmapList) {
                mPrinter.printColorImg2Gray(bitmap, PrinterConstants.PAlign.NONE, 0, false);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //空4行
            mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 4);
            new Handler(Looper.getMainLooper()).postDelayed(() -> callBack.onSuccess("success"), 100);
        }).start();
    }
}
