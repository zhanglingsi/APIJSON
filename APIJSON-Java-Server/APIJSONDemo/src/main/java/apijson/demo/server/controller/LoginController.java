package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.User;
import apijson.demo.server.model.Verify;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.ConditionErrorException;
import com.zhangls.apijson.base.exception.NotExistException;
import com.zhangls.apijson.base.service.impl.JsonBaseRequest;
import com.zhangls.apijson.utils.StringUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import static com.zhangls.apijson.base.model.RequestMethod.GETS;
import static com.zhangls.apijson.base.model.RequestMethod.HEADS;

/**
 * Controller层的职责
 * 1. 验证收到的请求  异常：返回异常结果，和异常码， 必须的请求参数是否为空，格式是否正确，解析是否正常等
 * 2. 校验请求正常（表示该请求可以被处理）：发送给Service层调用 并返回结果
 * 3. 返回结果处理（将用户信息添加到session，操作cookie等）
 *
 * service层的职责
 * 1. 业务逻辑校验  注册时 输入的两次密码不一致  登陆时用户名不存在 等业务逻辑校验
 * 2. 处理业务，包括事物原子性等 调用DAO层 访问数据库  并缓存查询结果  key：sql语句  value：ResultSet
 * 3. 封装返回结果
 *
 * DAO层的职责
 * 1. 查询参数校验，查询对象非空校验，插入表数据的唯一校验，以及非空校验等
 * 2. 访问数据库，从数据源连接池获取Connection对象，并创建PreparedStatement，设置sql语句，并设置所需参数。
 * 3. 执行SQL，并对返回的ResultSet进行转换，封装成对象，返回给service层
 *
 * 登陆
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
@RestController
public class LoginController {

    /**
     * 用户登录
     *
     * @return
     *  ##查询手机号是否存在  head方法
     *  SELECT  COUNT(*)  AS COUNT  FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0;
     *
     *  ##根据手机号码查询用户隐私表信息，从中获取用户ID
     *  SELECT * FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0;
     *
     *  ##查询用户表中信息  验证用户名密码正确性  是否可以登录
     *  SELECT  COUNT(*)  AS COUNT  FROM `apijson`.`apijson_privacy` WHERE  (  (`id`='82001') AND (`_password`='123456')  )  LIMIT 1 OFFSET 0;
     *
     *  ##根据用户ID查询用户基本信息 并返回  setSession
     *  SELECT * FROM `apijson`.`apijson_user` WHERE  (  (`id`='82001')  )  LIMIT 1 OFFSET 0;
     *
     * @see <pre>
     * {
     * "type": 0,  //登录方式，非必须  0-密码 1-验证码
     * "phone": "13000082001",
     * "password": "1234567",
     * "version": 1 //全局版本号，非必须
     * }
     * </pre>
     */
    @PostMapping("login")
    public JSONObject login(@RequestBody String request, HttpSession session) {
        JSONObject requestObject = null;
        boolean isPassword;
        String phone;
        String password;
        boolean remember;
        int version;
        Boolean format;
        try {
            requestObject = StandardParser.parseRequest(request);

            isPassword = requestObject.getIntValue(UtilConstants.Login.TYPE) == UtilConstants.Login.LOGIN_TYPE_PASSWORD;

            phone = requestObject.getString(UtilConstants.Login.PHONE);
            password = requestObject.getString(UtilConstants.Login.PASS_WORD);

            if (StringUtil.isPhone(phone) == false) {
                throw new IllegalArgumentException("手机号不合法！");
            }

            if (isPassword) {
                if (StringUtil.isPassword(password) == false) {
                    throw new IllegalArgumentException("密码不合法！");
                }
            } else {
                if (StringUtil.isVerify(password) == false) {
                    throw new IllegalArgumentException("验证码不合法！");
                }
            }

            remember = requestObject.getBooleanValue(UtilConstants.Login.REMEMBER);
            version = requestObject.getIntValue(UtilConstants.Login.VERSION);
            format = requestObject.getBoolean(UtilConstants.Login.FORMAT);
            requestObject.remove(UtilConstants.Login.REMEMBER);
            requestObject.remove(UtilConstants.Login.VERSION);
            requestObject.remove(UtilConstants.Login.FORMAT);

        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }


        //手机号是否已注册
        //SELECT  COUNT(*)  AS COUNT  FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0
        JSONObject phoneResponse = new StandardParser(HEADS, true).parseResponse(
                new JsonBaseRequest(
                        new Privacy().setPhone(phone)
                )
        );

        if (!JsonApiResponse.isSuccess(phoneResponse)) {
            return StandardParser.newResult(phoneResponse.getIntValue(JsonApiResponse.KEY_CODE), phoneResponse.getString(JsonApiResponse.KEY_MSG));
        }

        JsonApiResponse response = new JsonApiResponse(phoneResponse).getJSONResponse(UtilConstants.Public.PRIVACY_);

        if (!JsonApiResponse.isExist(response)) {
            return StandardParser.newErrorResult(new NotExistException("手机号未注册"));
        }

        //根据phone获取User
        JSONObject privacyResponse = new StandardParser(GETS, true).parseResponse(
                new JsonBaseRequest(
                        new Privacy().setPhone(phone)
                ).setFormat(true)
        );
        response = new JsonApiResponse(privacyResponse);

        Privacy privacy = response == null ? null : response.getObject(Privacy.class);
        long userId = privacy == null ? 0 : BaseModel.value(privacy.getId());
        if (userId <= 0) {
            return privacyResponse;
        }

        //校验凭证
        if (isPassword) {
            response = new JsonApiResponse(
                    new StandardParser(HEADS, true).parseResponse(
                            new JsonBaseRequest(new Privacy(userId).setPassword(password))
                    )
            );
        } else {//verify手机验证码登录
            response = new JsonApiResponse(ControllerUtils.headVerify(Verify.TYPE_LOGIN, phone, password));
        }

        if (!JsonApiResponse.isSuccess(response)) {
            return response;
        }

        response = response.getJSONResponse(isPassword ? UtilConstants.Public.PRIVACY_ : UtilConstants.Public.VERIFY_);
        if (!JsonApiResponse.isExist(response)) {
            return StandardParser.newErrorResult(new ConditionErrorException("账号或密码错误"));
        }

        response = new JsonApiResponse(
                new StandardParser(GETS, true).parseResponse(
                        new JsonBaseRequest(new User(userId)).setFormat(true)
                )
        );

        User user = response.getObject(User.class);
        if (user == null || BaseModel.value(user.getId()) != userId) {
            return StandardParser.newErrorResult(new NullPointerException("服务器内部错误"));
        }


        //登录状态保存至session
        //用户id
        session.setAttribute(UtilConstants.Login.USER_ID, userId);
        //登录方式
        session.setAttribute(UtilConstants.Login.TYPE, isPassword ? UtilConstants.Login.LOGIN_TYPE_PASSWORD : UtilConstants.Login.LOGIN_TYPE_VERIFY);
        //用户
        session.setAttribute(UtilConstants.Public.USER_, user);
        //用户隐私信息
        session.setAttribute(UtilConstants.Public.PRIVACY_, privacy);
        //记住登录
        session.setAttribute(UtilConstants.Login.REMEMBER, remember);
        //全局默认版本号
        session.setAttribute(UtilConstants.Login.VERSION, version);
        //全局默认格式化配置
        session.setAttribute(UtilConstants.Login.FORMAT, format);

        //设置session过期时间
        session.setMaxInactiveInterval(60 * 60 * 24 * (remember ? 7 : 1));

        response.put(UtilConstants.Login.REMEMBER, remember);

        return response;
    }
}
