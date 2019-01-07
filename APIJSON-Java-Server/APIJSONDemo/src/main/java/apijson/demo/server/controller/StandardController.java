package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.service.StandardService;
import apijson.demo.server.utils.ControllerUtils;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zuo.biao.apijson.RequestMethod;
import zuo.biao.apijson.StringUtil;

import javax.servlet.http.HttpSession;
import java.net.URLDecoder;

import static zuo.biao.apijson.RequestMethod.*;
import static zuo.biao.apijson.RequestMethod.DELETE;
import static zuo.biao.apijson.RequestMethod.PUT;

/**
 * Created by zhangls on 2019/1/7.
 * @author zhangls
 */
@Slf4j
@RestController
public class StandardController {


    @Autowired
    private StandardService service;

    /**
     * 获取 查询
     */
    @PostMapping(UtilConstants.Request.GET)
    public JSONObject get(@RequestBody String request, HttpSession session) {
        log.info("【进入 {} 方法，请求JSON串为】：{}", RequestMethod.getName(GET), request);

        // 1. 访问权限
        JSONObject jsonValidate = ControllerUtils.standardValidator(RequestMethod.GET, request, session);
        log.info("【验证后的，返回的JSON串为】：{}", jsonValidate);

        // 验证通过
        if (jsonValidate.containsKey(JsonResponse.KEY_CODE) && jsonValidate.get(JsonResponse.KEY_CODE).equals(JsonResponse.CODE_SUCCESS)) {
            // 有可能返回业务异常信息
            return service.queryService(jsonValidate);
        } else {
            jsonValidate.put("request", request);

            return jsonValidate;
        }
    }

    /**
     * 计数 查询count(*)
     */
    @PostMapping(UtilConstants.Request.HEAD)
    public String head(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(HEAD), request);

        return new StandardParser(HEAD).setSession(session).parse(request);
    }

    /**
     * 限制性GET，request和response都非明文，浏览器看不到，用于对安全性要求高的GET请求
     */
    @PostMapping(UtilConstants.Request.GETS)
    public String gets(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(GETS), request);

        return new StandardParser(GETS).setSession(session).parse(request);
    }

    /**
     * 限制性HEAD，request和response都非明文，浏览器看不到，用于对安全性要求高的HEAD请求
     */
    @PostMapping(UtilConstants.Request.HEADS)
    public String heads(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(HEADS), request);

        return new StandardParser(HEADS).setSession(session).parse(request);
    }

    /**
     * 新增POST
     */
    @PostMapping(UtilConstants.Request.POST)
    public String post(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(POST), request);

        return new StandardParser(POST).setSession(session).parse(request);
    }

    /**
     * 修改 PUT
     */
    @PostMapping(UtilConstants.Request.PUT)
    public String put(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(PUT), request);

        return new StandardParser(PUT).setSession(session).parse(request);
    }

    /**
     * 删除 DELETE
     */
    @PostMapping(UtilConstants.Request.DELETE)
    public String delete(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(DELETE), request);

        return new StandardParser(DELETE).setSession(session).parse(request);
    }
}
