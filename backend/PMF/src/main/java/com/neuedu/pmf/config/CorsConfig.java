package com.neuedu.pmf.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    // 跨域配置
    private CorsConfiguration corsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");//允许任何来源访问
        config.setAllowCredentials(true);//允许携带任何cookie
        config.addAllowedHeader("*");//允许任何请求头
        config.addAllowedMethod("*");//允许任何HTTP方法
        config.addExposedHeader("Authorization");
        config.setMaxAge(3600L);
        return config;
    }

    // 关键：用 FilterRegistrationBean 设置最高优先级
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig());

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CorsFilter(source));

        // 优先级最高（0 比任何过滤器都先执行）
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }
}