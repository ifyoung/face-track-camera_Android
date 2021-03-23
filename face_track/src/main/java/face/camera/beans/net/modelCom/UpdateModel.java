package face.camera.beans.net.modelCom;



/**
 * 更新
 * <p>
 * {
 * "status" : 200,
 * "message" : "操作成功",
 * "data" : {
 * "id" : 8,
 * "appVersion" : "1.5",
 * "zipSize" : "117476",
 * "url" : "/ufile/20190719/c593eae082484ff69a68eeafefc9f944.zip",
 * "pt" : "ios,android",
 * "dev" : true,
 * "createDate" : 1563542989000,
 * "uid" : 1,
 * "projectId" : 1,
 * "del" : false,
 * "dateFromate" : "2019-07-19",
 * "tokens" : null
 * },
 * "tag" : null
 * }
 */
public class UpdateModel {
    private int id;//版本号
    private String appVersion;
    private String zipSize;
    private String url;
    private String pt;
    private String dev;
    private String createDate;
    private String uid;
    private String projectId;
    private String del;
    private String dateFromate;
    private String tokens;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getZipSize() {
        return zipSize;
    }

    public void setZipSize(String zipSize) {
        this.zipSize = zipSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPt() {
        return pt;
    }

    public void setPt(String pt) {
        this.pt = pt;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDel() {
        return del;
    }

    public void setDel(String del) {
        this.del = del;
    }

    public String getDateFromate() {
        return dateFromate;
    }

    public void setDateFromate(String dateFromate) {
        this.dateFromate = dateFromate;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }
}
