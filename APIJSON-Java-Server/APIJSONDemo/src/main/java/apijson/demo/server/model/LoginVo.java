package apijson.demo.server.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by zhangls on 2019/1/24.
 *
 * @author zhangls
 */
@Data
public class LoginVo implements Serializable {

    @JSONField(name = "userName", ordinal = 1)
    private String userName;
    @JSONField(name = "password", ordinal = 2)
    private String password;
    @JSONField(name = "verifyCode", ordinal = 3)
    private String verifyCode;
    @JSONField(name = "loginPhone", ordinal = 4)
    private String phone;
    @JSONField(name = "ipAddress", ordinal = 5)
    private String ip;
    @JSONField(name = "outTime", ordinal = 6)
    private Long time;
}
