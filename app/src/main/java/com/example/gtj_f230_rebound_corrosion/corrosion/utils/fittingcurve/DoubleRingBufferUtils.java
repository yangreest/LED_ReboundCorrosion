package com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve;

import java.util.concurrent.atomic.AtomicInteger;

public class DoubleRingBufferUtils {
    private final double[] buffer;
    private final int capacity;
    private final int mask; // 用于快速取模（要求capacity是2的幂）
    private final AtomicInteger writeIndex = new AtomicInteger(0);
    private final AtomicInteger readIndex = new AtomicInteger(0);
    private volatile int cachedSize = 0;

    public DoubleRingBufferUtils(int capacity) {
        // 确保容量是2的幂，便于位运算优化
        if ((capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("容量必须是2的幂: " + capacity);
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new double[capacity];
    }

    /**
     * 添加数据（无锁版本）
     */
    public boolean add(double value) {
        int current = writeIndex.get();
        int next = (current + 1) & mask;

        // 检查是否已满（最多落后一个位置）
        if (next == readIndex.get()) {
            return false; // 缓冲区满
        }

        buffer[current] = value;
        writeIndex.lazySet(next); // 使用lazySet减少内存屏障
        cachedSize = (next - readIndex.get()) & mask;
        return true;
    }

    /**
     * 添加数据（带CAS的版本，更安全）
     */
    public boolean addCAS(double value) {
        while (true) {
            int current = writeIndex.get();
            int next = (current + 1) & mask;

            if (next == readIndex.get()) {
                return false; // 缓冲区满
            }

            if (writeIndex.compareAndSet(current, next)) {
                buffer[current] = value;
                cachedSize = (next - readIndex.get()) & mask;
                return true;
            }
        }
    }

    /**
     * 批量添加，减少同步开销
     */
    public int addAll(double[] values) {
        return addAll(values, 0, values.length);
    }

    public int addAll(double[] values, int offset, int length) {
        int added = 0;
        for (int i = 0; i < length; i++) {
            if (add(values[offset + i])) {
                added++;
            } else {
                break;
            }
        }
        return added;
    }

    /**
     * 获取所有数据（快照）
     */
    public double[] getValues() {
        int r = readIndex.get();
        int w = writeIndex.get();
        int size = (w - r) & mask;

        if (size == 0) {
            return new double[0];
        }

        double[] result = new double[size];

        if (w > r) {
            // 数据连续
            System.arraycopy(buffer, r, result, 0, size);
        } else {
            // 数据环绕
            int firstPart = capacity - r;
            System.arraycopy(buffer, r, result, 0, firstPart);
            System.arraycopy(buffer, 0, result, firstPart, w);
        }

        return result;
    }

    /**
     * 获取当前大小
     */
    public int size() {
        return cachedSize;
    }

    public boolean isEmpty() {
        return cachedSize == 0;
    }

    public boolean isFull() {
        return cachedSize == capacity - 1;
    }

    /**
     * 清空缓冲区
     */
    public void clear() {
        readIndex.set(writeIndex.get());
        cachedSize = 0;
    }

    /**
     * 移除并返回最旧的数据
     */
    public double poll() {
        int r = readIndex.get();
        int w = writeIndex.get();

        if (r == w) {
            throw new IllegalStateException("缓冲区为空");
        }

        double value = buffer[r];
        readIndex.lazySet((r + 1) & mask);
        cachedSize = (w - ((r + 1) & mask)) & mask;
        return value;
    }

    public int getCapacity() {
        return capacity;
    }
}
