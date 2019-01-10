package apijson.demo.server.model;

import com.zhangls.apijson.annotation.MethodAccess;

import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.CONTACT;
import static com.zhangls.apijson.base.model.RequestRole.LOGIN;
import static com.zhangls.apijson.base.model.RequestRole.OWNER;
import static com.zhangls.apijson.base.model.RequestRole.UNKNOWN;


/**
 * 登录日志
 *
 * @author Lemon
 */
@SuppressWarnings("serial")
@MethodAccess(
        GET = {},
        HEAD = {},
        GETS = {UNKNOWN, LOGIN, CONTACT, OWNER, ADMIN},
        HEADS = {UNKNOWN, LOGIN, CONTACT, OWNER, ADMIN},
        POST = {ADMIN},
        PUT = {ADMIN},
        DELETE = {ADMIN}
)
public class Login extends BaseModel {

    /**
     * 密码登录
     */
    public static final int TYPE_PASSWORD = 0;

    /**
     * 验证码登录
     */
    public static final int TYPE_VERIFY = 1;

    private Integer type;

    public Login() {
        super();
    }

    public Login(long userId) {
        this();
        setUserId(userId);
    }


    public Integer getType() {
        return type;
    }

    public Login setType(Integer type) {
        this.type = type;
        return this;
    }

}
