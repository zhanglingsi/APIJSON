package apijson.demo.server.utils;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Verify;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.ConditionErrorException;
import com.zhangls.apijson.base.exception.NotExistException;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import com.zhangls.apijson.base.model.RequestMethod;

import java.util.concurrent.TimeoutException;


/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
@Slf4j
public class ControllerUtils {

    /**
     * 校验验证码
     *
     * @param type
     * @param phone
     * @param code
     * @return
     * @author Lemon
     */
    public static JSONObject headVerify(int type, String phone, String code) {
        JsonApiResponse response = new JsonApiResponse(
                new StandardParser(RequestMethod.GETS, true).parseResponse(
                        new JsonApiRequest(new Verify(type, phone).setVerify(code)
                        ).setTag(UtilConstants.Public.VERIFY_)
                )
        );
        Verify verify = response.getObject(Verify.class);
        if (verify == null) {
            return StandardParser.newErrorResult(StringUtil.isEmpty(code, true)
                    ? new NotExistException("验证码不存在！") : new ConditionErrorException("手机号或验证码错误！"));
        }

        //验证码过期
        long time = BaseModel.getTimeMillis(verify.getDate());
        long now = System.currentTimeMillis();
        if (now > 60 * 1000 + time) {
            new StandardParser(RequestMethod.DELETE, true).parseResponse(
                    new JsonApiRequest(new Verify(type, phone)).setTag(UtilConstants.Public.VERIFY_)
            );
            return StandardParser.newErrorResult(new TimeoutException("验证码已过期！"));
        }

        return new JsonApiResponse(
                new StandardParser(RequestMethod.HEADS, true).parseResponse(
                        new JsonApiRequest(new Verify(type, phone).setVerify(code)).setFormat(true)
                )
        );
    }

    /**
     * 校验方法
     * @param method
     * @param reqJson
     * @return
     */
    public static JSONObject standardValidator(RequestMethod method, String reqJson){
        // 1. 转换验证
        JSONObject requestObject = null;
        try {
            requestObject = JsonParseUtils.parseRequest(reqJson);
            log.info("【请求JSON串转换JSONObject成功！】");
        } catch (Exception e) {
            return JsonParseUtils.newErrorResult(e);
        }

        // 2. 权限验证


        return requestObject;
    }
}
