package apijson.demo.server.common;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.MediaType;

/**
 * @author zhangls
 */
@ToString
public enum RespCode {
    /****************业务异常代码***********************************************************************************/
    /*** 系统繁忙*/
    BUSS_BUSY_ERR("999999", "系统繁忙，请稍后再试。"),
    /*** 系统繁忙*/
    BUSS_SUCCESS("000000", "业务成功"),
    /*************************************************************************************************************/

    /****************校验异常代码***********************************************************************************/
    /*** 校验错误*/
    ERROR_JSON_FORMAT("000100", "请求JSON格式有误"),
    ERROR_CONTENT_TYPE("000101", "请求Content-type格式有误,必须为【" + MediaType.APPLICATION_JSON_UTF8_VALUE + "】"),
    ERROR_ACCEPT("000102", "请求Accept格式有误,必须为【" + MediaType.APPLICATION_JSON_UTF8_VALUE + "】"),
    /*************************************************************************************************************/

    /****************生成token必填字段******************************************************************************/
    TOKEN_USERNAME_NULL("000200", "生成token时，参数userName不能为空"),
    TOKEN_PASSWORD_NULL("000201", "生成token时，参数password不能为空"),
    TOKEN_IP_NULL("000202", "生成token时，参数ip不能为空"),
    TOKEN_WITHOUT_USER("000203", "用户或密码有误，请重新输入"),
    TOKEN_GET_ERROR("000204", "请求头中Authorization获取JWT-TOKEN为空或者不符合要求"),
    TOKEN_OUT_TIME_ERROR("000205", "获取JWT-TOKEN已超时，不符合要求"),
    TOKEN_PARSE_ERROR("000206", "获取JWT-TOKEN解析错误，已被篡改，不符合要求"),
    TOKEN_ERROR("000207", "获取JWT-TOKEN未知错误，不符合要求");
    /*************************************************************************************************************/

    @Getter
    @Setter
    private String resCode;
    @Getter
    @Setter
    private String resDesc;

    private RespCode(String resCode, String resDesc) {
        this.resCode = resCode;
        this.resDesc = resDesc;
    }

    public static RespCode from(String code) {
        for (RespCode respCode : RespCode.values()) {
            if (Objects.equal(respCode.getResCode(), code)) {
                return respCode;
            }
        }
        return null;
    }

    public static String getRespCodeAndDesc(RespCode respCode) {
        return respCode.getResCode() + respCode.getResDesc();
    }

    public static String getRespCodeAndDesc(RespCode respCode, String respDesc) {
        return respCode.getResCode() + ":" + respDesc;
    }

}
