package apijson.demo.server.common;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Created by zhangls on 2019/1/23.
 *
 * @author zhangls
 */
@Slf4j
@NoArgsConstructor
public class JsonResponse<T extends Response> implements Serializable {

    private static final long serialVersionUID = 8350327877975282483L;

    @Getter
    @Setter
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

    /**
     * 结果数据
     */
    @Getter
    @Setter
    private T result;


    public JsonResponse(T result) {
        this.success = Boolean.TRUE;
        this.result = result;
    }

    public JsonResponse(String errorCode, String errorMsg) {
        this.success = Boolean.FALSE;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public JsonResponse(String errorCode, String errorMsg, T result) {
        this.success = Boolean.FALSE;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.result = result;
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
        if (!result.equals(response.result)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = (success ? 1 : 0);
        result1 = 31 * result1 + result.hashCode();
        result1 = 31 * result1 + errorCode.hashCode();
        return result1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("success", success)
                .add("result", result)
                .add("errorCode", errorCode)
                .add("errorMsg", errorMsg)
                .omitNullValues()
                .toString();
    }
}
