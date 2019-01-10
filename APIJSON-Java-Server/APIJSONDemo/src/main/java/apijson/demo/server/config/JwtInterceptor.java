package apijson.demo.server.config;

import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.utils.JwtUtils;
import com.zhangls.apijson.utils.StringUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhangls on 2019/1/9.
 * @author zhangls
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * 预处理回调方法，实现处理器的预处理
     * 返回值：true表示继续流程；false表示流程中断，不会继续调用其他的拦截器或处理器
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        log.info("【JwtInterceptor请求拦截器预处理方法】：{}", "preHandle");
        log.info("【JwtInterceptor请求拦截器request方法】：{}", req.getMethod());

        String authHeader = req.getHeader(UtilConstants.Jwt.JWT_AUTHOR);

        if (StringUtil.isEmpty(authHeader, Boolean.TRUE) || !authHeader.startsWith(UtilConstants.Jwt.JWT_BEARER)) {
            log.info("【JWT-TOKEN 获取Request Header信息 Error】：{}", authHeader);
            throw new ServletException("Missing or invalid Authorization header.");
        }

        String token = authHeader.substring(UtilConstants.Jwt.JWT_BEARER.length());

        try {
            Claims claims = JwtUtils.parseJWT(token);
            req.setAttribute(UtilConstants.Jwt.JWT_USER_INFO, claims);
        } catch (ExpiredJwtException e) {
            log.info("【过期的JWT-TOKEN】：{}", token);
            throw new ServletException("token expired.");
        } catch (SignatureException e) {
            log.info("【错误的JWT-TOKEN】：{}", token);
            throw new ServletException("Invalid token.");
        } catch (Exception e) {
            log.info("【获取JWT-TOKEN Error】：{}", token);
            throw new ServletException("Error token.");
        }

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
}