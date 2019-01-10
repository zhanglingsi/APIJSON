package apijson.demo.server.model;

import com.zhangls.apijson.annotation.MethodAccess;

import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.CONTACT;
import static com.zhangls.apijson.base.model.RequestRole.LOGIN;
import static com.zhangls.apijson.base.model.RequestRole.OWNER;
import static com.zhangls.apijson.base.model.RequestRole.UNKNOWN;
import static com.zhangls.apijson.base.model.RequestRole.CIRCLE;

/**
 * 验证码
 *
 * @author Lemon
 */
@MethodAccess(
        GET = {},
        HEAD = {},
        GETS = {UNKNOWN, LOGIN, CONTACT, CIRCLE, OWNER, ADMIN},
        HEADS = {UNKNOWN, LOGIN, CONTACT, CIRCLE, OWNER, ADMIN},
        POST = {UNKNOWN, LOGIN, CONTACT, CIRCLE, OWNER, ADMIN},
        PUT = {ADMIN},
        DELETE = {ADMIN}
)
public class Verify extends BaseModel {
    private static final long serialVersionUID = 1L;

    /**
     * 登录
     */
    public static final int TYPE_LOGIN = 0;

    /**
     * 注册
     */
    public static final int TYPE_REGISTER = 1;

    /**
     * 登录密码
     */
    public static final int TYPE_PASSWORD = 2;

    /**
     * 支付密码
     */
    public static final int TYPE_PAY_PASSWORD = 3;

    /**
     * 手机
     */
    private String phone;

    /**
     * 验证码
     */
    private String verify;

    /**
     * 验证类型
     */
    private Integer type;

    public Verify() {
        super();
    }

    /**
     * type和phone为联合主键，必传
     *
     * @param type
     * @param phone
     */
    public Verify(int type, String phone) {
        this();
        setType(type);
        setPhone(phone);
    }


    public String getVerify() {
        return verify;
    }

    public Verify setVerify(String verify) {
        this.verify = verify;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Verify setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public Verify setType(Integer type) {
        this.type = type;
        return this;
    }

}
