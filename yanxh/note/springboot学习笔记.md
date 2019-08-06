# SpringBoot 学习笔记

## 一.类路径

IDEA项目中，resources目录和java目录为类路径根目录

## 二.映射关系

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

> 一旦配置该属性，则springboot 默认配置被覆盖

## 三.模板引擎(Thymeleaf)

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

## 四.Servlet容器(内嵌)

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

### 4. 外置(servlet)容器

- 创建项目时建为war项目，并添加webapp目录

- 将嵌入式的servlet容器的<scope/>设置为provided

- 必须有一个SpringBootServletInitializer的子类

作用：外置tomcat支持jsp

## 五．数据访问

### 1.数据源的自动配置分析

进入自动配置的jia包

找到DataSource的配置类

进入DataSourceConfiguration类

```java
	@Configuration
	@ConditionalOnClass(org.apache.tomcat.jdbc.pool.DataSource.class)
	@ConditionalOnMissingBean(DataSource.class)
	@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "org.apache.tomcat.jdbc.pool.DataSource",
			matchIfMissing = true)
	static class Tomcat {

		@Bean
		@ConfigurationProperties(prefix = "spring.datasource.tomcat")
        ...
    }

	@Configuration
	@ConditionalOnClass(HikariDataSource.class)
	@ConditionalOnMissingBean(DataSource.class)
	@ConditionalOnProperty(name = "spring.datasource.type", havingValue = 
                           "com.zaxxer.hikari.HikariDataSource",
		matchIfMissing = true)
	static class Hikari {

		@Bean
		@ConfigurationProperties(prefix = "spring.datasource.hikari")
        ...
    }
```

下面还有

```java
static class Dbcp2
static class Generic
```

结构都差不多，就不贴代码了

从最上面可以看出，要选择使用哪个数据源是在application.properties文件里面用*spring.datasource.type*属性指定的，该属性值类型为数据源完整类名

从刚才的代码运行中得出默认数据源为　**com.zaxxer.hikari.HikariDataSource**

即默认

```properties
spring.datasource.type=com.zaxxer.hikari.HikariDataSource
```

而*com.zaxxer.hikari.HikariDataSource*类内部属性的配置由上代码可知为

```properties
spring.datasource.hikari＝xxx
```

其余类推即可

### 2.DDL和DML的执行

首先找到初始化数据资源的类并进入

```java
class DataSourceInitializer {

	...

DataSourceInitializer(DataSource dataSource, DataSourceProperties properties, ResourceLoader resourceLoader) {
		this.dataSource = dataSource;
		this.properties = properties;
		this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
	}
    ...
}
```

可以看见构造器中传入了三个对象

- DataSource
- DataSourceProperties
- ResourceLoader

#### 1.DataSource

先说DataSource

```java

/**
...
 * The {@code DataSource} interface is implemented by a driver vendor.
...
 * A {@code DataSource} object has properties that can be modified
 * when necessary.  For example, if the data source is moved to a different
 * server, the property for the server can be changed.  The benefit is that
 * because the data source's properties can be changed, any code accessing
 * that data source does not need to be changed.
 *...
 * @since 1.4
 */

public interface DataSource  extends CommonDataSource, Wrapper {
  
  Connection getConnection() throws SQLException;

  Connection getConnection(String username, String password)
    throws SQLException;
}

```

- The {@code DataSource} interface is implemented by a driver vendor.

  > 这个接口由驱动程序供应商提供

- 意味着这个接口使得在数据源变化的情况下通过更改其属性而避免改动代码

知道这个类通过用户名和密码返回连接就可以了

#### 2.ResourceLoader

引用类中的描述

```java
@param resourceLoader the resource loader to use (can be null)
```

ResourceLoader接口中的注释：

```java
/**
 * Strategy interface for loading resources (e.. class path or file system
 * resources)
 */
```

#### 3.DataSourceProperties

看这个类名格式是不是很熟悉

xxxProperties

就是DataSource的配置类么

进去看看

```java
/**
 * Base class for configuration of a data source.
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties implements BeanClassLoaderAware, InitializingBean {

}
```

- 要配置该类的属性，在application.yml/properties中以*spring.datasource*开头

看看第一个方法

```java
/**
 * Create the schema if necessary.
 * @return {@code true} if the schema was created
 * @see DataSourceProperties#getSchema()
 */
public boolean createSchema() {
	List<Resource> scripts = getScripts("spring.datasource.schema", 
        this.properties.getSchema(), "schema");
	if (!scripts.isEmpty()) {
		if (!isEnabled()) {
			logger.debug("Initialization disabled (not running DDL scripts)");
			return false;
		}
		String username = this.properties.getSchemaUsername();
		String password = this.properties.getSchemaPassword();
		runScripts(scripts, username, password);
	}
	return !scripts.isEmpty();
}
```

这就是初始化DDL的方法

方法一开始就调用*getScripts()*方法试图获取DDL资源列表，并传入三个参数

- spring.datasource.schema 

  > 配置文件中的属性名

- this.properties.getSchema()

  > 该函数返回配置类中schema的值，schema为**List<String>类型**

