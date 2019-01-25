package apijson.demo.server.controller;

import apijson.demo.server.common.JsonRequest;
import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.model.LoginVo;
import apijson.demo.server.service.StandardService;
import apijson.demo.server.utils.JwtUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 登陆生成token
 *
 * Created by zhangls on 2019/1/25.
 * @author zhangls
 */
@Slf4j
@RestController
public class TokenController {

    @Autowired
    private StandardService service;

    /**
     * 生成token
     *
     * @param reqJson 参数 userName (必填-登陆用户名), password (必填-登陆密码),
     *                ip (必填-访问IP), time(选填默认24小时<86400秒>-token超时时间)
     * @return
     */
    @PostMapping("/generateToken")
    public String generateToken(@RequestBody(required=false) String reqJson, HttpServletRequest req) {
        Long startTime = System.currentTimeMillis();
        JsonResponse<JSONObject> jsonRequest = StandardControllerHelper.checkJsonFormat(reqJson);
        if (!jsonRequest.getSuccess()) {
            return StandardControllerHelper.jsonToString(jsonRequest);
        }

        JSONObject obj = jsonRequest.getData();

        log.info("【请求JSON串：】{},", obj);

        JsonResponse<Map<String, Object>> jsonResponse = service.loginService(JSON.toJavaObject(obj, LoginVo.class));

        if (!jsonResponse.getSuccess()) {
            jsonResponse.setJsonReq(new JsonRequest(String.valueOf(req.getRequestURL()), obj.toJSONString()));
            return StandardControllerHelper.jsonToString(jsonResponse);
        }

        Map<String, Object> map = Maps.newHashMap();
        map.put("ID", jsonResponse.getData().get("ID"));

        // 生成claims
        JSONObject claims = JSONObject.parseObject(JSON.toJSONString(map));

        // 生成JWT
        String jwt = JwtUtils.createJwt(JwtUtils.createClaims(claims), "JsonApi", Integer.valueOf(obj.get("time").toString()));

        JsonResponse result = new JsonResponse(jwt);

        // 添加请求基本信息 返回客户端
        result.setJsonReq(new JsonRequest(String.valueOf(req.getRequestURL()), obj.toJSONString()));

        Long endTime = System.currentTimeMillis();

        return StandardControllerHelper.addTimeInfo(result, startTime, endTime);
    }
}
