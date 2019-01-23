package apijson.demo.server.common;

import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.User;
import apijson.demo.server.model.Verify;
import com.zhangls.apijson.base.JsonApiRequest;

/**
 * Created by zhangls on 2019/1/2.
 *
 * @author zhangls
 */
public interface UtilConstants {

    /**
     * 公共常量
     */
    class Public {
        public static final String VERIFY_ = Verify.class.getSimpleName();
        public static final String USER_ = User.class.getSimpleName();
        public static final String PRIVACY_ = Privacy.class.getSimpleName();
    }

    /**
     * 登陆相关
     */
    class Login {
        public static final String VERSION = JsonApiRequest.KEY_VERSION;
        public static final String FORMAT = JsonApiRequest.KEY_FORMAT;
        public static final String USER_ID = "userId";
        public static final String PHONE = "phone";
        public static final String PASS_WORD = "password";
        public static final String TYPE = "type";
        public static final String REMEMBER = "remember";
        /**
         * 密码登陆
         */
        public static final Integer LOGIN_TYPE_PASSWORD = 0;
        /**
         * 验证码登陆
         */
        public static final Integer LOGIN_TYPE_VERIFY = 1;
    }

    /**
     * 充值相关
     */
    class Balance {
        public static final String _PAY_PASS_WORD = "_payPassword";
    }

    class Request {
        public static final String GET = "get";
        public static final String GETS = "gets";
        public static final String HEAD = "head";
        public static final String HEADS = "heads";
        public static final String POST = "post";
        public static final String PUT = "put";
        public static final String DELETE = "delete";
    }

    /**
     * 设置密码
     */
    class Reset {
        public static final String ID = "id";
        public static final String PHONE = "phone";
        public static final String _PASS_WORD = "_password";
        public static final String _PAY_PASS_WORD = "_payPassword";
        public static final String OLD_PASS_WORD = "oldPassword";
        public static final String VERIFY = "verify";
    }

    class Jwt {
        public static final String JWT_KEY = "7786df7fc3a34e26a61c034d5ec8245d";
        public static final String JWT_BEARER = "cIjpcIjEwMDFcIixcInJ";
        public static final String JWT_AUTHOR = "Authorization";
        public static final String JWT_ID = "jwtId";
        public static final String JWT_USER_INFO = "jwtUserInfo";

    }

    class ISqlConfig {
        public static final String DATABASE_MYSQL = "MySQL";
        public static final String DATABASE_POSTGRESQL = "PostgreSQL";
        public static final String SCHEMA_INFORMATION = "information_schema";
        public static final String TABLE_SCHEMA = "table_schema";
        public static final String TABLE_NAME = "table_name";
        public static final Integer TYPE_CHILD = 0;
        public static final Integer TYPE_ITEM = 1;
        public static final Integer TYPE_ITEM_CHILD_0 = 2;
    }
}