- "schema"

分析该函数

```java
private List<Resource> getScripts(String propertyName, List<String> resources, String fallback) {
	if (resources != null) {
		return getResources(propertyName, resources, true);
	}
	String platform = this.properties.getPlatform();
	List<String> fallbackResources = new ArrayList<>();
	fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
	fallbackResources.add("classpath*:" + fallback + ".sql");
	return getResources(propertyName, fallbackResources, false);
}
```

该函数判断schema列表是否为空，

若为空

- 未在配置文件中指定DDL资源位置，schema == null，则查找默认的路径文件
- 其中fallback为"schema",platform默认为"all"，可在配置文件中修改
- 即该方法会去寻找类路径下的*schema-all.sql*或*schema.sql*

否则

- 已经指定路径，则通过*getResources()*方法将指定位置的资源加载到资源对象列表中并返回

**注意:**spring boot 2.0x后，DataSourceProperties类中多了一个属性

```java
/**
 * Initialize the datasource with available DDL and DML scripts.
 */
private DataSourceInitializationMode initializationMode = 
    DataSourceInitializationMode.EMBEDDED;

```

进入 DataSourceInitializationMode 类

```java
public enum DataSourceInitializationMode {

	/**
	 * Always initialize the datasource.
	 */
	ALWAYS,

	/**
	 * Only initialize an embedded datasource.
	 */
	EMBEDDED,

	/**
	 * Do not initialize the datasource.
	 */
	NEVER

}

```

可能是因为数据库非嵌入资源，而这里默认是只初始化嵌入资源，所以不会去初始化DDL资源，在配置文件中改为ＡＬＷＡＹＳ即可

```yaml
spring:
  datasource:
    ...
    initialization-mode: always
```

这样启动springboot时，DDL资源就会被执行

DML的过程也差不多

```java
/**
 * Initialize the schema if necessary.
 * @see DataSourceProperties#getData()
 */
public void initSchema() {
	List<Resource> scripts = getScripts("spring.datasource.data", this.properties.getData(), "data");
	if (!scripts.isEmpty()) {
		if (!isEnabled()) {
			logger.debug("Initialization disabled (not running data scripts)");
			return;
		}
		String username = this.properties.getDataUsername();
		String password = this.properties.getDataPassword();
		runScripts(scripts, username, password);
	}
}
```

调用的方法都一样，只是schema换成了data

配置类中的data属性：

```java
/**
 * Data (DML) script resource references.
 */
private List<String> data;
```

和schema一样的类型

剩下的就不多说了

### 3.jdbc操作数据库

自动配置类的jdbc包下有

```java
@Bean
@Primary
@ConditionalOnMissingBean(JdbcOperations.class)
public JdbcTemplate jdbcTemplate() {
	JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
	JdbcProperties.Template template = this.properties.getTemplate();
	jdbcTemplate.setFetchSize(template.getFetchSize());
	jdbcTemplate.setMaxRows(template.getMaxRows());
	if (template.getQueryTimeout() != null) {
		jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
	}
	return jdbcTemplate;
}
```

使用的时候直接

```java
/**
 * @author galaxy
 * @date 19-8-2 - 下午4:34
 */
@Controller
public class jdbcTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/query")
    @ResponseBody
    public Map<String,Object> jdbcSearch()
    {
        List<Map<String,Object>> list = jdbcTemplate.queryForList("select * from test");
        return list.get(0);
    }
}
```

请求结果

```json
{"name":"ll","sex":"0"}
```

### 4.整合数据源(Druid)

#### 1.绑定参数

阿里的druid源

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.19</version>
</dependency>
```

上面说过了，在yml文件中把type的值设置为duruid的完全限定类名

```yaml
spring:
  datasource:
    ...
    type: com.alibaba.druid.pool.DruidDataSource
```

##### 1.配置bean

##### 创建DruidDataSource的bean,因为这样可以用注解将application.yml/properties文件中配置的对应属性的值绑定到bean

```yml
peispring:
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/jdbc
    driver-class-name: com.mysql.cj.jdbc.Driver
    initialization-mode: always
    platform: linux
    #type: com.alibaba.druid.pool.DruidDataSource


    #   数据源其他配置
    initialSize: 5
    minIdle: 5
    maxActive: 20
    maxWait: 60000
    timeBetweenEvictionRunsMillis: 60000
    minEvictableIdleTimeMillis: 300000
    validationQuery: SELECT 1 FROM DUAL
    testWhileIdle: true
    testOnBorrow: false
    testOnReturn: false
    poolPreparedStatements: true
    #   配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
    filters: stat,wall,slf4j
    maxPoolPreparedStatementPerConnectionSize: 20
    useGlobalDataSourceStat: true
    connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500

