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

    /**
     * 受保护的url
     */
    private static final String[] INCLUDE_PATH_PATTERNS = {"/api", "/jwt"};


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("【web容器Filer初始化方法调用】");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.info("【web容器Filer处理开始】");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 0:0:0:0:0:0:0:1
        log.info("【request对象属性RemoteAddr】：{}", request.getRemoteAddr());
        // 0:0:0:0:0:0:0:1
        log.info("【request对象属性RemoteHost】：{}", request.getRemoteHost());
        // 59883
        log.info("【request对象属性RemotePort】：{}", request.getRemotePort());

        // /api/loginNew/get
        log.info("【request对象属性ServletPath】：{}", request.getServletPath());
        // ""
        log.info("【request对象属性 ContextPath】：{}", request.getContextPath());

        // http://localhost:9090/api/loginNew/get
        log.info("【request对象属性 RequestURL】：{}", request.getRequestURL());
        // /api/loginNew/get
        log.info("【request对象属性 RequestURI】：{}", request.getRequestURI());
        // D02D2F5CB951AD64805475B2DF301D9F
        log.info("【request对象属性 RequestedSessionId】：{}", request.getRequestedSessionId());

        // HTTP/1.1
        log.info("【request对象属性 Protocol】：{}", request.getProtocol());
        // org.apache.tomcat.util.http.NamesEnumerator@3b9643c4
        log.info("【request对象属性 HeaderNames】：{}", request.getHeaderNames());
        // 9090
        log.info("【request对象属性 ServerPort】：{}", request.getServerPort());
        // localhost
        log.info("【request对象属性 ServerName】：{}", request.getServerName());
        // POST
        log.info("【request对象属性 Method】：{}", request.getMethod());


        // 遇到options请求 直接返回
        if (RequestMethod.OPTIONS.equals(request.getMethod())){
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(req, res);
        }


        String uri = request.getRequestURI();
        // 受保护的URL  jwt 验证 token
        List<String> ls = Arrays.asList(INCLUDE_PATH_PATTERNS);

        for (String str : ls){
            if(uri.contains(str)){

                String authHeader = request.getHeader(UtilConstants.Jwt.JWT_AUTHOR);
                if (StringUtil.isEmpty(authHeader, Boolean.TRUE) || !authHeader.startsWith(UtilConstants.Jwt.JWT_BEARER)) {
                    log.info("【JWT-TOKEN 获取Request Header信息 Error】：{}", authHeader);
                    throw new ServletException("Missing or invalid Authorization header.");
                }

                String token = authHeader.substring(UtilConstants.Jwt.JWT_BEARER.length());

                try {
                    Claims claims = JwtUtils.getToken(token);
                    request.setAttribute(UtilConstants.Jwt.JWT_USER_INFO, claims);
                } catch (ExpiredJwtException e){
                    log.info("【过期的JWT-TOKEN】：{}", token);
                    throw new ServletException("token expired.");
                }catch (SignatureException e) {
                    log.info("【错误的JWT-TOKEN】：{}", token);
                    throw new ServletException("Invalid token.");
                } catch (Exception e){
                    log.info("【获取JWT-TOKEN Error】：{}", token);
                    throw new ServletException("Error token.");
                }

            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("【web容器Filer销毁方法调用】");
    }
}
