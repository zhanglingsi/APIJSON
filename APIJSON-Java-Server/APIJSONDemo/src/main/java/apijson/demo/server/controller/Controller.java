package apijson.demo.server.controller;

import static zuo.biao.apijson.RequestMethod.DELETE;
import static zuo.biao.apijson.RequestMethod.GET;
import static zuo.biao.apijson.RequestMethod.GETS;
import static zuo.biao.apijson.RequestMethod.HEAD;
import static zuo.biao.apijson.RequestMethod.HEADS;
import static zuo.biao.apijson.RequestMethod.POST;
import static zuo.biao.apijson.RequestMethod.PUT;

import java.net.URLDecoder;

import javax.servlet.http.HttpSession;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.UtilConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import zuo.biao.apijson.*;
import zuo.biao.apijson.RequestMethod;

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

    /**
     * 获取 查询
     */
    @PostMapping(UtilConstants.Request.GET)
    public String get(@RequestBody String request, HttpSession session) {
        log.info("【进入 {} 方法，请求JSON串为】：{}", RequestMethod.getName(GET), request);

        return new StandardParser(GET).setSession(session).parse(request);
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
