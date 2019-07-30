# SpringMvc Web 开发实战之 Curd

## 首页

#### 静态资源访问

首页访问的是静态资源文件夹下的 index.html 而不是 template 目录下的 index.html

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

会发现当没有 `WebMvcConfigurationSupport` 类，而且类路径下存在 ` Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class` 这些类的时候会自动配置，所以我们可以通过实现 `WebMvcConfigurer` 接口来扩展MVC 的配置。

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

#### 静态资源被拦截

但当我们测试运行后会发现，拦截器不仅拦截了 main.html 请求，同时也拦截了**静态资源**。Spring 2.x 中使用拦截器会拦截所有的请求，包括静态资源，而在 SpringBoot 1.x 中则会对静态资源进行处理，放行对静态资源的处理。

示例代码：

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoginHandlerInterceptor())
        .excludePathPatterns("/", "/login.html", "/user/login", "/asserts/**", "/favicon.ico")
        .addPathPatterns("/**");
```

这样可以就可以访问到静态资源了，但是这样会使得控制器 mapping 的 url 不能以 asserts 开头。

> Spring 提供的 API 有误：
>
> ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190723091738.png)
>
> 详情：[Github issues](https://github.com/spring-projects/spring-boot/issues/12313)

#### Favicon.ico 找不到

然后还有一个问题会出现，那就是我们自定义的 favicon.ico 图标会失效，这种情况是由于我们设置了 `server.servlet.context-path=` ，但浏览器请求 favicon.ico 仍然会请求 localhost:8080/favicon.ico。导致出现 404 错误，不显示图标。

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190724100436.png)

但我们可以通过添加 Html（Thymeleaf） 代码来解决，手动指定图标：

```html
<link rel="icon" th:href="@{/favicon.ico}" type="image/x-icon" />
<link rel="shortcut icon" th:href="@{/favicon.ico}" type="image/x-icon" />
```



## 员工列表

实验要求：

- RestfulCRUD

  - 满足 Rest 风格：/资源名称/资源标示 HTTP 请求方式区分对资源的 CRUD 操作

  | 操作 | 普通 CRUD         | RestfuiCRUD         |
  | ---- | ----------------- | ------------------- |
  | 查询 | getEmp            | emp-GET             |
  | 添加 | addEmp?xxx        | emp----POST         |
  | 修改 | updateEmp?id=xxxx | emp/{id} --- PUT    |
  | 删除 | deleteEmp?id=1    | emp/{id} --- DELETE |

- 实验的请求架构

  |                                      | 请求 URI | 请求方式 |
  | ------------------------------------ | -------- | -------- |
  | 查询所有员工                         | emps     | GET      |
  | 查询某个员工                         | emp/{id} | GET      |
  | 添加页面                             | emp      | GET      |
  | 添加员工                             | emp      | POST     |
  | 来到修改页面（查出员工进行信息回显） | emp/{id} | GET      |
  | 修改员工                             | emp/{id} | PUT      |
  | 删除员工                             | emp/{id} | DELETE   |

- 员工列表

  我们在实验中会发现，很多页面都含有公告部分，那么我们为了开发和维护，可以将公共的页面抽取出来：

  ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190724113202.png)

  #### 那么这就用到了 Thymeleaf 公共页面抽取

  1. 抽取公共片段

     ```html
     <!DOCTYPE html>
     <html xmlns:th="http://www.thymeleaf.org">
     <body>
     <div th:fragment="copy">
     &copy; 2011 The Good Thymes Virtual Grocery
     </div>
     </body>
     </html>
     ```

  2. 引入公共片段

     ```html
     <body>
     ...
     <div th:insert="~{footer :: copy}"></div>
     </body>
     ```

     `~{templatename::selector}`  模板名：选择器

     ` ~{templatename::fragmentname}` 模板名：片段名

     其中，`~{}` 可以省略。

     ---

     

     那么在 Thymeleaf 中有三种插入方式：
     
     `th:insert ` ：将公共片段整个插入到声明的引入的元素中
     
     `th:replace` ：将声明引入的元素替换为公共片段
     
     `th:include ` ：将被引入的片段的内容包含到这个标签中
     
     ```html
     <footer th:fragment="copy">
     &copy; 2011 The Good Thymes Virtual Grocery
     </footer>
     ```
     
     ```html
     <body>
     ...
     <div th:insert="footer :: copy"></div>
     <div th:replace="footer :: copy"></div>
     <div th:include="footer :: copy"></div>
     </body>
     
     <!-- 实际上 -->
     
     <body>
     ...
     <div>
     <footer>
     &copy; 2011 The Good Thymes Virtual Grocery
     </footer>
     </div>
     <footer>
     &copy; 2011 The Good Thymes Virtual Grocery
     </footer>
     <div>
     &copy; 2011 The Good Thymes Virtual Grocery
     </div>
     </body>
     ```
     
     除了声明片段来引用之外，我们还可以使用选择器来引用：
     
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190724142359.png)
     
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725103311.png)
     
     
     
  3. 引入片段的时候传入参数
  
     可以看到，当查看员工列表时，侧边栏本应该是 **员工管理** 被激活，高亮显示，我们如果想要用 Thymeleaf 来实现的话，可以在引用片段时传入参数，实现动态效果。
  
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725103408.png)
  
     我们可以先把公用的页面放入一个页面，在引用片段时传入参数，通过参数判断是否添加 `active` 类。
  
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190724145358.png)
  
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190724145511.png)
  
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190724145540.png)
  
     
  
  4. 添加员工
  
     在添加员工时，需要注意时间的数据格式。
  
     日期的格式化：SpringMvc 将页面提交的值转换为指定的类型
  
     eg: 2017-12-12 ---> Date：会发生数据转换、类型转换
  
     **默认的时间格式**为（WebMvcProperties.java）：![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725103759.png)
  
     如果时间的格式不是默认所规定的格式，也没有修改配置，那么会发生错误：
  
     ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725103823.png)
  
     可以修改配置文件来指定时间格式：
  
     ```properties
     spring.mvc.date-format=yyyy-MM-dd
     ```
  
  5. 修改员工信息
  
     为了简单我们直接重用添加和修改页面，将员工信息和公寓信息放入 model，根据是否具有员工对象判断是那种页面。
  
     但是由于我们遵循 Restful 风格的 api 设计，修改员工发送的是 PUT 请求，而表单呢则只支持发送 GET 和 PUT 请求。
  
     我们可以使用 SpringMvc 的 `HiddenHttpMethodFilter` 将请求转成我们需要的请求。
  
     具体操作为：
  
     1. SpringMvc 中配置 HiddeHttpMethodFilter （SpringBoot 自动配置好了）
     2. 页面创建一个 post 表单
     3. 创建一个 input 项，`name=_method'` ，其值就是我们指定的请求方式
  
     ```html
     <input type="hidden" name="_method" value="put" th:if="${emp != null}" />
     ```
  
  6. 删除员工信息
  
     ```html
     <td>
         <a class="btn-sm btn-primary" th:href="@{'/emp/' + ${emp.id}}">编辑</a>
         <form th:action="@{'/emp/' + ${emp.id}}" method="post">
             <input type="hidden" name="_method" value="delete" />
             <button type="submit" class="btn-sm btn-danger" style="color: #ffffff;">删除</button>
         </form>
     </td>
     ```
  
     也是使用上面的方法，将 POST 请求转换为 DELETE 请求。
  
     

