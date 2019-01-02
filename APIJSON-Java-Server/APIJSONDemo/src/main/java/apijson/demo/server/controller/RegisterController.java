package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.User;
import apijson.demo.server.model.Verify;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import zuo.biao.apijson.JSONResponse;
import zuo.biao.apijson.StringUtil;
import zuo.biao.apijson.server.JSONRequest;
import zuo.biao.apijson.server.exception.ConditionErrorException;

import static zuo.biao.apijson.RequestMethod.DELETE;
import static zuo.biao.apijson.RequestMethod.POST;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
public class RegisterController {

    private static final String REGISTER = "register";

    public static final String USER_;
    public static final String PRIVACY_;
    public static final String VERIFY_;

    static {
        USER_ = User.class.getSimpleName();
        PRIVACY_ = Privacy.class.getSimpleName();
        VERIFY_ = Verify.class.getSimpleName();
    }

    public static final String PHONE = "phone";
    public static final String _PASSWORD = "_password";
    public static final String VERIFY = "verify";


    /**
     * 注册
     *
     * @param request 只用String，避免encode后未decode
     * @return
     * @see <pre>
     * {
     * "Privacy": {
     * "phone": "13000082222",
     * "_password": "123456"
     * },
     * "User": {
     * "name": "APIJSONUser"
     * },
     * "verify": "1234"
     * }
     * </pre>
     */
    @PostMapping(REGISTER)
    public JSONObject register(@RequestBody String request) {
        JSONObject requestObject = null;

        JSONObject privacyObj;

        String phone;
        String verify;
        String password;
        try {
            requestObject = StandardParser.parseRequest(request);
            privacyObj = requestObject.getJSONObject(PRIVACY_);

            phone = StringUtil.getString(privacyObj.getString(PHONE));
            verify = requestObject.getString(VERIFY);
            password = privacyObj.getString(_PASSWORD);

            if (StringUtil.isPhone(phone) == false) {
                return newIllegalArgumentResult(requestObject, PRIVACY_ + "/" + PHONE);
            }
            if (StringUtil.isPassword(password) == false) {
                return newIllegalArgumentResult(requestObject, PRIVACY_ + "/" + _PASSWORD);
            }
            if (StringUtil.isVerify(verify) == false) {
                return newIllegalArgumentResult(requestObject, VERIFY);
            }
        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }


        JSONResponse response = new JSONResponse(ControllerUtils.headVerify(Verify.TYPE_REGISTER, phone, verify));
        if (JSONResponse.isSuccess(response) == false) {
            return response;
        }
        //手机号或验证码错误
        if (JSONResponse.isExist(response.getJSONResponse(VERIFY_)) == false) {
            return StandardParser.extendErrorResult(response, new ConditionErrorException("手机号或验证码错误！"));
        }


        //生成User和Privacy
        if (StringUtil.isEmpty(requestObject.getString(JSONRequest.KEY_TAG), true)) {
            requestObject.put(JSONRequest.KEY_TAG, REGISTER);
        }
        requestObject.put(JSONRequest.KEY_FORMAT, true);
        response = new JSONResponse(
                new StandardParser(POST).setNoVerifyLogin(true).parseResponse(requestObject)
        );

        //验证User和Privacy
        User user = response.getObject(User.class);
        long userId = user == null ? 0 : BaseModel.value(user.getId());
        Privacy privacy = response.getObject(Privacy.class);
        long userId2 = privacy == null ? 0 : BaseModel.value(privacy.getId());
        Exception e = null;
        if (userId <= 0 || userId != userId2) { //id不同
            e = new Exception("服务器内部错误！写入User或Privacy失败！");
        }

        if (e != null) { //出现错误，回退
            new StandardParser(DELETE, true).parseResponse(
                    new JSONRequest(new User(userId))
            );
            new StandardParser(DELETE, true).parseResponse(
                    new JSONRequest(new Privacy(userId2))
            );
        }

        return response;
    }


    /**
     * @param requestObject
     * @param key
     * @return
     */
    public static JSONObject newIllegalArgumentResult(JSONObject requestObject, String key) {
        return newIllegalArgumentResult(requestObject, key, null);
    }

    /**
     * @param requestObject
     * @param key
     * @param msg           详细说明
     * @return
     */
    public static JSONObject newIllegalArgumentResult(JSONObject requestObject, String key, String msg) {
        return StandardParser.extendErrorResult(requestObject
                , new IllegalArgumentException(key + ":value 中value不合法！" + StringUtil.getString(msg)));
    }

}
