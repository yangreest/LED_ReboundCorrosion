package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class OptimizedDataProcessor {
    private static final String TAG = "DataProcessor";
    //锁
    private final Object lock = new Object();
    //线程安全的阻塞队列
    private final LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>(2000);
    //处理线程的状态
    private volatile boolean isProcessing;
    //正在被处理的数据
    private byte[] currentByte;
    //数据回调
    private final DataProcessor dataProcessor;

    public interface DataProcessor {
        //子线程
        void processData(int displacement, int signal, String frameData);
    }

    public OptimizedDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    /**
     * 使用生产者-消费者模式处理高频率数据
     *
     * @param data：接收蓝牙帧数据
     */
    public void receiveDataOptimized(byte[] data) {
        // 非阻塞方式添加数据（如果队列已满，立即返回 false，不会等待空间可用）
        boolean offered = dataQueue.offer(data);
        if (!offered) {
            return;
        }
        // 确保只有一个处理线程在运行
        synchronized (lock) {
            if (!isProcessing) {
                isProcessing = true;
                startProcessingThread();
            }
        }
    }

    /**
     * 开启线程处理数据
     */
    private void startProcessingThread() {
        new Thread(() -> {
            try {
                while (true) {
                    //取数据
                    byte[] data = dataQueue.poll(10, TimeUnit.MILLISECONDS);
                    //
                    if (data == null) {
                        synchronized (lock) {
                            //空队列时跳出循环
                            if (dataQueue.isEmpty()) {
                                isProcessing = false;
                                break;
                            }
                        }
                    } else {
                        treatData(data);  //拼接及处理数据
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 拼接及处理数据
     *
     * @param data：待拼接的数据
     */
    private void treatData(byte[] data) {
        //拼接
        currentByte = ByteArrayUtils.concat(currentByte, data);
        // 检查数据完整性
        int index = -1;
        for (int i = 0; i < currentByte.length - 1; i++) {
            if (currentByte[i] == (byte) 0xF1 && currentByte[i + 1] == (byte) 0x00) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            currentByte = ByteArrayUtils.subarrayFrom(currentByte, 1);
            for (int i = 0; i < currentByte.length - 1; i++) {
                if (currentByte[i] == (byte) 0xF1 && currentByte[i + 1] == (byte) 0x00) {
                    index = i;
                    break;
                }
            }
        }
        if (index > 0) {
            currentByte = ByteArrayUtils.subarrayFrom(currentByte, index);
        }
        //
        //分段解析
        //F1 00 01 47 02 6C FF
        int size = currentByte.length / 7;
        int last = currentByte.length % 7;
        for (int i = 0; i < size; i++) {
            processData(ByteArrayUtils.subarrayLength(currentByte, i * 7, 7));
        }
        //剩余
        currentByte = ByteArrayUtils.subarrayFrom(currentByte, currentByte.length - last);
    }

    /**
     * 解析
     *
     * @param data：帧数据
     */
    private void processData(byte[] data) {
        String[] array = StringUtils.bytesToHexStrArray(data);
        //位移
        int displacement = Integer.parseInt(array[4] + array[5], 16);
        //信号
        int signal = Integer.parseInt(array[1] + array[2] + array[3], 16);
        //
        if (signal > 30000) {
            return;
        }
        dataProcessor.processData(displacement, signal, Arrays.toString(array));
    }
}
