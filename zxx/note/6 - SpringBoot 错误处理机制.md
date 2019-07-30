# **Spring** Boot 错误处理机制

## 默认的错误页面

错误页面默认效果：（浏览器返回错误页面）

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725104806.png)

POSTMAN 测试：（返回 JSON 数据）

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725105153.png)

## 原理

可以参照 ***ErrorMvcAutoConfiguration*** 查看错误处理的自动配置

自动配置为容器中添加了下列组件：

- `DefaultErrorAttributes`
- `BasicErrorController`
- `ErrorPageCustomizer`
- `DefaultErrorViewResolver`



处理的步骤：

1. 一旦系统出现 4xx 或 5xx 之类的错误，`ErrorPageCustomizer` 就会生效（定制错误的响应规则）

   ```java
   @Override
   public void registerErrorPages(ErrorPageRegistry errorPageRegistry) {
       ErrorPage errorPage = new ErrorPage(
          
        // 出现错误后，根据配置查找 error.path 的值，来到指定的请求进行处理（类似于 web.xml 定制的错误规则）
           this.dispatcherServletPath.getRelativePath(this.properties.getError().getPath()));
       errorPageRegistry.addErrorPages(errorPage);
   }
   
   // Class: ErrorProperties ------------------------
   /**
    * Path of the error controller.
    */
   @Value("${error.path:/error}")
   private String path = "/error";
   
   public String getPath() {
       return this.path;
   }
   
   ```

2. 之后由 `BasicErrorController` 来处理错误请求

   ```java
   @Controller
   @RequestMapping("${server.error.path:${error.path:/error}}")
   public class BasicErrorController extends AbstractErrorController {
   
   ```

   然后由这两个方法分别进行处理：

   ```java
   // 产生 text/html 类型的数据
   @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
   public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
       // 获取状态码和model
       HttpStatus status = getStatus(request);
       Map<String, Object> model = Collections
           .unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
       response.setStatus(status.value());
       ModelAndView modelAndView = resolveErrorView(request, response, status, model);
       // 判断去哪个页面，
       return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
   }
   
   // 产生 json 数据
   @RequestMapping
   public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
       Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
       HttpStatus status = getStatus(request);
       return new ResponseEntity<>(body, status);
   }
   ```

   那么 SpringBoot 是如何获取发送请求的是浏览器还是客户端呢？

   SpringBoot 是根据请求的请求头来判断的。

   浏览器发送的请求：

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725112022.png)

   POSTMAN 客户端发送的请求：

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725112128.png)

   

3. 响应页面的解析代码

   ```java
   protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                                           Map<String, Object> model) {
       for (ErrorViewResolver resolver : this.errorViewResolvers) {
           ModelAndView modelAndView = resolver.resolveErrorView(request, status, model);
           if (modelAndView != null) {
               return modelAndView;
           }
       }
       return null;
   }
   ```

   `ErrorViewResolver` 是异常视图解析器，去哪个页面是由 `ErrorViewResolver` 解析得到的。

   这里的 `ErrorViewResolver` 是由

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190726152922.png)

   注册到容器中的。

4. DefaultErrorViewResolver

   ```java
   @Override
   public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
       ModelAndView modelAndView = resolve(String.valueOf(status.value()), model);
       if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
           modelAndView = resolve(SERIES_VIEWS.get(status.series()), model);
       }
       return modelAndView;
   }
   
   private ModelAndView resolve(String viewName, Map<String, Object> model) {
       // 默认 SpringBoot 可以找到一个页面 error/404
       String errorViewName = "error/" + viewName;
       
       TemplateAvailabilityProvider provider = this.templateAvailabilityProviders.getProvider(errorViewName,
                                                                                              this.applicationContext);
       if (provider != null) {
           // 模板引擎可以用的情况下返回到 errorViewName 指定的视图地址
           return new ModelAndView(errorViewName, model);
       }
       // 模板引擎不可用，就会调用这个方法
     
       return resolveResource(errorViewName, model);
   }
   ```

   ```java
   // 在静态资源文件夹下，找 errorViewName 对应的页面： error/404.html
   private ModelAndView resolveResource(String viewName, Map<String, Object> model) {
       for (String location : this.resourceProperties.getStaticLocations()) {
           try {
               Resource resource = this.applicationContext.getResource(location);
               resource = resource.createRelative(viewName + ".html");
               if (resource.exists()) {
                   return new ModelAndView(new HtmlResourceView(resource), model);
               }
           }
           catch (Exception ex) {
           }
       }
       // 如果不存在，返回 null
       return null;
   }
   ```

   

## 自定义错误处理

### 定制错误页面

1. 有模板引擎的情况下：将错误页面命名为错误状态码，并放在 error 文件夹下：error/状态码，发生错误就会来到对应的页面。

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190726154213.png)

   代码中还有一个 `4xx` 和 `5xx`，我们可以使用 4xx 和 5xx 来匹配这种类型的所有错误，但优先寻找精确的状态码。

   

   **页面可以获取的信息：**

   我们可以获取的信息来自于代码里的 model ，而 model 的获取来自于 `getErrorAttributes` 方法。

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190726154518.png)

   这个方法可以追溯到：

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190726154859.png)

   - **timestamp**: 时间戳

   - **status**: 状态码

   - **error**：错误提示

   - **exception**：异常

   - **message**：异常消息

   - **errors**：JSR303 数据校验的错误

     

