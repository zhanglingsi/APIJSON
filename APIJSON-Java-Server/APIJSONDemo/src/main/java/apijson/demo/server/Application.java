package apijson.demo.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * application
 *
 * @author Lemon
 */
@SpringBootApplication
@MapperScan("apijson.demo.server.mapper")
public class Application {

    @Bean
    public CharacterEncodingFilter filter () {
        CharacterEncodingFilter filter =new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
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
