package apijson.demo.server.utils;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.BaseModel;
import apijson.demo.server.model.Verify;
import com.alibaba.fastjson.JSONObject;
import zuo.biao.apijson.JSONResponse;
import zuo.biao.apijson.StringUtil;
import zuo.biao.apijson.server.JSONRequest;
import zuo.biao.apijson.server.exception.ConditionErrorException;
import zuo.biao.apijson.server.exception.NotExistException;

import java.util.concurrent.TimeoutException;

import static zuo.biao.apijson.RequestMethod.DELETE;
import static zuo.biao.apijson.RequestMethod.GETS;
import static zuo.biao.apijson.RequestMethod.HEADS;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
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
        JSONResponse response = new JSONResponse(
                new StandardParser(GETS, true).parseResponse(
                        new JSONRequest(new Verify(type, phone).setVerify(code)
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
            new StandardParser(DELETE, true).parseResponse(
                    new JSONRequest(new Verify(type, phone)).setTag(UtilConstants.Public.VERIFY_)
            );
            return StandardParser.newErrorResult(new TimeoutException("验证码已过期！"));
        }

        return new JSONResponse(
                new StandardParser(HEADS, true).parseResponse(
                        new JSONRequest(new Verify(type, phone).setVerify(code)).setFormat(true)
                )
        );
    }
}