2. 没有模板引擎（模板引擎找不到错误页面）

   在静态资源文件夹下找 error/错误码 页面（或者 4xx / 5xx）。

   

3. 以上都没有机会使用默认的页面

   

### 定制错误 JSON 数据

如果在请求中发生异常，SpringBoot 默认不会将异常类名和异常的堆栈信息放入 model 。可以在配置文件中选择开启：

```properties
server.error.include-stacktrace=always
server.error.include-exception=true
```



1. 使用异常处理器处理异常：

```java
@ControllerAdvice
public class MyExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(UserNotExistException.class)
    public Map<String, Object> handleException(Exception e) {
        logger.error("发生异常！\n");
        e.printStackTrace();

        Map<String, Object> map = new HashMap<>();
        map.put("errorCode", 400);
        map.put("message", e.getMessage());
        return map;
    }
```

**缺点：**没有自适应，浏览器和客户端返回的都是 json 数据，没有自适应。

可以转发到 /error 来自适应

```java
    @ExceptionHandler(UserNotExistException.class)
    public String handleException(Exception e) {
        logger.error("发生异常！\n");
        e.printStackTrace();

        Map<String, Object> map = new HashMap<>();
        map.put("errorCode", 400);
        map.put("message", e.getMessage());
        return "forward:/error";
    }
```

但是这样会导致浏览器访问的是默认的错误页面，而且错误状态码是200。

2.我们可以指定错误码，来让我们设定的错误页面生效，达到一个自适应的效果：

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190729171718.png)

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190729171806.png)

3.但这样我们自己定制的错误数据不能被携带出去

SpringBoot 的错误处理原理：

出现错误后，会来到 /error 请求，会被 `BasicErrorController` 处理，返回时做了自适应效果，但 model 的来源是相同的，是由 `getErrorAttributes` 方法获取到的，这个方法是在 `AbstractErrorController` 中所规定的，也是 `BasicErrorController` 的父类。

当容器中没有 `ErrorController` 类的时候，自动配置类 `ErrorMvcAutoConfiguration` 会自动将 `BasicErrorController` 组件添加到容器中：

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190730110840.png)

- 第一种办法：

  编写一个 `ErrorController` 类，放在容器中。

- 第二种方法：

  我们可以看到， model 里面的数据实际来自于：

  ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190730111220.png)

  这里的 `ErrorAttributes` 其实就是自动配置类中所加入组件的：![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190730111549.png)

  也就是说，`DefaultAttributes` 这个类的 `getErrorAttributes` 是容器处理 model 数据的方法，那么我们就可以实现一个这样的类加入组件中，加入自定义的数据：

  ```java
  @Component
  public class MyErrorAttributes extends DefaultErrorAttributes {
      @Override
      public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
          Map<String, Object> map = super.getErrorAttributes(webRequest, includeStackTrace);
          
          // 加入异常处理器中的所设置的属性
          Map<String, Object> ext = (Map<String, Object>) webRequest.getAttribute("ext", 0);
          map.put("ext", ext);
          
          map.put("testMsg", "hhhh");
          return map;
      }
  }
  ```

  ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190730113342.png)

  

  但这样还是存在问题，我们在属性中设置的包含异常和堆栈信息的设置会失效：

  ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190730112603.png)

  我们可以设置父类中的值，通过获取配置文件的属性来设置是否显示堆栈信息和异常信息：
  
  ```java
  @Component
  public class MyErrorAttributes extends DefaultErrorAttributes {
  
      private final MyErrorConf myErrorConf;
  
      public MyErrorAttributes(MyErrorConf myErrorConf) {
          super(myErrorConf.isIncludeException());
          this.myErrorConf = myErrorConf;
          System.out.println(myErrorConf);
      }
  
      @Override
      public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
          Map<String, Object> map = super.getErrorAttributes(webRequest, myErrorConf.isIncludeStacktrace());
          map.put("testMsg", "hhhh");
          Map<String, Object> ext = (Map<String, Object>) webRequest.getAttribute("ext", 0);
          map.put("ext", ext);
          return map;
      }
  }
  ```
  
  ```java
  @Component
  @ConfigurationProperties(prefix = "my")
  public class MyErrorConf {
  
      // @Value("${my.include-exception}")
      private boolean includeStacktrace;
      // @Value("${my.include-stacktrace}")
      private boolean includeException;
  
      boolean isIncludeStacktrace() {
          return includeStacktrace;
      }
  
      boolean isIncludeException() {
          return includeException;
      }
  
      @Override
      public String toString() {
          return "MyErrorConf{" +
                  "includeException=" + includeException +
                  ", includeStackTrace=" + includeStacktrace +
                  '}';
      }
  
      public void setIncludeStacktrace(boolean includeStacktrace) {
          this.includeStacktrace = includeStacktrace;
      }
  
      public void setIncludeException(boolean includeException) {
          this.includeException = includeException;
      }
  }
  ```
  
  ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/深度截图_选择区域_20190730155249.png)
  
  > 本来想直接在 **MyErrorAttributes** 中获取配置文件的值，但是 `@Value` 不能作用于 `static` 变量，而且需要使用依赖注入才可以，直接 new 一个对象无法获取值。

