package com.haiyisoft.cross.region.common.swagger;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author CLY
 * @date 2022/4/21 19:27
 **/
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:NA}")
    private String active;

    @Bean
    public Docket createRestApi(){
        // 访问地址为http://localhost:28888/swagger-ui/index.html
        return new Docket(DocumentationType.OAS_30)
                .enable(!"prod".equals(active)) // 非生产环境才启动
                .apiInfo(apiInfo()) // 用于生成标题头信息
                .groupName("api测试组1") // 为了避免混乱，可以创建多个分组（创建多个Docket Bean即可），不同的成员使用不同的分组，配置不同的api()，监控不同的接口
                .select() // select()返回一个ApiSelectorBuilder，用来控制接口被swagger做成文档
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)) //指定扫描被@ApiOperation注解的方法
                .paths(PathSelectors.any()) //选择所有path，可以配置只为部分path生成文档，比如PathSelectors.ant("/kuang/**")，只扫描以/kuang开头的接口
                .build()
                .securitySchemes(securitySchemes()) // 安全方案，比如为每一个请求带上Authorization请求头，用于JWT认证
                .securityContexts(securityContext()); // 安全上下文

    }

    private List<SecurityScheme> securitySchemes() {
        // 定义了一个名为"Authorization"的API密钥方案，它需要在HTTP头中包含"Authorization"字段才能访问API。
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
        return Arrays.asList(apiKey);
    }

    // securityContexts则用于将安全要求应用于特定的API操作或资源
    private List<SecurityContext> securityContext() {
        final SecurityContext securityContext = SecurityContext.builder()
                .securityReferences(securityReference())
                .operationSelector(new Predicate<OperationContext>() {
                    @Override
                    public boolean test(OperationContext operationContext) {
                        // 对所有的请求应用安全上下文，或者指定请求/user.*
                        return operationContext.requestMappingPattern().matches("/.*");
                    }
                }).build();
        return Arrays.asList(securityContext);
    }

    // 我们定义了一个安全上下文，它要求使用"defaultAuth"方法中定义的"Authorization"方案才能访问匹配"/.*"的路径。
    // 我们还为方案定义了一个授权范围，即"accessEverything"，表示该方案可以访问所有资源。
    private List<SecurityReference> securityReference() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        SecurityReference securityReference = new SecurityReference("Authorization",authorizationScopes);
        return Arrays.asList(securityReference);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(applicationName+"接口文档").description("swagger的API文档")
                .version("1.0").build();
    }



}
