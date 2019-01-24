package apijson.demo.server.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * 状态码，分页信息，数据详情，字段描述
 * Created by zhangls on 2019/1/23.
 *
 * @author zhangls
 */
@Slf4j
@NoArgsConstructor
public class JsonResponse<T> implements Serializable {

    private static final long serialVersionUID = 8350327877975282483L;

    private static final String JSON_FORMAT_DATE = "yyyy-MM-dd HH:mm:ss";

    @Getter
    @Setter
    @JSONField(name = "success", ordinal = 1, serialize = false)
    private Boolean success;

    /**
     * 获取错误码
     */
    @Getter
    @Setter
    private String errorCode;

    @Getter
    @Setter
    private String errorMsg;

    /**
     * 是否可以查询
     */
    @Getter
    @Setter
    private Boolean hasSearch;

    /**
     * 是否是文件服务
     */
    @Getter
    @Setter
    private Boolean hasFile;

    @JSONField(format = JSON_FORMAT_DATE, ordinal = 2)
    private Date date = new Date();

    /**
     * 结果数据
     */
    @Getter
    @Setter
    private T data;

    @Getter
    @Setter
    @JSONField(name = "reqMeta")
    private JsonRequest jsonReq;


    public JsonResponse(T data) {
        this.success = Boolean.TRUE;
        this.data = data;
    }

    public JsonResponse(String errorCode, String errorMsg) {
        this.success = Boolean.FALSE;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public JsonResponse(String errorCode, String errorMsg, T data) {
        this.success = Boolean.FALSE;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonResponse response = (JsonResponse) o;

        if (success.equals(response.success)) {
            return false;
        }
        if (!errorCode.equals(response.errorCode)) {
            return false;
        }
        if (!data.equals(response.data)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = (success ? 1 : 0);
        result1 = 31 * result1 + data.hashCode();
        result1 = 31 * result1 + errorCode.hashCode();
        return result1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("success", success)
                .add("data", data)
                .add("errorCode", errorCode)
                .add("errorMsg", errorMsg)
                .omitNullValues()
                .toString();
    }
}
