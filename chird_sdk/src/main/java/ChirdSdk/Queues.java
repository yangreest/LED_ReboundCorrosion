package ChirdSdk;

import ChirdSdk.Apis.st_VideoFrame;

/**
 * 高性能循环队列实现
 * 用于视频帧缓冲，优化了内存分配和同步性能
 */
public class Queues {

    // 队列最大容量
    public static final int MAXQSIZE = 1024 * 100;

    private int front;
    private int rear;
    private final st_VideoFrame[] base;

    // 预分配的临时对象，用于 getQueue 返回
    private final st_VideoFrame tempFrame;

    public Queues() {
        front = 0;
        rear = 0;
        base = new st_VideoFrame[MAXQSIZE + 1];
        tempFrame = new st_VideoFrame();
    }

    /**
     * 入队 - 直接引用赋值，避免 clone() 开销
     * 注意：调用方不应在入队后修改 data 对象
     */
    public int putQueue(st_VideoFrame data) {
        if (front == (rear + 1) % MAXQSIZE) {
            return -1; // 队列满
        }

        synchronized (this) {
            base[rear] = data;
            rear = (rear + 1) % MAXQSIZE;
        }

        return 0;
    }

    /**
     * 出队 - 返回队列中的对象引用
     */
    public st_VideoFrame getQueue() {
        if (front == rear) {
            return null; // 队列空
        }

        int index;
        synchronized (this) {
            index = front;
            front = (front + 1) % MAXQSIZE;
        }

        return base[index];
    }

    /**
     * 获取队列长度
     */
    public int getLength() {
        return (rear - front + MAXQSIZE) % MAXQSIZE;
    }

    /**
     * 清空队列
     */
    public void clear() {
        synchronized (this) {
            front = 0;
            rear = 0;
        }
    }

    /**
     * 检查队列是否为空
     */
    public boolean isEmpty() {
        return front == rear;
    }

    /**
     * 检查队列是否已满
     */
    public boolean isFull() {
        return front == (rear + 1) % MAXQSIZE;
    }
}