# SpringMvc Web 开发实战之 Curd

## 首页

#### 首页访问的是静态资源文件夹下的 index.html 而不是 template 目录下的 index.html

**解决方法 1**：

```java
@Controller
public class HelloController {

    @RequestMapping({"/", "/index.html"})
    public String index() {
        return "index";
    }
```

**缺陷**：需要为每个页面都写一个空方法。

**解决方法2**：

当我们查看 SpringMvc 自动配置的源码时：

```java
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
		ValidationAutoConfiguration.class })
public class WebMvcAutoConfiguration {
```

会发现当没有 `WebMvcConfigurationSupport` 类，而且类路径下存在 ` Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class` 这些类的时候会自动配置，所以我们可以通过高实现 `WebMvcConfigurer` 接口来扩展MVC 的配置。

```java
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    // 将自定义配置放在容器中，让自动配置类将自定义配置与默认配置组合在一起
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                System.out.println("WebMvcConfigurer start...");
                registry.addViewController("/").setViewName("index");
                registry.addViewController("/index.html").setViewName("index");
            }
        };
    }

}
```

由此，我们可以实现修改首页显示为 template 目录下的 index.html。



## 登录

错误消息的显示：

使用 Thymeleaf 所提供的工具对象，以及比 `th:text` 优先级高的 `th:if` 来动态显示错误消息；

```html
<p style="color: red;" th:text="${msg}" th:if="${not #strings.isEmpty(msg)}"></p>
```

#### 页面缓存

开发期间，当我们更改了页面时，有时候会发现页面并没有发生变化，这时我们需要**禁用模板引擎的缓存**。

```properties
# 禁用缓存
spring.thymeleaf.cache=false
```

而且由于Idea 并不是实时刷新的，所以我们可以按住 <kbd>Ctrl+Shift+F9</kbd> **重新编译**下。

#### 表单重复提交

当登录成功后，当我们 <kbd>F5</kbd> 刷新的时候，会发现浏览器提示表单重复提交：

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190722170501.png)

这是登录成功后转发到这个页面，当我们刷新的时候，发送的还是上一次的请求，所以最简单的解决方法就是使用重定向。

````java
if (!StringUtils.isEmpty(username) && "123456".equals(password)) {
    return "redirect:/main.html";
} else {
    // 登录失败
    map.put("msg", "密码错误");
    return "login";
}
````

#### 拦截器

我们可以添加拦截器来拦截需要用户登录的页面。

```java
@Bean
public WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/").setViewName("login");
            registry.addViewController("/login.html").setViewName("login");
            registry.addViewController("/main.html").setViewName("dashboard");
        }

        // 添加拦截器
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new LoginHandlerInterceptor())
                .excludePathPatterns("/", "/login.html", "/user/login")
                .addPathPatterns("/**");
        }

    };
```

```java
public class LoginHandlerInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object user = request.getSession().getAttribute("loginUser");
        String prefixInfo = ">>> " + request.getRequestURL() + " :: ";

        if (user == null) {
            logger.info(prefixInfo + "被拦截!");
            request.setAttribute("msg", "你还没有登录");
            request.getRequestDispatcher("/login.html").forward(request, response);
            return false;
        } else {
            logger.info(prefixInfo + "放行！");
            return true;
        }
    }
```

但当我们测试运行后会发现，拦截器不仅拦截了 main.html 请求，同时也拦截了**静态资源**。Spring 2.x 中使用拦截器会拦截所有的请求，包括静态资源，而在 SpringBoot 1.x 中则会对静态资源进行处理，放行对静态资源的处理。

示例代码：

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoginHandlerInterceptor())
        .excludePathPatterns("/", "/login.html", "/user/login", "/asserts/**", "/favicon.ico")
        .addPathPatterns("/**");
```

这样可以就可以访问到静态资源了，但是这样会使得控制器 mapping 的 url 不能以 asserts 开头，同时在测试中发现 SpringBoot 的 favicon.ico 也出现问题，自定义的图标和默认的小绿叶图标都会失效。

> Spring 提供的 API 有误：
>
> ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190723091738.png)
>
> 详情：[Github issues](https://github.com/spring-projects/spring-boot/issues/12313)

