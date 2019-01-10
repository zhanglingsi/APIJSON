package apijson.demo.server.config;

import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.utils.JwtUtils;
import com.zhangls.apijson.utils.StringUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by zhangls on 2019/1/8.
 *
 * @author zhangls
 */
@Slf4j
@Component
@WebFilter(urlPatterns = "/**", filterName = "standardFilter")
public class StandardFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("【web容器初始化，调用此方法，预制方法，可初始化数据等操作】");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.info("【web容器Filer处理开始，处理全局拦截，如XXS跨站脚本过滤，打印请求日志，记录请求重要信息入库等】");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;


        log.debug("【####请求信息#####################################################################】");
        log.debug("【客户机IP地址】：{}", request.getRemoteAddr());
        log.debug("【客户机端口号】：{}", request.getRemotePort());
        log.debug("【客户机主机名】：{}", request.getRemoteHost());
        log.debug("【访问全路径】：{}", request.getRequestURL());
        log.debug("【访问资源路径】：{}", request.getRequestURI());
        log.debug("【RequestedSessionId】：{}", request.getRequestedSessionId());
        log.debug("【服务端端口ServerPort】：{}", request.getServerPort());
        log.debug("【服务端主机名ServerName】：{}", request.getServerName());
        log.debug("【请求方法Method】：{}", request.getMethod());
        log.debug("【####请求信息#####################################################################】");

        log.debug("【####请求头信息#####################################################################】");
        Enumeration<String> reqHeadInfo = request.getHeaderNames();
        while (reqHeadInfo.hasMoreElements()) {
            String headName = String.valueOf(reqHeadInfo.nextElement());
            String headValue = request.getHeader(headName);
            log.debug("【请求头信息】【{}】：【{}】", headName, headValue);
        }

        log.debug("【####请求头信息#####################################################################】");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("【web容器Filer销毁方法调用】");
    }
}
