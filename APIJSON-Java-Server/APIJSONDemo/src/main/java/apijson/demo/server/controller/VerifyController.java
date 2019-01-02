package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.Verify;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zuo.biao.apijson.JSON;
import zuo.biao.apijson.JSONResponse;
import zuo.biao.apijson.StringUtil;
import zuo.biao.apijson.server.JSONRequest;

import java.util.Random;

import static zuo.biao.apijson.RequestMethod.*;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
@RestController
public class VerifyController {

    /**
     * 生成验证码,修改为post请求
     *
     * @param request
     * @return
     */
    @PostMapping("post/verify")
    public JSONObject postVerify(@RequestBody String request) {
        JSONObject requestObject = null;
        int type;
        String phone;
        try {
            requestObject = StandardParser.parseRequest(request);
            type = requestObject.getIntValue(UtilConstants.Login.TYPE);
            phone = requestObject.getString(UtilConstants.Reset.PHONE);
        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }

        new StandardParser(DELETE, true).parse(newVerifyRequest(type, phone, null));

        JSONObject response = new StandardParser(POST, true).parseResponse(
                newVerifyRequest(type, phone, "" + (new Random().nextInt(9999) + 1000))
        );

        JSONObject verify = null;
        try {
            verify = response.getJSONObject(StringUtil.firstCase(UtilConstants.Public.VERIFY_));
        } catch (Exception e) {
        }

        if (verify == null || JSONResponse.isSuccess(verify.getIntValue(JSONResponse.KEY_CODE)) == false) {
            new StandardParser(DELETE, true).parseResponse(new JSONRequest(new Verify(type, phone)));
            return response;
        }

        //TODO 这里直接返回验证码，方便测试。实际上应该只返回成功信息，验证码通过短信发送
        JSONObject object = new JSONObject();
        object.put(UtilConstants.Login.TYPE, type);
        object.put(UtilConstants.Reset.PHONE, phone);
        return getVerify(JSON.toJSONString(object));
    }

    /**
     * 获取验证码
     *
     * @param request
     * @return
     */
    @PostMapping("gets/verify")
    public JSONObject getVerify(@RequestBody String request) {
        JSONObject requestObject = null;
        int type;
        String phone;
        try {
            requestObject = StandardParser.parseRequest(request);
            type = requestObject.getIntValue(UtilConstants.Login.TYPE);
            phone = requestObject.getString(UtilConstants.Reset.PHONE);
        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }
        return new StandardParser(GETS, true).parseResponse(newVerifyRequest(type, phone, null));
    }

    /**
     * 校验验证码
     *
     * @param request
     * @return
     */
    @PostMapping("heads/verify")
    public JSONObject headVerify(@RequestBody String request) {
        JSONObject requestObject = null;
        int type;
        String phone;
        String verify;
        try {
            requestObject = StandardParser.parseRequest(request);
            type = requestObject.getIntValue(UtilConstants.Login.TYPE);
            phone = requestObject.getString(UtilConstants.Reset.PHONE);
            verify = requestObject.getString(UtilConstants.Reset.VERIFY);
        } catch (Exception e) {
            return StandardParser.extendErrorResult(requestObject, e);
        }
        return ControllerUtils.headVerify(type, phone, verify);
    }


    /**
     * 新建一个验证码请求
     *
     * @param phone
     * @param verify
     * @return
     */
    private zuo.biao.apijson.JSONRequest newVerifyRequest(int type, String phone, String verify) {
        return new JSONRequest(new Verify(type, phone).setVerify(verify)).setTag(UtilConstants.Public.VERIFY_).setFormat(true);
    }
}
