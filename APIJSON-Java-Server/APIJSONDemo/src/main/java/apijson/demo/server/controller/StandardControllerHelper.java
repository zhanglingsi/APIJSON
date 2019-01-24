package apijson.demo.server.controller;

import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.common.RespCode;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApi;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Created by zhangls on 2019/1/24.
 *
 * @author zhangls
 */
@Slf4j
public class StandardControllerHelper {

    /**
     * 用户名 生成token 必填字段
     */
    private static final String USER_NAME = "userName";
    /**
     * 密码 生成token 必填字段
     */
    private static final String PASS_WORD = "password";
    /**
     * 客户端IP 生成token 必填字段
     */
    private static final String IP = "ip";

    /**
     * 校验 Json串的格式合法性，是否能被解析
     *
     * @param jsonStr
     * @return
     */
    public static JsonResponse<JSONObject> checkJsonFormat(String jsonStr) {
        JSONObject obj = JsonApi.parseObject(jsonStr);
        if (obj == null) {
            return new JsonResponse(RespCode.ERROR_JSON_FORMAT.getResCode(), RespCode.ERROR_JSON_FORMAT.getResDesc());
        }

        if (!Optional.ofNullable(obj.getString(USER_NAME)).isPresent()) {
            return new JsonResponse(RespCode.TOKEN_USERNAME_NULL.getResCode(), RespCode.TOKEN_USERNAME_NULL.getResDesc());
        }

        if (!Optional.ofNullable(obj.getString(PASS_WORD)).isPresent()) {
            return new JsonResponse(RespCode.TOKEN_PASSWORD_NULL.getResCode(), RespCode.TOKEN_PASSWORD_NULL.getResDesc());
        }

        if (!Optional.ofNullable(obj.getString(IP)).isPresent()) {
            return new JsonResponse(RespCode.TOKEN_IP_NULL.getResCode(), RespCode.TOKEN_IP_NULL.getResDesc());
        }

        obj.put("time", Optional.ofNullable(obj.getLong("time")).orElse(86400L));

        return new JsonResponse(obj);
    }

}
