package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.User;
import apijson.demo.server.model.Verify;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zuo.biao.apijson.JSONResponse;
import zuo.biao.apijson.StringUtil;
import zuo.biao.apijson.server.JSONRequest;
import zuo.biao.apijson.server.exception.ConditionErrorException;
import zuo.biao.apijson.server.exception.NotExistException;

import javax.servlet.http.HttpSession;
import static zuo.biao.apijson.RequestMethod.GETS;
import static zuo.biao.apijson.RequestMethod.HEADS;

/**
 * 登陆
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
@RestController
public class LoginController {

    /**
     * 用户登录
     *
     * @param request 只用String，避免encode后未decode
     * @return
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
        JSONObject phoneResponse = new StandardParser(HEADS, true).parseResponse(
                new JSONRequest(
                        new Privacy().setPhone(phone)
                )
        );
        if (JSONResponse.isSuccess(phoneResponse) == false) {
            return StandardParser.newResult(phoneResponse.getIntValue(JSONResponse.KEY_CODE), phoneResponse.getString(JSONResponse.KEY_MSG));
        }
        JSONResponse response = new JSONResponse(phoneResponse).getJSONResponse(UtilConstants.Public.PRIVACY_);
        if (JSONResponse.isExist(response) == false) {
            return StandardParser.newErrorResult(new NotExistException("手机号未注册"));
        }

        //根据phone获取User
        JSONObject privacyResponse = new StandardParser(GETS, true).parseResponse(
                new JSONRequest(
                        new Privacy().setPhone(phone)
                ).setFormat(true)
        );
        response = new JSONResponse(privacyResponse);

        Privacy privacy = response == null ? null : response.getObject(Privacy.class);
        long userId = privacy == null ? 0 : BaseModel.value(privacy.getId());
        if (userId <= 0) {
            return privacyResponse;
        }

        //校验凭证
        if (isPassword) {
            response = new JSONResponse(
                    new StandardParser(HEADS, true).parseResponse(
                            new JSONRequest(new Privacy(userId).setPassword(password))
                    )
            );
        } else {//verify手机验证码登录
            response = new JSONResponse(ControllerUtils.headVerify(Verify.TYPE_LOGIN, phone, password));
        }
        if (JSONResponse.isSuccess(response) == false) {
            return response;
        }
        response = response.getJSONResponse(isPassword ? UtilConstants.Public.PRIVACY_ : UtilConstants.Public.VERIFY_);
        if (JSONResponse.isExist(response) == false) {
            return StandardParser.newErrorResult(new ConditionErrorException("账号或密码错误"));
        }

        response = new JSONResponse(
                new StandardParser(GETS, true).parseResponse(
                        new JSONRequest(new User(userId)).setFormat(true)
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
