/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepcover.datacenter.service.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Swagger2 配置类
 * <p>
 * http://127.0.0.1:8081/precision/doc.html
 *
 * <p></p>
 *
 * @Author: jiuxi
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig extends WebMvcConfigurerAdapter {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(true)
                .apiInfo(apiInfo())
                .groupName("datacenter")
                .select()
                // 为当前包路径
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
//                .build().globalOperationParameters(getParameterList());
                .build();
    }
    /**
     * 构建 api 文档的详细信息函数, 注意这里的注解引用的是哪个
     *
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 页面标题
                .title("项目接口文档")
                // 创建人
                .contact(new Contact("jiangli", "", "deepcover@example.com"))
                // 版本号
                .version("1.0")
                // 描述
                .description("接口文档")
                .build();
    }
    /**
     * 标准化工程结合 Swagger2，访问显示 404
     * 有可能是某处把默认的静态资源路径覆盖了
     * 目前尝试手动设置再重新指定一次
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
//    /**
//     * header 参数配置
//     *
//     * @return
//     */
//    private List<Parameter> getParameterList() {
//        List<Parameter> ps = new ArrayList<>();
//        ParameterBuilder pb = new ParameterBuilder();
//        pb.name("X-Tsign-Open-App-Id").description("appID").modelRef(new ModelRef("string"))
//                .parameterType("header").required(false).build();
//        ps.add(pb.build());
//        return ps;
//    }
}