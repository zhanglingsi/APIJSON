package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.Verify;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.ConditionErrorException;
import com.zhangls.apijson.base.exception.ConflictException;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.utils.StringUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
@RestController
public class ResetPassword {


    /**
     * 设置密码
     *
     * @param request 只用String，避免encode后未decode
     * @return
     * @see <pre>
     * 使用旧密码修改
     * {
     * "oldPassword": 123456,
     * "Privacy":{
     * "id": 13000082001,
     * "_password": "1234567"
     * }
     * }
     * 或使用手机号+验证码修改
     * {
     * "verify": "1234",
     * "Privacy":{
     * "phone": "13000082001",
     * "_password": "1234567"
     * }
     * }
     * </pre>
     */
    @PostMapping("put/password")
    public JSONObject putPassword(@RequestBody String request) {
        JSONObject requestObject = null;
        String oldPassword;
        String verify;

        int type = Verify.TYPE_PASSWORD;

        JSONObject privacyObj;
        long userId;
        String phone;
        String password;
        try {
            requestObject = StandardParser.parseRequest(request);
            oldPassword = StringUtil.getString(requestObject.getString(UtilConstants.Reset.OLD_PASS_WORD));
            verify = StringUtil.getString(requestObject.getString(UtilConstants.Reset.VERIFY));

            requestObject.remove(UtilConstants.Reset.OLD_PASS_WORD);
            requestObject.remove(UtilConstants.Reset.VERIFY);

            privacyObj = requestObject.getJSONObject(UtilConstants.Public.PRIVACY_);
            if (privacyObj == null) {
                throw new IllegalArgumentException(UtilConstants.Public.PRIVACY_ + " 不能为空！");
            }
            userId = privacyObj.getLongValue(UtilConstants.Reset.ID);
            phone = privacyObj.getString(UtilConstants.Reset.PHONE);
            password = privacyObj.getString(UtilConstants.Reset._PASS_WORD);

            if (StringUtil.isEmpty(password, true)) {
                type = Verify.TYPE_PAY_PASSWORD;
                password = privacyObj.getString(UtilConstants.Balance._PAY_PASS_WORD);
                if (StringUtil.isNumberPassword(password) == false) {
                    throw new IllegalArgumentException(UtilConstants.Public.PRIVACY_ + "/" + UtilConstants.Balance._PAY_PASS_WORD + ":value 中value不合法！");
                }
            } else { //登录密码
                if (StringUtil.isPassword(password) == false) {
                    throw new IllegalArgumentException(UtilConstants.Public.PRIVACY_ + "/" + UtilConstants.Reset._PASS_WORD + ":value 中value不合法！");
                }
            }
        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }


        if (StringUtil.isPassword(oldPassword)) {
            if (userId <= 0) {
                return StandardParser.extendErrorResult(requestObject, new IllegalArgumentException(UtilConstants.Reset.ID + ":value 中value不合法！"));
            }
            if (oldPassword.equals(password)) {
                return StandardParser.extendErrorResult(requestObject, new ConflictException("新旧密码不能一样！"));
            }

            //验证旧密码
            Privacy privacy = new Privacy(userId);
            if (type == Verify.TYPE_PASSWORD) {
                privacy.setPassword(oldPassword);
            } else {
                privacy.setPayPassword(oldPassword);
            }
            JsonApiResponse response = new JsonApiResponse(
                    new StandardParser(RequestMethod.HEAD, true).parseResponse(
                            new JsonApiRequest(privacy).setFormat(true)
                    )
            );
            if (JsonApiResponse.isExist(response.getJSONResponse(UtilConstants.Public.PRIVACY_)) == false) {
                return StandardParser.extendErrorResult(requestObject, new ConditionErrorException("账号或原密码错误，请重新输入！"));
            }
        } else if (StringUtil.isPhone(phone) && StringUtil.isVerify(verify)) {
            JsonApiResponse response = new JsonApiResponse(ControllerUtils.headVerify(type, phone, verify));
            if (JsonApiResponse.isSuccess(response) == false) {
                return response;
            }
            if (JsonApiResponse.isExist(response.getJSONResponse(UtilConstants.Public.VERIFY_)) == false) {
                return StandardParser.extendErrorResult(response, new ConditionErrorException("手机号或验证码错误！"));
            }
            response = new JsonApiResponse(
                    new StandardParser(RequestMethod.GET, true).parseResponse(
                            new JsonApiRequest(
                                    new Privacy().setPhone(phone)
                            )
                    )
            );
            Privacy privacy = response.getObject(Privacy.class);
            long id = privacy == null ? 0 : BaseModel.value(privacy.getId());
            privacyObj.remove(UtilConstants.Reset.PHONE);
            privacyObj.put(UtilConstants.Reset.ID, id);

            requestObject.put(UtilConstants.Public.PRIVACY_, privacyObj);
        } else {
            return StandardParser.extendErrorResult(requestObject, new IllegalArgumentException("请输入合法的 旧密码 或 手机号+验证码 ！"));
        }
        //TODO 上线版加上   password = MD5Util.MD5(password);


        //		requestObject.put(JSONRequest.KEY_TAG, "Password");
        requestObject.put(JsonApiRequest.KEY_FORMAT, true);
        //修改密码
        return new StandardParser(RequestMethod.PUT, true).parseResponse(requestObject);
    }
}
