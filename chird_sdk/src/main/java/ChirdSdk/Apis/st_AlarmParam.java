package ChirdSdk.Apis;

public class st_AlarmParam {

    public static final int ALARM_MDCOND_DISABLE = 0;    // 不报警
    public static final int ALARM_MDCOND_DYNAMIC = 1;    // 动时报警
    public static final int ALARM_MDCOND_STATIC = 2;    // 静时报警

    public static final int ALARMCOND_ALL_DATY = 0;    // 全天报警
    public static final int ALARMCOND_CUSTOM_DATY = 1;    // 自定义时间段


    public byte enable = 0;        // 报警使能，必须打开其他参数才有效
    public byte period = 0;        // 报警周期  (1 ~ 60分)

    public byte mdcond = 0;        // 报警检测条件(见上面的宏定义)

    public byte alarmcond = 0;    // 报警时间段设置(见上面的宏定义)

    // 自定义报警时间段 (最大设置10个)
    public byte[] times_enable = new byte[10];
    public byte[] times_sh = new byte[10];
    public byte[] times_sm = new byte[10];
    public byte[] times_eh = new byte[10];
    public byte[] times_em = new byte[10];

}
