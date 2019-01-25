package apijson.demo.server.config;

import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.common.RespCode;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.utils.JwtUtils;
import apijson.demo.server.utils.WebUtils;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.utils.StringUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Created by zhangls on 2019/1/8.
 *
 * @author zhangls
 */
@Slf4j
@Component
public class StandardInterceptor implements HandlerInterceptor {

    public static final String CONTENT_TYPE = "content-type";
    public static final String ACCEPT = "Accept";
    public static final String UTF8 = "UTF-8";


    /**
     * 预处理回调方法，实现处理器的预处理
     * 返回值：true表示继续流程；false表示流程中断，不会继续调用其他的拦截器或处理器
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        log.info("【StandardInterceptor请求拦截器预处理方法】：{}", "preHandle");

        String contentType = "";
        String accept = "";
        log.debug("【####请求头信息#####################################################################】");
        Enumeration<String> reqHeadInfo = req.getHeaderNames();
        while (reqHeadInfo.hasMoreElements()) {
            String headName = String.valueOf(reqHeadInfo.nextElement());
            String headValue = req.getHeader(headName);
            if (CONTENT_TYPE.equalsIgnoreCase(headName)) {
                contentType = headValue;
            } else if (ACCEPT.equalsIgnoreCase(headName)) {
                accept = headValue;
            }
            log.debug("【请求头信息】【{}】：【{}】", headName, headValue);
        }


        if (!MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(accept)) {
            JSONObject jsonObject = new JSONObject(true);

            jsonObject.put("success", false);
            jsonObject.put("errorCode", RespCode.ERROR_ACCEPT.getResCode());
            jsonObject.put("errorMsg", RespCode.ERROR_ACCEPT.getResDesc());
            StandardInterceptor.responseJson(res, jsonObject);

            return false;
        } else if (!MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(contentType)) {
            JSONObject jsonObject = new JSONObject(true);

            jsonObject.put("success", false);
            jsonObject.put("errorCode", RespCode.ERROR_CONTENT_TYPE.getResCode());
            jsonObject.put("errorMsg", RespCode.ERROR_CONTENT_TYPE.getResDesc());
            StandardInterceptor.responseJson(res, jsonObject);
            return false;
        }
        log.debug("【####请求头信息#####################################################################】");


        String[] strings = req.getRequestURI().split("/");

        for (int i = 0; i < strings.length; i++) {
            switch (i) {
                case 2:
                    log.info("【进入 getDataJson 方法，请求apiCode为】：{}", strings[2]);
                    break;
                case 3:
                    log.info("【进入 getDataJson 方法，请求apiId为】：{}", strings[3]);
                    break;
                case 4:
                    log.info("【进入 getDataJson 方法，请求token为】：{}", strings[4]);
                    break;
                default:
                    ;
            }
        }

        String ip = WebUtils.getClientIpAddr(req);

        log.info("【客户端IP地址：】{}，【浏览器:】{}", ip, req.getHeader("user-agent"));

        return true;
    }

    /**
     * 后处理回调方法，实现处理器（controller）的后处理，但在渲染视图之前
     * 此时我们可以通过modelAndView对模型数据进行处理或对视图进行处理
     */
    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("【请求拦截器后处理回调方法】：{}", "postHandle");


    }

    /**
     * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，
     * 如性能监控中我们可以在此记录结束时间并输出消耗时间，
     * 还可以进行一些资源清理，类似于try-catch-finally中的finally，
     * 但仅调用处理器执行链中
     */
    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception e) throws Exception {
        log.info("【请求拦截器处理完毕回调方法】：{}", "afterCompletion");

    }


    /**
     * 不符合要求的request，直接返回response
     * @param res
     * @param jsonObject
     */
    private static void responseJson(HttpServletResponse res, JSONObject jsonObject) {
        res.setCharacterEncoding(UTF8);
        res.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        PrintWriter out = null;

        try {
            out = res.getWriter();
            out.append(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }


}
