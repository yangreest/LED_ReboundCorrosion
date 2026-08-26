package ChirdSdk.Apis;

public class st_StorageInfo {

    // record information getRecordParam() 函数获取
    public int record_type = 0; // 0:不录像 1: 连接录像 2: 条件录像
    public int record_cond = 0; // 条件录像下，根据动态帧率录像(0录像还是1录像)
    public int segmenttime = 0; // 录像时间:一段录像文件保存多长时间(分)
    public int boverylay = 0; // 是否覆盖

    // sd card information getSdCardInfo() 获取
    public int isexist = 0; // 是否存在 SD 卡
    public String totalspace = ""; // 总存储空间
    public String availablespace = ""; // 可用存储空间
}
