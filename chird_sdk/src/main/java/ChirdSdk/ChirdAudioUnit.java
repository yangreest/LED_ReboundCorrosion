package ChirdSdk;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.concurrent.Semaphore;

public class ChirdAudioUnit {

    private callBack mCallbak = null;

    private int playRate = 16000;
    private int playChn = 1;
    private int playBits = 16;

    private AudioTrack mAudioTrack = null;

    public interface callBack {
        void recordCallBack(byte[] data, int length);
    }

    public ChirdAudioUnit(callBack callbak) {
        mCallbak = callbak;

        // 音频采集
        recordThreadRun = true;
        recordThread = new _audio_sampling_thread();
        recordThread.AudioRecordInit(recordRate, recordChn, recordBits);
        recordThread.start();

    }

    public void Destory() {

        mCallbak = null;

        // record thread quit
        isSampling = false;
        recordThreadRun = false;
        recordSemp.release();

        audioPlayDestory();
    }

    private void audioPlayInit(int rate, int chn, int bits) {
        int plyBufSize = 0;
        int frequency = 16000;
        int channel = AudioFormat.CHANNEL_OUT_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        if (mAudioTrack != null
                && mAudioTrack.getPlayState() == mAudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }
        frequency = rate;
        channel = chn != 1 ? AudioFormat.CHANNEL_OUT_STEREO
                : AudioFormat.CHANNEL_OUT_MONO;
        audioEncoding = bits == 8 ? AudioFormat.ENCODING_PCM_8BIT
                : AudioFormat.ENCODING_PCM_16BIT;

        plyBufSize = AudioTrack.getMinBufferSize(frequency, channel,
                audioEncoding);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                channel, audioEncoding, plyBufSize * 2, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
        playRate = rate;
        playChn = chn;
        playBits = bits;
    }

    public void audioPlayDestory() {
        synchronized (this) {
            if (mAudioTrack != null
                    && mAudioTrack.getPlayState() == mAudioTrack.PLAYSTATE_PLAYING) {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            }
        }
    }

    public void playAudioData(int rate, int chn, int bits, byte[] data,
                              int length) {
        synchronized (this) {
            if (mAudioTrack == null || rate != playRate || chn != playChn
                    || bits != playBits) {
                audioPlayInit(rate, chn, bits);
                Log.v("test", "audioPlayInit....");
            }

            if (mAudioTrack != null) {
                mAudioTrack.write(data, 0, length);
            }
        }
    }

    // audio record
    public int recordRate = 16000;
    public int recordChn = 1;
    public int recordBits = 16;

    private Boolean isSampling = false;
    private Boolean recordThreadRun = false;

    private AudioRecord mAudioRecorder = null;
    private _audio_sampling_thread recordThread = null;
    private Semaphore recordSemp = new Semaphore(0);

    // 音频采集数据大小(1s) = 采样率 x 采样位深/8 x 通道数（Bytes）: 20 * 16000 * (16 / 8) * 1
    private int recordReadSize = 640 * 5; // 1s(32000) / 20ms(640)

    public int startSampling() {

        if (mAudioRecorder == null) {
            recordThread.AudioRecordInit(recordRate, recordChn, recordBits);
        }

        if (mAudioRecorder == null) {
            return -1;
        }

        isSampling = true;
        mAudioRecorder.startRecording();
        recordSemp.release();

        return 0;
    }

    public int stopSampling() {
        if (mAudioRecorder == null) {
            recordThread.AudioRecordInit(recordRate, recordChn, recordBits);
        }

        if (mAudioRecorder == null) {
            return -1;
        }

        isSampling = false;
        mAudioRecorder.stop();

        return 0;
    }

    class _audio_sampling_thread extends Thread {

        private int length = 0;

        public void AudioRecordInit(int rate, int chn, int bits) {

            int audioformat = AudioFormat.ENCODING_PCM_16BIT;
            if (bits == 8) {
                audioformat = AudioFormat.ENCODING_PCM_8BIT;
            }

            int minBuffSize = AudioRecord.getMinBufferSize(rate, chn,
                    audioformat);
            mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    rate, chn, audioformat, minBuffSize * 3);
        }

        public void run() {

            byte[] buffer = new byte[64000];

            while (recordThreadRun) {
                try {
                    recordSemp.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }

                if (mAudioRecorder == null) {
                    AudioRecordInit(recordRate, recordChn, recordBits);
                }

                while (isSampling) {
                    if (mAudioRecorder == null) {
                        break;
                    }

                    length = mAudioRecorder.read(buffer, 0, recordReadSize);
                    if (mCallbak != null && length > 0) {
                        mCallbak.recordCallBack(buffer, length);
                    }
                } /* end while (isSampling) */
            } /* end while (recordThreadRun) */
        } /* end public void run() */
    }/* end class audioRecordThread extends Thread */

}
