package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.StandardVerifier;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Privacy;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.ConditionErrorException;
import com.zhangls.apijson.base.exception.OutOfRangeException;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.utils.StringUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;


/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
@RestController
public class BalanceController {

    /**
     * 充值/提现
     *
     * @param request 只用String，避免encode后未decode
     * @param session
     * @return
     * @see <pre>
     * {
     * "Privacy": {
     * "id": 82001,
     * "balance+": 100,
     * "_payPassword": "123456"
     * }
     * }
     * </pre>
     */
    @PostMapping("put/balance")
    public JSONObject putBalance(@RequestBody String request, HttpSession session) {
        JSONObject requestObject = null;
        JSONObject privacyObj;
        long userId;
        String payPassword;
        double change;
        try {
            StandardVerifier.verifyLogin(session);
            requestObject = new StandardParser(RequestMethod.PUT).setRequest(StandardParser.parseRequest(request)).parseCorrectRequest();

            privacyObj = requestObject.getJSONObject(UtilConstants.Public.PRIVACY_);
            if (privacyObj == null) {
                throw new NullPointerException("请设置 " + UtilConstants.Public.PRIVACY_ + "!");
            }
            userId = privacyObj.getLongValue(UtilConstants.Reset.ID);
            payPassword = privacyObj.getString(UtilConstants.Balance._PAY_PASS_WORD);
            change = privacyObj.getDoubleValue("balance+");

            if (userId <= 0) {
                throw new IllegalArgumentException(UtilConstants.Public.PRIVACY_ + "." + UtilConstants.Reset.ID + ":value 中value不合法！");
            }
            if (StringUtil.isPassword(payPassword) == false) {
                throw new IllegalArgumentException(UtilConstants.Public.PRIVACY_ + "." + UtilConstants.Balance._PAY_PASS_WORD + ":value 中value不合法！");
            }
        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }

        //验证密码<<<<<<<<<<<<<<<<<<<<<<<

        privacyObj.remove("balance+");
        JsonApiResponse response = new JsonApiResponse(
                new StandardParser(RequestMethod.HEADS, true).setSession(session).parseResponse(
                        new JsonApiRequest(UtilConstants.Public.PRIVACY_, privacyObj)
                )
        );
        response = response.getJSONResponse(UtilConstants.Public.PRIVACY_);
        if (JsonApiResponse.isExist(response) == false) {
            return StandardParser.extendErrorResult(requestObject, new ConditionErrorException("支付密码错误！"));
        }

        //验证密码>>>>>>>>>>>>>>>>>>>>>>>>


        //验证金额范围<<<<<<<<<<<<<<<<<<<<<<<

        if (change == 0) {
            return StandardParser.extendErrorResult(requestObject, new OutOfRangeException("balance+的值不能为0！"));
        }
        if (Math.abs(change) > 10000) {
            return StandardParser.extendErrorResult(requestObject, new OutOfRangeException("单次 充值/提现 的金额不能超过10000元！"));
        }

        //验证金额范围>>>>>>>>>>>>>>>>>>>>>>>>

        if (change < 0) {//提现
            response = new JsonApiResponse(
                    new StandardParser(RequestMethod.GETS, true).parseResponse(
                            new JsonApiRequest(
                                    new Privacy(userId)
                            )
                    )
            );
            Privacy privacy = response == null ? null : response.getObject(Privacy.class);
            long id = privacy == null ? 0 : BaseModel.value(privacy.getId());
            if (id != userId) {
                return StandardParser.extendErrorResult(requestObject, new Exception("服务器内部错误！"));
            }

            if (BaseModel.value(privacy.getBalance()) < -change) {
                return StandardParser.extendErrorResult(requestObject, new OutOfRangeException("余额不足！"));
            }
        }


        privacyObj.remove(UtilConstants.Balance._PAY_PASS_WORD);
        privacyObj.put("balance+", change);
        requestObject.put(UtilConstants.Public.PRIVACY_, privacyObj);
        requestObject.put(JsonApiRequest.KEY_TAG, UtilConstants.Public.PRIVACY_);
        requestObject.put(JsonApiRequest.KEY_FORMAT, true);
        //不免验证，里面会验证身份
        return new StandardParser(RequestMethod.PUT).setSession(session).parseResponse(requestObject);
    }
}
