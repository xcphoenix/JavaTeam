# SpringBoot 学习笔记

## 1.类路径

IDEA项目中，resources目录和java目录为类路径根目录

## 2.映射关系

### 1.普通静态资源映射

```java
/*
存放静态资源
没有人处理的请求被定向至下面四个文件夹来访问静态资源
*/
static final String[] 
CLASSPATH_RESOURCE_LOCATIONS 
= new String[]{
	"classpath:/META-INF/resources/",
	"classpath:/resources/",
	"classpath:/static/", 
	"classpath:/public/"
	｝
```

#### 1.欢迎页映射

spring boot配置的这个方法返回一个映射器作为bean来处理映射欢迎页面：

```java
@Bean
        public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext applicationContext) {
            return new WelcomePageHandlerMapping(new TemplateAvailabilityProviders(applicationContext), applicationContext, this.getWelcomePage(), this.mvcProperties.getStaticPathPattern());
        }
```

this.getWelcomePage()：首页的静态资源文件路径

```java
/*
通过this.resourceProperties.getStaticLocations()获取默认的路径集合
*/
private Optional<Resource> getWelcomePage() {
            String[] locations = getResourceLocations(this.resourceProperties.getStaticLocations());
            return Arrays.stream(locations).map(this::getIndexHtml).filter(this::isReadable).findFirst();
        }

/*
在 ResourceProperties　类中，staticLocations　被赋值为CLASSPATH_RESOURCE_LOCATIONS的值
*/
public ResourceProperties() {
        this.staticLocations = CLASSPATH_RESOURCE_LOCATIONS;
        ...
    }

private static final String[] CLASSPATH_RESOURCE_LOCATIONS = new String[]{"classpath:/META-INF/resources/", 
                                                                          "classpath:/resources/", 
                                                                          "classpath:/static/", 
                                                                          "classpath:/public/"
                                                                         };

/*
该方法默认页面名为index.html，把传入的路径与＂index.html＂拼接起来查找静态资源
*/
private Resource getIndexHtml(String location) {
            return this.resourceLoader.getResource(location + "index.html");
        }
```



this.mvcProperties.getStaticPathPattern()：被映射的url

```java
//类　WebMvcProperties　构造器中赋予staticPathPattern初值/**
public WebMvcProperties() {
        ...
        this.staticPathPattern = "/**";
        ...
    }
```

#### 2.小图标映射

```java
/*
图片资源映射器
*/
@Bean
            public SimpleUrlHandlerMapping faviconHandlerMapping() {
                SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
                mapping.setOrder(-2147483647);
                //这句应该是把所有请求favico.ico的请求映射到后面那个处理器
                mapping.setUrlMap(Collections.singletonMap("**/favicon.ico", this.faviconRequestHandler()));
                return mapping;
            }
```

进入this.faviconRequestHandler()方法

```java
/*
这个方法返回一个handler，并给这个handler设置了路径集合，路径集合由this.resolveFaviconLocations()方法返回
*/
@Bean
            public ResourceHttpRequestHandler faviconRequestHandler() {
                ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
                requestHandler.setLocations(this.resolveFaviconLocations());
                return requestHandler;
            }
```

进入this.resolveFaviconLocations()方法

```java
/*
通过类名分析这个类的功能是定位ico图片的位置，返回路径集合
*/
private List<Resource> resolveFaviconLocations() {
                String[] staticLocations = WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.getResourceLocations(this.resourceProperties.getStaticLocations());
                List<Resource> locations = new ArrayList(staticLocations.length + 1);
                ...
                locations.add(new ClassPathResource("/"));
                return Collections.unmodifiableList(locations);
            }
```

看样子资源路径集合同上

倒数三、五行表明该路径集合比默认的多了/路径，意味着直接放到/路径下也可以？一会儿试一下

**的确可以**



### 2.覆盖默认静态资源路径

staticLocations属性属于ResourceProperties类，在ResourceProperties类中，

```java
public ResourceProperties() {
        this.staticLocations = CLASSPATH_RESOURCE_LOCATIONS;
        ...
    }
```

而 CLASSPATH_RESOURCE_LOCATIONS 默认为下值：

```java
static final String[] 
CLASSPATH_RESOURCE_LOCATIONS 
= new String[]{
	"classpath:/META-INF/resources/",
	"classpath:/resources/",
	"classpath:/static/", 
	"classpath:/public/"
	｝
```

