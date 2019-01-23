package apijson.demo.server.utils;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WEB相关的工具方法。
 */
public class WebUtils {

    private static final Logger logger = LoggerFactory.getLogger(WebUtils.class);
    /**
     * 代理的名称,也代理了判断的顺序..
     */
    private static final String[] AGENT_INDEX = {
            "MSIE", "Firefox", "Chrome", "Opera", "Safari"
    };
    /**
     * 存放用户代理解析的正则容器.
     */
    private static final Map<String, Pattern> AGENT_PATTERNS =
            ImmutableMap.of(AGENT_INDEX[0], Pattern.compile("MSIE ([\\d.]+)"),
                    AGENT_INDEX[1], Pattern.compile("Firefox/(\\d.+)"),
                    AGENT_INDEX[2], Pattern.compile("Chrome/([\\d.]+)"),
                    AGENT_INDEX[3], Pattern.compile("Opera[/\\s]([\\d.]+)"),
                    AGENT_INDEX[4], Pattern.compile("Version/([\\d.]+)"));

    private WebUtils() {
    }

    /**
     * 取实际用户的访问地址。
     *
     * @param request 当前请求。
     * @return 客户端IP地址。
     */

    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        //处理多级代理ip异常
        return doMultiLevelAgencyIp(ip);
    }
    /***
     * 取首个IP地址(多个ip情况下)
     * @param oldClientIp
     * @return
     */
    private static String doMultiLevelAgencyIp(String oldClientIp){
        String newClientIp = oldClientIp;
        //如果ip地址大于15位，则为多IP
        if(oldClientIp.length()>15&&oldClientIp.contains(",")){
            newClientIp = oldClientIp.split(",")[0].trim();
        }
        return newClientIp;
    }
    /**
     * 查找指定请求中的指定名称的Cookie。
     *
     * @param request 请求。
     * @param name    cookie名称。
     * @return 如果有相应名称的Cookie，则返回相应Cookie实例。没有返回null。
     */
    public static Cookie findCookie(HttpServletRequest request, String name) {
        if (request != null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(name)) {
                        return cookie;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 查找指定请求中的指定名称Cookie的值，如果不存在将返回null。
     *
     * @param request 请求。
     * @param name    Cookie名称。
     * @return cookie的值。
     */
    public static String findCookieValue(HttpServletRequest request, String name) {
        Cookie cookie = findCookie(request, name);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * 增加一个Cookie,使用默认域名。
     *
     * @param request  请求。
     * @param response 响应。
     * @param name     Cookie名称 。
     * @param value    Cookie的值。
     * @param maxAge   生命周期。
     */
    public static void addCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            String name,
            String value,
            int maxAge) {
        addCookie(request, response, name, value, null, maxAge, false);
    }

    /**
     * 增加一个Cookie,使用指定域名。
     *
     * @param request  请求。
     * @param response 响应。
     * @param name     Cookie名称 。
     * @param value    Cookie的值。
     * @param maxAge   生命周期。
     */
    public static void addCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            String name,
            String value,
            String domain,
            int maxAge, boolean httpOnly) {
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "/";
        }
        addCookie(request, response, name, value, domain, contextPath, maxAge, httpOnly);
    }

    /**
     * 增加一个Cookie.ContextPath如果为空或者长度为0，将使用"/".
     *
     * @param request     当前请求。
     * @param response    当前响应。
     * @param name        cookie名称
     * @param value       cookie值
     * @param domain      cookie域名
     * @param contextPath cookie路径。
     * @param maxAge      有效时间。
     */
    public static void addCookie(HttpServletRequest request, HttpServletResponse response,
                                 String name, String value, String domain, String contextPath, int maxAge, boolean httpOnly) {
        if (request != null && response != null) {
            Cookie cookie = new Cookie(name, value);
            cookie.setMaxAge(maxAge);
            cookie.setSecure(request.isSecure());

            if (contextPath == null || contextPath.isEmpty()) {
                cookie.setPath("/");
            } else {
                cookie.setPath(contextPath);
            }

            if (domain != null && !domain.isEmpty()) {
                cookie.setDomain(domain);
            }

            if (httpOnly) {
                cookie.setHttpOnly(true);
            }

            response.addCookie(cookie);

            logger.debug("Cookie update the sessionID.[name={},value={},maxAge={},httpOnly={},path={},domain={}]",
                    cookie.getName(), cookie.getValue(), cookie.getMaxAge(), httpOnly, cookie.getPath(),
                    cookie.getDomain());
        }
    }

    /**
     * 失效一个Cookie.
     *
     * @param request     当前请求。
     * @param response    当前响应。
     * @param name        Cookie名称。
     * @param domain      Cookie域名。
     * @param contextPath 有效路径。
     */
    public static void failureCookie(HttpServletRequest request, HttpServletResponse response,
                                     String name, String domain, String contextPath) {
        if (request != null && response != null) {
            addCookie(request, response, name, null, domain, contextPath, 0, true);
        }
    }

    /**
     * 将指定的Cookie失效掉。
     *
     * @param request  请求
     * @param response 响应。
     * @param name     cookie名称。
     * @param domain   cookie的域名。
     */
    public static void failureCookie(HttpServletRequest request,
                                     HttpServletResponse response, String name, String domain) {
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "/";
        }
        failureCookie(request, response, name, domain, contextPath);
    }

    /**
     * 将指定的Cookie失效掉。
     *
     * @param request  请求
     * @param response 响应。
     * @param name     cookie名称。
     */
    public static void failureCookie(HttpServletRequest request,
                                     HttpServletResponse response, String name) {
        failureCookie(request, response, name, null);
    }

    /**
     * 获取请求的完整地址,包括参数。
     *
     * @param request 请求。
     * @return 完整地址。
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        StringBuilder buff = new StringBuilder(
                request.getRequestURL().toString());
        String queryString = request.getQueryString();
        if (queryString != null) {
            buff.append("?").append(queryString);
        }

        return buff.toString();
    }

    /**
     * 一个客户端转向的方便工具方法.可以选择使用301或者302方式进行跳转.
     *
     * @param response        当前响应.
     * @param url             需要转向的地址.
     * @param movePermanently true表示进行301永久跳转,false表示302临时跳转.
     * @throws java.io.IOException I/O异常.
     */
    public static void redirect(HttpServletResponse response, String url,
                                boolean movePermanently) throws IOException {
        if (!movePermanently) {
            response.sendRedirect(url);
        } else {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", url);
        }
    }

    /**
     * 获取用户代理信息.
     *
     * @param userAgent 用户代理信息字符串.
     * @return 用户代理信息.
     */
    public static UserAgent getUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }

        for (String aAGENT_INDEX : AGENT_INDEX) {
            Pattern pattern = AGENT_PATTERNS.get(aAGENT_INDEX);
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                return new UserAgent(aAGENT_INDEX, matcher.group(1));
            }
        }
        return null;
    }

    /**
     * 获取指定请求中的用户代理.
     *
     * @param request 请求.
     * @return 用户代理信息.
     */
    public static UserAgent getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgentHead = request.getHeader("User-Agent");
        return getUserAgent(userAgentHead);
    }

    /**
     * 表示一个用户代理的信息.
     */
    public static class UserAgent {

        private String name = "";
        private String version = "";

        /**
         * 构造一个用户代理信息.
         *
         * @param name    代理名称.
         * @param version 代理版本号.
         */
        public UserAgent(String name, String version) {
            this.name = name;
            this.version = version;
        }

        /**
         * 获取代理名称.
         *
         * @return 代理名称.
         */
        public String getName() {
            return name;
        }

        /**
         * 获取版本号.
         *
         * @return 版本号.
         */
        public String getVersion() {
            return version;
        }

    }
}
