package apijson.demo.server.controller;

import static com.zhangls.apijson.base.model.RequestMethod.DELETE;
import static com.zhangls.apijson.base.model.RequestMethod.GET;
import static com.zhangls.apijson.base.model.RequestMethod.GETS;
import static com.zhangls.apijson.base.model.RequestMethod.HEAD;
import static com.zhangls.apijson.base.model.RequestMethod.HEADS;
import static com.zhangls.apijson.base.model.RequestMethod.POST;
import static com.zhangls.apijson.base.model.RequestMethod.PUT;

import java.net.URLDecoder;

import javax.servlet.http.HttpSession;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import com.zhangls.apijson.base.model.RequestMethod;

/**
 * 通用Controller
 *
 * 建议全部使用POST请求
 *
 * @author zhangls
 */
@Slf4j
@RestController
public class Controller {

    @Autowired
    private StandardParser standardParser;

    /**
     * 获取 查询
     */
    @PostMapping(UtilConstants.Request.GET)
    public String get(@RequestBody String request, HttpSession session) {
        log.info("【进入 {} 方法，请求JSON串为】：{}", RequestMethod.getName(GET), request);

        standardParser.setMethod(GET);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 计数 查询count(*)
     */
    @PostMapping(UtilConstants.Request.HEAD)
    public String head(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(HEAD), request);

        standardParser.setMethod(HEAD);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 限制性GET，request和response都非明文，浏览器看不到，用于对安全性要求高的GET请求
     */
    @PostMapping(UtilConstants.Request.GETS)
    public String gets(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(GETS), request);

        standardParser.setMethod(GETS);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 限制性HEAD，request和response都非明文，浏览器看不到，用于对安全性要求高的HEAD请求
     */
    @PostMapping(UtilConstants.Request.HEADS)
    public String heads(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(HEADS), request);

        standardParser.setMethod(HEADS);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 新增POST
     */
    @PostMapping(UtilConstants.Request.POST)
    public String post(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(POST), request);

        standardParser.setMethod(POST);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 修改 PUT
     */
    @PostMapping(UtilConstants.Request.PUT)
    public String put(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(PUT), request);

        standardParser.setMethod(PUT);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 删除 DELETE
     */
    @PostMapping(UtilConstants.Request.DELETE)
    public String delete(@RequestBody String request, HttpSession session) {
        log.info("进入 {} 方法，请求JSON串为：{}", RequestMethod.getName(DELETE), request);

        standardParser.setMethod(DELETE);
        return standardParser.setSession(session).parse(request);
    }

    /**
     * 获取
     * 只为兼容HTTP GET请求，推荐用HTTP POST，可删除
     */
    @Deprecated
    @GetMapping("get/{request}")
    public String openGet(@PathVariable String request, HttpSession session) {
        try {
            request = URLDecoder.decode(request, StringUtil.UTF_8);
        } catch (Exception e) {
            // Parser会报错
        }
        return get(request, session);
    }

    /**
     * 计数
     * 只为兼容HTTP GET请求，推荐用HTTP POST，可删除
     */
    @Deprecated
    @GetMapping("head/{request}")
    public String openHead(@PathVariable String request, HttpSession session) {
        try {
            request = URLDecoder.decode(request, StringUtil.UTF_8);
        } catch (Exception e) {
            // Parser会报错
        }
        return head(request, session);
    }

}
