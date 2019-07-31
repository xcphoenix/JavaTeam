# 配置嵌入式 Servlet 容器

SpringBoot 默认使用嵌入式的 Servlet 容器（Tomcat）

![项目pom.xml依赖图](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190730211539.png)



## 定制和修改 Servlet 容器的相关设置

### 修改和 server 有关的配置

这部分的配置与 **`ServerProperties`** 类相关联，例如：

```properties
server.port=8081
server.servlet.context-path=/crud
```

可以分为这么几类：

- 通用的 Servlet 容器设置

  `server.xxx`

- Tomcat 的设置

  `server.tomcat.xxx`
  
  

### 编写嵌入式的 Servlet 容器的定制器

在 SpringBoot 2.0 之后，实现对嵌入式 Servlet 容器的配置需要在` WebServerFactoryCustomizer` 接口中使用 `ConfigurableWebServerFactory` 对象重写 `customize` 方法。

```java
@Bean
public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
    // 定制嵌入式容器的相关规则
    return new WebServerFactoryCustomizer<ConfigurableWebServerFactory>() {
        @Override
        public void customize(ConfigurableWebServerFactory factory) {
            factory.setPort(8086);
        }
    };
}
```



### 注册 Servlet 的三大组件：Servlet、Filter、Listener

由于 SpringBoot 默认以 jar 包的方式启动嵌入式的 Servlet 容器来启动 SpringBoot 的 Web 应用，没有 web.xml 文件，所以注册组件有以下方式：

- `ServletRegistrationBean`

- `FilterRegistrationBean`

- `ServletListenerRegistrationBean`

```java
/**
     * 注册三大组件
     */
@Bean
public ServletRegistrationBean<MyServlet> myServlet() {
    return new ServletRegistrationBean<>(new MyServlet(), "/myServlet");
}

@Bean
public FilterRegistrationBean<MyFilter> myFilter() {
    FilterRegistrationBean<MyFilter> registrationBean = new FilterRegistrationBean<MyFilter>();
    registrationBean.setFilter(new MyFilter());
    registrationBean.setUrlPatterns(Arrays.asList("/hello", "/myServlet"));
    return registrationBean;
}

@Bean
public ServletListenerRegistrationBean<MyListener> myListener() {
    return new ServletListenerRegistrationBean<>(new MyListener());
}
```

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190731100034.png)



SpringBoot 帮我们自动 SpringMvc 自动 SpringMvc 的时候，自动的注册 SpringMvc 的前端控制器：DispatcherServlet。

```java
@Bean(name = DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
@ConditionalOnBean(value = DispatcherServlet.class, name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
public DispatcherServletRegistrationBean dispatcherServletRegistration(DispatcherServlet dispatcherServlet) {
    DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(dispatcherServlet,
                                                                                           this.webMvcProperties.getServlet().getPath());
    registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
    registration.setLoadOnStartup(this.webMvcProperties.getServlet().getLoadOnStartup());
    if (this.multipartConfig != null) {
        registration.setMultipartConfig(this.multipartConfig);
    }
    return registration;
}
```



## 使用其他的 Web 容器

Jetty（更适合长连接）

Undertow（不支持 JSP，性能好）

SpringBoot 支持更换默认的 Web 容器，支持的容器：![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190731101051.png)



我们在 pom.xml 的依赖树里排除 tomcat-starter，加入其他容器的 starter 就可以更换 Web 容器了:

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190731101605.png)



## 使用外置的 Servlet 容器

嵌入式 Servlet 容器：

- 简单、便携
- 默认不支持 jsp、**优化定制**比较复杂
  - 使用定制器
  - 自己编写嵌入式容器的创建工厂

我们可以使用外置的 Servlet 容器，在外面安装应用程序的前提下，将应用打包为 War 包。



1. 我们在创建 SpringBoot 应用的时候，`package` 选择 ***war***，创建之后我们会发现包的根路径下会多出一个文件：

    ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190731112234.png)

    但 src 下面还没与 webapp 文件，我们在项目结构中的 ***Modules*** 双击下面的 ***Web Resource Directory***，生成文件夹，点击上面的加号创建 web.xml 文件。

    ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190731112944.png)

    最后生成的目录结构为：

    ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190731112858.png)

2. 将嵌入式 tomcat 容器指定为 `provided` 

3. 编写一个 `SpringBootServletInitializer` 子类，调用 `configure` 方法

   ```java
   public class ServletInitializer extends SpringBootServletInitializer {
   
       @Override
       protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
           // 传入主程序
           return application.sources(SpringBoot04WebJspApplication.class);
       }
   
   }
   ```

4. 启动服务器