如果我们想要修改默认静态资源路径该怎么做呢？总不能重新编译jdk吧233333

我们来看看源码：



```java
@ConfigurationProperties(prefix = "spring.resources", ignoreUnknownFields = false)
public class ResourceProperties {
    ...
}
```

在ResourceProperties类上有一个*@ConfigurationProperties*注解

```java
/**
 * Annotation for externalized configuration. Add this to a class definition or a
 * {@code @Bean} method in a {@code @Configuration} class if you want to bind and validate
 * some external Properties (e.g. from a .properties file).
 * <p>
 *...
 *
 * @author Dave Syer
 * @see ConfigurationPropertiesBindingPostProcessor
 * @see EnableConfigurationProperties
 */
```

上面是*@ConfigurationProperties*内部的注释，大意是***这是一个外部化配置的注解，可以添加一些外部属性（例如.properties文件）到类中***,也就是说我们可以在application.properties文件中配置staticLocations这个属性.

注解上设置了默认前缀

```java
prefix = "spring.resources"
```

而我们要设置的静态资源路径属性名为staticLocations,那就在application.properties输入**spring.resources.staticLocations**，实际IDEA会提示补全为**spring.resources.static-locations**.因为staticLocations是个String数组,因此可以赋多个值，值之间用逗号分隔即可

即如下格式：

```java
spring.resources.static-locations=path1,path2,...
```

> 一旦配置该属性，则apringboot 默认配置被覆盖

## 3.模板引擎(Thymeleaf)

理解：按一定的语法将给定的数据填充到写好的模板里

### 1.添加依赖

[查看spring boot2.1.6参考文档](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/)