```

配置的bean

```java
@Configuration
public class MyDruidConfig {


    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DruidDataSource myDruidSourceConfig()
    {
        return new DruidDataSource();
    }
}
```

type: com.alibaba.druid.pool.DruidDataSource使用默认值启动数据源

当使用了bean配置驱动时，yml/properties中的配置覆盖默认配置，type属性可以不配置

##### 2.引入starter

druid[参考文档](https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)

[如何在Spring Boot中集成Druid连接池和监控？](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter)

引入

```xml
<dependency>
   <groupId>com.alibaba</groupId>
   <artifactId>druid-spring-boot-starter</artifactId>
   <version>1.1.17</version>
</dependency>
```

引入该jar后springboot就可自动读取application.properties/yml下的配置，就不用使用bean来配置了

#### 2.servlet配置

内置监控页面的servlet

[内置监控页面是一个Servlet，具体配置看这里](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter)

文档中使用xml的配置方法，下面我使用java配置

```java
    /**
     * 注册druid的监控servlet用于提供监控服务
     * 以便使用Druid的内置监控页面
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean()
    {
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(),"/druid/*");
        Map<String,String> map = new HashMap<>();
        map.put("loginUsername","admin");
        map.put("loginPassword","123456");
        map.put("allow","");
        map.put("deny","127.0.0.2 ");
        bean.setInitParameters(map);
        return bean;
    }
```

使用*ServletRegistrationBean*类向容器注册一个servlet并设置其映射规则

用Map传入起初始化所需参数

则内置监控页面的首页是/druid/index.html

例如：
http://localhost:8080/druid/index.html 

#### 3.filter配置

配置WebStatFilter

> WebStatFilter用于采集web-jdbc关联监控的数据。

[配置WebStatFilter官方文档](https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_%E9%85%8D%E7%BD%AEWebStatFilter)

下面是java配置方式

```java
    /**
     * 注册web监控的过滤器
     */
    @Bean
    public FilterRegistrationBean webStatFilter()
    {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new WebStatFilter());

        Map<String,String> map = new HashMap<>();
        //排除一些不必要的url
        map.put("exclusions","*.js,*.css,/druid/*");
        bean.setInitParameters(map);
        bean.setUrlPatterns(Arrays.asList("/**")    );
        return bean;
    }
```

### 5.mybatis整合

引入mybatis的starter

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

相比于springmvc，不用写很多的xml

创建数据表

创建对应Pojo

写一个mapper接口将上述对应起来

这里有两种方法

- 注解

```java
@Select("select * from department where id=#{id}")
public Department getDepartmentById(Integer id);
```

- xml

>  在yml或使用java类中配置配置文件的路径，然后将mybatis配置和pojo的映射xml放在路径下即可

这些和以前都一样

下面说说自动配置原理

首先找到自动配置类

```java
public class MybatisAutoConfiguration implements InitializingBean {

  private final MybatisProperties properties;
｝
```

看到这个就应该知道是干什么的了吧，标准的xxxProperties格式

进入*MybatisProperties*

```java
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
public class MybatisProperties {

  public static final String MYBATIS_PREFIX = "mybatis";

    ...
}
```

默认配置前缀为*mybatis*,也就是在application.yml或properties中以*mybatis*开头

*MybatisProperties*类中有个属性config

```java
  /**
   * A Configuration object for customize default settings. If {@link #configLocation} is specified, this property is
   * not used.
   */
  @NestedConfigurationProperty
  private Configuration configuration;
```

注释说这个类是自定义默认设置的，也就是我们用来配置mybatis的

这个配置类里面有很多mybatis属性

这里把分段命名和驼峰命名的转换开启

```yaml
mybatis:
    configuration:
      mapUnderscoreToCamelCase: true
```

也可以使用定制配置类

```java
    @Bean
    public ConfigurationCustomizer configurationCustomizer()
    {
        return new ConfigurationCustomizer(){

            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }
```



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

### 3.@ConditionalOnMissingBean(xxx.class)

```java
/*
	当括号里面的类没有时该注解装饰的类生效
*/
```

### 4.@ConditionalOnProperty(name=xxx,havingValue=xxx)

```java
/*
	当properies配置文件中有名为"spring.datasource.type"且值		　　
	为"org.apache.tomcat.jdbc.pool.DataSource"的配置时，启用该类
*/
即spring.datasource.type=org.apache.tomcat.jdbc.pool.DataSource

@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "org.apache.tomcat.jdbc.pool.DataSource",matchIfMissing = true)

```

### 5.@ConditionalOnClass(xxx.class)

```java
/*
	当有括号中的类或其子类时，该注解注释类生效
*/
例如
@ConditionalOnClass(org.apache.tomcat.jdbc.pool.DataSource.class)
/*
	当检测到有org.apache.tomcat.jdbc.pool.DataSource类存在时，启用该注解注释的类
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

### 9.generate

> 生成

### 10.strategy

> 策略

## 3.学习过程总结的东西

### 1.关于xxxProperties配置类

- 开始的静态资源配置在resourceProperties类里面

- 之后的错误处理配置在ErrorProperties类里面

- 刚才servlet容器的配置在ServerProperties类里面

  我猜springboot所有有关yyy配置的类都在一个yyyProperties类里面

  通过指定前缀，在application.properties文件中以**前缀．yyy.xxx=**来配置
