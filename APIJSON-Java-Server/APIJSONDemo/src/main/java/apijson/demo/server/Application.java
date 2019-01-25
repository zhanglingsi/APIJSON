package apijson.demo.server;

import apijson.demo.server.config.ZreContextValueFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * application
 *
 * @author Lemon
 */
@Configuration
@SpringBootApplication
@MapperScan("apijson.demo.service.mapper")
public class Application extends WebMvcConfigurerAdapter {

    @Bean
    public CharacterEncodingFilter filter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    /**
     * （1）在启动类App.java类中继承：WebMvcConfigurerAdapter
     * <p>
     * （2）覆盖方法：configureContentNegotiation
     * <p>
     * <p>
     * <p>
     * favorPathExtension表示支持后缀匹配，
     * <p>
     * 属性ignoreAcceptHeader默认为fasle，表示accept-header匹配，defaultContentType开启默认匹配。
     * <p>
     * 例如：请求aaa.xx，若设置<entry key="xx" value="application/xml"/> 也能匹配以xml返回。
     * <p>
     * 根据以上条件进行一一匹配最终，得到相关并符合的策略初始化ContentNegotiationManager
     */

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//        configurer.favorPathExtension(false);
//        configurer.useJaf(false);
    }

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        //创建FastJson信息转换对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //创建FastJson对象并设定序列化规则
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //添加自定义valueFilter
        fastJsonConfig.setSerializeFilters(new ZreContextValueFilter());
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        //规则赋予转换对象
        fastConverter.setFastJsonConfig(fastJsonConfig);

        return new HttpMessageConverters(fastConverter, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);

//		System.out.println("\n\n\n\n\n<<<<<<<<<<<<<<<<<<<<<<<<< APIJSON >>>>>>>>>>>>>>>>>>>>>>>>\n");
//		System.out.println("开始测试:远程函数 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
//		try {
//			DemoFunction.test();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("\n完成测试:远程函数 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//
//
//		System.out.println("\n\n\n开始测试:请求校验 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
//		try {
//			StructureUtil.test();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("\n完成测试:请求校验 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//
//		System.out.println("\n\n<<<<<<<<<<<<<<<<<<<<<<<<< APIJSON已启动 >>>>>>>>>>>>>>>>>>>>>>>>\n");
    }


    /**
     * 跨域过滤器
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    /**
     * CORS跨域配置
     *
     * @return
     */
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许的域名或IP地址
        corsConfiguration.addAllowedOrigin("*");
        //允许的请求头
        corsConfiguration.addAllowedHeader("*");
        //允许的HTTP请求方法
        corsConfiguration.addAllowedMethod("*");
        //允许发送跨域凭据，前端Axios存取JSESSIONID必须要
        corsConfiguration.setAllowCredentials(true);

        return corsConfiguration;
    }

}