[各种依赖的starter](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/using-boot-build-systems.html#using-boot-starter)

```xml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2.映射关系分析

根据之前学到的映射关系知识，分析该模版引擎的映射规则

找到自动配置类

```java
/*所有的自动配置在此包下*/
package org.springframework.boot.autoconfigure
```

找到模板类

```java
package org.springframework.boot.autoconfigure.thymeleaf;
/**
 * Properties for Thymeleaf.
 * ...
 */
@ConfigurationProperties(prefix = "spring.thymeleaf")
public class ThymeleafProperties {

	private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

	public static final String DEFAULT_PREFIX = "classpath:/templates/";

	public static final String DEFAULT_SUFFIX = ".html";
	...
}
```

可以看到Thymeleaf类识别的默认资源为"classpath:/templates/"下的html文件

### 3.Thymeleaf使用语法

#### 1.导入名称空间

```html
<html xmlns:th="http://www.thymeleaf.org">
```

导入后可以提供补全等功能

#### 2.语法

##### 1.简单表达式

- ${...}

  > 变量表达式

- #{...}

  > 消息表达式

##### 2.具体使用

- th:text="${...}"

  >  替换标签文本内容

- th:utext="${...}"

  > 同上，但不转义文本内容

- th:each="a:${array} "

  > 用a遍历array数组

### 4.Restful

[阮一峰](http://www.ruanyifeng.com/blog/2018/10/restful-api-best-practices.html)

就是把请求方法类型利用起来，请求方法＋url＝目标操作

eg:

>  GET+orange = GET orange,意为获取一个橘子

### 5.错误处理

#### 1.错误访问时服务端反应

浏览器返回html

```html
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Tue Jul 30 09:33:15 CST 2019
There was an unexpected error (type=Not Found, status=404).
No message available
```



客户端返回json

```json
{
    "timestamp": "2019-07-30T01:31:27.089+0000",
    "status": 404,
    "error": "Not Found",
    "message": "No message available",
    "path": "/cccccc"
}
```

#### 2.定制目标：

1. 定制错误页面

2. 定制错误json

#### 3.步骤

##### 1. 进入错误的自动配置类(ErrorMvcAutoConfiguration)

基本组件

---

(1)DefaultErrorAttributes

```java
@Bean
@ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
public DefaultErrorAttributes errorAttributes() {
	return new DefaultErrorAttributes(this.serverProperties.getError().isIncludeException());
	}
```

(2)BasicErrorController

```java
@Bean
@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
public BasicErrorController basicErrorController(ErrorAttributes errorAttributes) {
	return new BasicErrorController(errorAttributes, this.serverProperties.getError(), this.errorViewResolvers);
	}
```

(3)ErrorPageCustomizer

```java
/*
	错误页面自定义类
*/
@Bean
public ErrorPageCustomizer errorPageCustomizer() {
	return new ErrorPageCustomizer(this.serverProperties, this.dispatcherServletPath);
}
```

(4)DefaultErrorViewResolver

```java
/*
	默认错误视图解析器
*/
@Bean
@ConditionalOnBean(DispatcherServlet.class)
@ConditionalOnMissingBean
public DefaultErrorViewResolver conventionErrorViewResolver() {
	return new DefaultErrorViewResolver(this.applicationContext, this.resourceProperties);
}
```

##### 2错误响应流程

###### 1.错误页面的生成

进入ErrorPageCustomizer类

找到注册错误页面的方法

```java
@Override
public void registerErrorPages(ErrorPageRegistry errorPageRegistry) {
	ErrorPage errorPage = new ErrorPage(
        this.dispatcherServletPath.getRelativePath(this.properties.getError().getPath()));
	errorPageRegistry.addErrorPages(errorPage);
}
```

即通过*this.properties.getError().getPath()*的返回值为路径构造*ErrorPage*

进入*this.properties.getError().getPath()*方法

```java
/**
 * Configuration properties for web error handling.
	．．．
 */
public class ErrorProperties {

	．．．

	public String getPath() {
		return this.path;
	}
```

找到*path*的定义

```java
/**
 * Path of the error controller.
 */
	@Value("${error.path:/error}")
	private String path = "/error";
```

即*path*默认值为"/error"

若获取不到${error.path}的值，则*path*为默认值"/error"

###### 2.处理/*path*请求

处理由BasicErrorController类完成，上一步生成了url，自然需要一个controller来处理

进入BasicErrorController类

```java
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
	public class BasicErrorController extends AbstractErrorController {

	．．．
｝
```

可以看到这个controller接受*server.error.path* or *error.path* or */error*的映射，优先级从高到低，若前两个变量都没有值则默认为"/error"

具体如何处理：

```java
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
	HttpStatus status = getStatus(request);
	Map<String, Object> model = Collections
			.unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
	response.setStatus(status.value());
	ModelAndView modelAndView = resolveErrorView(request, response, status, model);
	return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
}

@RequestMapping
public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
	Map<String, Object> body = getErrorAttributes(request,isIncludeStackTrace(request, MediaType.ALL));
	HttpStatus status = getStatus(request);
	return new ResponseEntity<>(body, status);
}
```

上面有两种对*/path*的处理方法

- @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)

  > 返回html类型数据，则对请求头中标示了接收text\html的请求有效，浏览器就是其一

- @RequestMapping

  > 被该controller映射且没被上面方法处理的被该方法处理

###### 3.寻找视图文件

第一种如何产生视图

```java
ModelAndView modelAndView = resolveErrorView(request, response, status, model);
```

进入resolveErrorView方法

```java
	/**
	 * Resolve any specific error views. By default this method delegates to
	 * {@link ErrorViewResolver ErrorViewResolvers}.
	 ...
	 */
	protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status,Map<String, Object> model) {
		for (ErrorViewResolver resolver : this.errorViewResolvers) {
			ModelAndView modelAndView = resolver.resolveErrorView(request, status, model);
			if (modelAndView != null) {
				return modelAndView;
			}
		}
		return null;
	}
