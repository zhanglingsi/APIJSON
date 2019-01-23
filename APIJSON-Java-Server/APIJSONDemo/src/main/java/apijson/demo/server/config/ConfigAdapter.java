package apijson.demo.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Created by zhangls on 2019/1/8.
 * @author zhangls
 */
@Configuration
public class ConfigAdapter extends WebMvcConfigurationSupport {

    @Autowired
    private StandardInterceptor standardInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 可配置多个拦截器
        registry.addInterceptor(standardInterceptor).addPathPatterns("/**");
//        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/**");

        super.addInterceptors(registry);
    }

}
