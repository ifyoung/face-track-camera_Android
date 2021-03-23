package face.camera.beans.net.modelCom;


public class BusMsg {
    private String netState;
    private String webProgress;//网页加载进度
    private String qrCodeString;//二维码
    private String pushMsg;//推送消息

    public String getWebProgress() {
        return webProgress;
    }

    public void setWebProgress(String webProgress) {
        this.webProgress = webProgress;
    }


    public String getQrCodeString() {
        return qrCodeString;
    }

    public void setQrCodeString(String qrCodeString) {
        this.qrCodeString = qrCodeString;
    }

    public String getNetState() {
        return netState;
    }

    public void setNetState(String netState) {
        this.netState = netState;
    }

    public String getPushMsg() {
        return pushMsg;
    }

    public void setPushMsg(String pushMsg) {
        this.pushMsg = pushMsg;
    }
}
