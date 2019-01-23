package apijson.demo.server.controller;

import static com.zhangls.apijson.base.model.RequestMethod.GET;

import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.service.StandardService;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiResponse;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhangls on 2019/1/7.
 *
 * @author zhangls
 */
@Slf4j
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class StandardController {


    @Autowired
    private StandardService service;

    /**
     * 获取 查询
     */
    @PostMapping("/{apiName}/{apiId}/{token}/getDataJson")
    public JsonResponse getDataJson(@RequestBody String queryStr,
                                    @PathVariable String apiName,
                                    @PathVariable String apiId,
                                    @PathVariable String token,
                                    final HttpServletRequest request) {
        log.info("【进入 getDataJson 方法，请求apiCode为】：{}", apiName);
        log.info("【进入 getDataJson 方法，请求apiId为】：{}", apiId);
        log.info("【进入 getDataJson 方法，请求token为】：{}", token);
        log.info("【进入 getDataJson 方法，请求JSON串为】：{}", queryStr);



//        // 获取JWT中的用户信息
//        Claims claims = (Claims) request.getAttribute(UtilConstants.Jwt.JWT_USER_INFO);
//        log.info("【获取JWT中的用户信息为】：{}", claims);
//
//        // 1. 访问权限
//        JSONObject jsonValidate = ControllerUtils.standardValidator(GET, queryStr);
//        log.info("【验证后的，返回的JSON串为】：{}", jsonValidate);
//
//        // 验证通过
//        if (jsonValidate.containsKey(JsonApiResponse.KEY_CODE) && jsonValidate.get(JsonApiResponse.KEY_CODE).equals(JsonApiResponse.CODE_SUCCESS)) {
//            // 有可能返回业务异常信息
//            return service.queryService(jsonValidate);
//        } else {
//            jsonValidate.put("request", queryStr);
//
//            return jsonValidate;
//        }

//        return jsonValidate;
        return null;
    }
}