```

注释：解析任何特定的错误视图．默认情况下此方法委托给类ErrorViewResolver

DefaultErrorViewResolver继承了ErrorViewResolver

进入DefaultErrorViewResolver

```java
@Override
public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
	ModelAndView modelAndView = resolve(String.valueOf(status.value()), model);
	if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
		modelAndView = resolve(SERIES_VIEWS.get(status.series()), model);
	}
	return modelAndView;
}
```

resolveErrorView通过resolve()方法获取ModelAndView,并传入了状态码的字符串和一个map对象

而resolve方法如下

```java
private ModelAndView resolve(String viewName, Map<String, Object> model) {
	String errorViewName = "error/" + viewName;
	TemplateAvailabilityProvider provider = this.templateAvailabilityProviders.getProvider(errorViewName,
			this.applicationContext);
	if (provider != null) {
		return new ModelAndView(errorViewName, model);
	}
	return resolveResource(errorViewName, model);
}
```

可以看出该方法是以状态码作为视图名的，并且默认视图文件放在路径**"error/"**下

下面几行大概意思是用模板引擎解析视图文件，成功则返回，失败则调用**resolveResource()**方法

进入resolveResource()方法

```java
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
	return null;
}
```

下面这几行就很熟悉了，获取静态资源路径，给视图名拼接上".html"，判断资源是否存在于静态资源路径下

如果存在，则返回模型和视图，如果还不存在，则抛出异常，返回null

## 4.内嵌容器

### 1.配置修改

#### 1.使用applicatioin.properties配置文件

##### 1.通用servlet配置

```java
server.xxx=
```

##### 2．tomcat配置

```java
server.tomcat.xxx=
```

分析

```java
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {
	...
    /**
	 * Tomcat properties.
	 */
	public static class Tomcat {
    	...
    }
}
```

即ServerProperties为web容器的配置类

#### 2. 实现配置类

```java
@Bean
public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryWebServerFactoryCustomizer()
{
     return new WebServerFactoryCustomizer<ConfigurableWebServerFactory>() {
        @Override
        public void customize(ConfigurableWebServerFactory factory) {
            factory.setPort(8080);
            factory.setXxx();
            ...
        }
    };
}
```

### 2.注册组件

#### 1.servlet

首先写一个类继承HttpServlet,实现自己想要重写的方法

```java
public class MyServlet extends HttpServlet {
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
        resp.getWriter().write("myServlet");
    }
}
```

然后在配置类里面注册一个返回ServletRegistrationBean的bean

```java
@Bean
public ServletRegistrationBean myServlet()
{
    ServletRegistrationBean registrationBean = new ServletRegistrationBean(new MyServlet(),"/myServlet");
    return registrationBean;
}
```

使用有参构造器

```java
	public ServletRegistrationBean(T servlet, String... urlMappings) {
		this(servlet, true, urlMappings);
	}
```

传入自己写的servlet和想要映射的url即可

#### 2.filter

首先写一个类实现Filter接口

```java
public class MyFilter implements Filter {
	...
}
```

```java
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    System.out.println("过滤");
    chain.doFilter(request,response);
}
```

过滤操作不止一个，自定义的过滤器执行完后过滤器链的下一个过滤器接着执行

```java
@Bean
public FilterRegistrationBean myFilter()
{
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new MyFilter());
    Collection<String> collection = new HashSet<>();
    collection.add("/myFilter");
    filterRegistrationBean.setUrlPatterns(collection);
    return filterRegistrationBean;
}
```

#### 3.listener

同上

```java
public class MyListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("监听器初始化");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("监听器销毁");
    }
}
```



```java
@Bean
public FilterRegistrationBean myFilter()
{
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new MyFilter());
    Collection<String> collection = new HashSet<>();
    collection.add("/myFilter");
    filterRegistrationBean.setUrlPatterns(collection);
    return filterRegistrationBean;
}
```

### 3.切换容器

首先将当前容器隔离

```xml
<exclusions>
	<exclusion>
        <artifactId>spring-boot-starter-tomcat</artifactId
        <groupId>org.springframework.boot</groupId>
    </exclusion>
</exclusions>
```

引入xxx依赖即可

```xml
<dependency>
    <artifactId>spring-boot-starter-xxx</artifactId>
    <groupId>org.springframework.boot</groupId>
</dependency>
```

目前springboot中可以切换三种容器

- tomcat
- jetty
- undertow















# 附录

## 1.遇到的注解

### 1.@see 

```java
/*
	可以在注释中实现链接跳转,方便更好的描述类之间的关系
*/
```

### 2.@ConfigurationProperties

```java
/*
	外部化配置的注解，可以添加一些外部属性（例如.properties文件）到类中
*/
```

### 3.@ConditionalOnMissingBean()

```java
/*
	当括号里面的类没有时该注解装饰的类生效
*/
```

## 2.单词积累

### 1.resolve

> 解析

### 2.instance

> 实例

### 3.conditional

> 条件

### 4.customizer

> 定制

### 5.relative

> adj.相对的 
>
> n.亲戚

### 6.delegates

> n.代表
>
> v.授权

### 7.convention

> 惯例

### 8.embedded

> 嵌入式

## 3.学习过程总结的东西

### 1.关于xxxProperties配置类

- 开始的静态资源配置在resourceProperties类里面

- 之后的错误处理配置在ErrorProperties类里面

- 刚才servlet容器的配置在ServerProperties类里面

  我猜springboot所有有关yyy配置的类都在一个yyyProperties类里面

  通过指定前缀，在application.properties文件中以**前缀．yyy.xxx=**来配置