# Spring 与 Web 开发

使用 SpringBoot：

1. 创建 SpringBoot 应用，选中需要的模块
2. SpringBoot 默认将这些场景自动配置好，只需要在配置文件中指定少量配置
3. 自己编写业务逻辑代码



## SpringBoot 对静态资源的映射规则

```java
// org/springframework/boot/autoconfigure/web/ResourceProperties.java
@ConfigurationProperties(prefix = "spring.resources", ignoreUnknownFields = false)
public class ResourceProperties {
    // 可以设置与静态资源有关的设置，例如缓冲时间等
```



```java
// org/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration.java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (!this.resourceProperties.isAddMappings()) {
        logger.debug("Default resource handling disabled");
        return;
    }
    Duration cachePeriod = this.resourceProperties.getCache().getPeriod();
    CacheControl cacheControl = this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
    if (!registry.hasMappingForPattern("/webjars/**")) {
        customizeResourceHandlerRegistration(registry.addResourceHandler("/webjars/**")
                                             .addResourceLocations("classpath:/META-INF/resources/webjars/")
                                             .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
    }
    String staticPathPattern = this.mvcProperties.getStaticPathPattern();
    if (!registry.hasMappingForPattern(staticPathPattern)) {
        customizeResourceHandlerRegistration(registry.addResourceHandler(staticPathPattern)
                                             .addResourceLocations(getResourceLocations(this.resourceProperties.getStaticLocations()))
                                             .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
    }
}

// 配置首页
@Bean
public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext applicationContext) {
    return new WelcomePageHandlerMapping(new TemplateAvailabilityProviders(applicationContext),
                                         applicationContext, getWelcomePage(), this.mvcProperties.getStaticPathPattern());
}

// 配置喜欢的图标
@Configuration
@ConditionalOnProperty(value = "spring.mvc.favicon.enabled", matchIfMissing = true)
public static class FaviconConfiguration implements ResourceLoaderAware {

    private final ResourceProperties resourceProperties;

    private ResourceLoader resourceLoader;

    public FaviconConfiguration(ResourceProperties resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public SimpleUrlHandlerMapping faviconHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
      
 // 在静态文件夹中寻找
        mapping.setUrlMap(Collections.singletonMap("**/favicon.ico", faviconRequestHandler()));
        return mapping;
    }

    @Bean
    public ResourceHttpRequestHandler faviconRequestHandler() {
        ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
        requestHandler.setLocations(resolveFaviconLocations());
        return requestHandler;
    }

    private List<Resource> resolveFaviconLocations() {
        String[] staticLocations = getResourceLocations(this.resourceProperties.getStaticLocations());
        List<Resource> locations = new ArrayList<>(staticLocations.length + 1);
        Arrays.stream(staticLocations).map(this.resourceLoader::getResource).forEach(locations::add);
        locations.add(new ClassPathResource("/"));
        return Collections.unmodifiableList(locations);
    }
```

1. 所有 /webjars/\*\*，都去 classpath:/META-INF/resources/webjars/ 找资源；

   [webjars](https://www.webjars.org/)：以 jar 包的方式引入静态资源；

   在 ***webjars*** 这个网站上可以使用 maven 导入常用 js 库的 webjar

   ```xml
   <dependency>
       <groupId>org.webjars</groupId>
       <artifactId>jquery</artifactId>
       <version>3.4.1</version>
   </dependency>
   ```

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190721132241.png)

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190721131900.png)

2.  "/**" 访问当前项目的任何资源，对应

   - classpath:/META-INF/resources/
   - classpath:/resources/

   - classpath:/static/

   - classpath:/public/
   - /：当前项目根路径

   ```java
   public class ResourceProperties {
   
       private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/",
                                                                     "classpath:/resources/", "classpath:/static/", "classpath:/public/" };
   
       /**
   	 * Locations of static resources. Defaults to classpath:[/META-INF/resources/,
   	 * /resources/, /static/, /public/].
   	 */
       private String[] staticLocations = CLASSPATH_RESOURCE_LOCATIONS;
   ```

3. 欢迎页面的映射

4. 页面图标映射（可能需要清除浏览器缓存 - [Chrome 插件](https://chrome.google.com/webstore/detail/classic-cache-killer/kkmknnnjliniefekpicbaaobdnjjikfp)或者 <kbd>Shift+F5</kbd>）

   需要放在映射路径的根目录下才可以。

> By default, resources are mapped on `/**`, but you can tune that with the `spring.mvc.static-path-pattern` property. For instance, relocating all resources to `/resources/**` can be achieved as follows:
>
> ```
> spring.mvc.static-path-pattern=/resources/**
> ```
>
> You can also customize the static resource locations by using the `spring.resources.static-locations` property (replacing the default values with a list of directory locations). The root Servlet context path, `"/"`, is automatically added as a location as well.
>
> In addition to the “standard” static resource locations mentioned earlier, a special case is made for [Webjars content](https://www.webjars.org/). Any resources with a path in`/webjars/**` are served from jar files if they are packaged in the Webjars format.
>
> | ![[Tip]](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/images/tip.png) |
> | ------------------------------------------------------------ |
> | Do not use the `src/main/webapp` directory if your application is packaged as a jar. Although this directory is a common standard, it works **only** with war packaging, and it is silently ignored by most build tools if you generate a jar. |



## 模板引擎

Jsp、Thymeleaf、Velocity、Freemarker ... 

SpringBoot 如果以 jar 的形式打包，而且由于 SpringBoot 使用的是内嵌的 Tomcat ，所以 SpringBoot 推荐使用 ***Thymeleaf***.

### 引入Thymeleaf

pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```



### Thymeleaf 语法

自动配置的默认规则：

```java
@ConfigurationProperties(prefix = "spring.thymeleaf")
public class ThymeleafProperties {

	private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

	public static final String DEFAULT_PREFIX = "classpath:/templates/";

	public static final String DEFAULT_SUFFIX = ".html";
```

只要把 html 放在 classpath:/templates/ 后，Thymeleaf 就会自动渲染。

使用：

1. 导入 thymeleaf 名称空间：

   ```html
   <html lang="zh" xmlns:th="http://www.thymeleaf.org">
   ```

2. 使用语法

   ```html
   <!DOCTYPE html>
   <html lang="zh" xmlns:th="http://www.thymeleaf.org">
   <head>
       <meta charset="UTF-8">
       <title>Title</title>
   </head>
   <body>
       <h1>SUCCESS</h1>
       <!-- 将 div 的内容设置为... -->
       <div th:text="${hello}">这是显示欢迎数据</div>
   </body>
   </html>
   ```

#### 语法规则

- `th:text` 改变当前元素里面的文本内容

   `th:` 任意 html 属性，来替换原生属性的值

   对应的行内写法：

   - `th:text` => `[[]]`

   - `th:utext` => `[()]`

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190721160559.png)

   

- 表达式

  **Simple expressions:**（表达式语法）

  - Variable Expressions: `${...}`

    - 获取对象的属性，调用方法
    - 使用内置的基本对象
      - #ctx : the context object.
      - #vars: the context variables.
      - #locale : the context locale.
      - #request : (only in Web Contexts) the HttpServletRequest object.
      - #response : (only in Web Contexts) the HttpServletResponse object.
      - #session : (only in Web Contexts) the HttpSession object.
      - #servletContext : (only in Web Contexts) the ServletContext object.
    - 使用内置的工具对象
      - #execInfo : information about the template being processed.
      - #messages : methods for obtaining externalized messages inside variables expressions, in the same way as they
        would be obtained using #{…} syntax.
      - #uris : methods for escaping parts of URLs/URIs
      - #conversions : methods for executing the configured conversion service (if any).
      - #dates : methods for java.util.Date objects: formatting, component extraction, etc.
      - #calendars : analogous to #dates , but for java.util.Calendar objects.
      - #numbers : methods for formatting numeric objects.
      - #strings : methods for String objects: contains, startsWith, prepending/appending, etc.
      - #objects : methods for objects in general.
      - #bools : methods for boolean evaluation.
      - #arrays : methods for arrays.
      - #lists : methods for lists.
      - #sets : methods for sets.
      - #maps : methods for maps.
      - #aggregates : methods for creating aggregates on arrays or collections.
      - #ids : methods for dealing with id attributes that might be repeated (for example, as a result of an iteration).

  - Selection Variable Expressions: 

    `*{...}`（选择表达式）和`#{}` 功能相同，补充使用：配置 `th:object` 使用，可以直接引用 `th:object` 的属性

  - Message Expressions: `#{...}`

    获取国际化内容

  - Link URL Expressions: `@{...}`

    定义 url，里面可以使用变量等值

  - Fragment Expressions: `~{...}`

    片段引用表达式

  

  **Literals**（字面量）

  - Text literals: 'one text' , 'Another one!' ,…

  - Number literals: 0 , 34 , 3.0 , 12.3 ,…

  - Boolean literals: true , false

  - Null literal: null

  - Literal tokens: one , sometext , main ,…

    

  **Text operations:**（文本操作）

  - String concatenation: +

  - Literal substitutions: |The name is ${name}|

    

  **Arithmetic operations:**（数学运算）

  - Binary operators: + , - , * , / , %

  - Minus sign (unary operator): -

    

  **Boolean operations:**（布尔运算）

  - Binary operators: and , or

  - Boolean negation (unary operator): ! , not

    

  **Comparisons and equality:**（比较运算）

  - Comparators: > , < , >= , <= ( gt , lt , ge , le )

  - Equality operators: == , != ( eq , ne )

    

  **Conditional operators:**（条件运算）

  - If-then: (if) ? (then)

  - If-then-else: (if) ? (then) : (else)

  - Default: (value) ?: (defaultvalue)

    

  **Special tokens:**

  - Page 17 of 104
  - No-Operation: _



## SpringBoot 对 MVC 的处理

SpringBoot 自动配置好了 SpringMVC。

### Spring MVC Auto-configuration

Spring Boot provides auto-configuration for Spring MVC that works well with most applications.

The auto-configuration adds the following features on top of Spring’s defaults:（默认配置）

- Inclusion of `ContentNegotiatingViewResolver` and `BeanNameViewResolver` beans.

  自动配置了视图解析器（根据方法的返回值获得视图对象，视图对象决定了如何渲染、转发、重定向）；

  ContentNegotiatingViewResolver：组合所有的视图解析器；

  可以自己给容器中添加一个视图解析器，会自动的将其组合起来；

- Support for serving static resources, including support for WebJars (covered [later in this document](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-static-content))).

  静态资源文件。

- Automatic registration of `Converter`, `GenericConverter`, and `Formatter` beans.

  `Converter`：转换器，处理页面提交数据与对象等的类型转换；

  `Formatter`：格式化器，将页面带来的数据进行数据转换时处理格式（比如，日期格式等）；

  自己添加的格式化器或转换器，只需要放在容器中即可。

- Support for `HttpMessageConverters` (covered [later in this document](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-message-converters)).

  `HttpMessageConverters`：消息转换器，SpringMVC 用来转换 Http 请求和响应的（例如：User => JSON），从容器中确定，获取所有的 `HttpMessageConverters`，实现自己的 `HttpMessageConverters`，只需要将自己的组件添加到组件中。

- Automatic registration of `MessageCodesResolver` (covered [later in this document](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-message-codes)).

  `MessageCodesResolver`：定义错误代码生成规则。

- Static `index.html` support.

  静态首页访问。

- Custom `Favicon` support (covered [later in this document](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-favicon)).

  图标。

- Automatic use of a `ConfigurableWebBindingInitializer` bean (covered [later in this document](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-web-binding-initializer)).

  也可以配置 bean 放入容器中，替换默认的。

  用来初始化 web 数据绑定，请求数据 ==> JavaBean，还牵扯到类型和格式转换。



### 扩展 SpringMVC

> If you want to keep Spring Boot MVC features and you want to add additional [MVC configuration](https://docs.spring.io/spring/docs/5.1.8.RELEASE/spring-framework-reference/web.html#mvc) (interceptors, formatters, view controllers, and other features), ***you can add your own `@Configuration` class of type `WebMvcConfigurer` but without `@EnableWebMvc`.*** If you wish to provide custom instances of `RequestMappingHandlerMapping`, `RequestMappingHandlerAdapter`, or `ExceptionHandlerExceptionResolver`, you can declare a `WebMvcRegistrationsAdapter` instance to provide such components.



编写配置类（使用注解 `@Configuration `），是 `WebMvcConfigurer` 类型的，但不使用 `@EnableWebMvc` 注解。

```java
/**
 * ClassName    spring-boot-04-web-restfulcrud-MyMvcConfig
 * Description  {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter}
 *              已被废弃，需要实现 {@code WebMvcConfigurer} 或者继承 {@code WebMvcConfigurationSupport}
 *
 * @author      xuanc
 * @date        2019/7/22 上午11:08
 * @version     1.0
 */
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/xuanc").setViewName("success");
    }
}
```

SpringBoot 在自动配置很多组件的时候，先会查看容器中有没有用户自己配置的组件，如果没有才会使用默认的，如果有，则会将用户自定义的和默认组件组合起来。**既保留了默认配置，又扩展了配置。**

在做其他配置的时候，会导入 `EnableWebMvcConfiguration.class`

```java
// Defined as a nested config to ensure WebMvcConfigurer is not read when not
	// on the classpath
	@Configuration
	@Import(EnableWebMvcConfiguration.class)
	@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
	@Order(0)
	public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer, ResourceLoaderAware {
Defined as a nested config to ensure WebMvcConfigurer is not read when not
	// on the classpath
```



```java
	/**
	 * Configuration equivalent to {@code @EnableWebMvc}.
	 */
	@Configuration
	public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration {
```

```java
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();

	// 从容器中获取所有的 WebMvcConfigurer，放到 configurers 中
	@Autowired(required = false)
	public void setConfigurers(List<WebMvcConfigurer> configurers) {
		if (!CollectionUtils.isEmpty(configurers)) {
			this.configurers.addWebMvcConfigurers(configurers);
		}
	}
```

在配置的时候，会遍历 `configurers` 里面的数据来处理，将所有的相关配置一起调用。

所以我们自己所实现的配置类也会起作用。



### 全面接管 SpringMVC

> If you want to take complete control of Spring MVC, you can add your own `@Configuration` annotated with `@EnableWebMvc`

放弃 SpringBoot 对 ***SpringMVC*** 的所有自动配置，**完全由自己配置**，全面接管。

在自己配置的类上添加上 `@Configuration` 和 `@EnableWebMvc` 注解。

**那么为什么这样会使得自动配置失效呢？**

`@EnableWebMvc` 注解的代码：

````java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
// 核心，添加了 WebMvcConfigurationSupport 的一个实现
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
````

```java
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
```

而 WebMvc 的自动配置类

```java
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
// 容器中没有这个类的时候，自动配置类才会生效
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
		ValidationAutoConfiguration.class })
public class WebMvcAutoConfiguration {
```



## 修改 SpringBoot 的默认配置

1. SpringBoot 在配置很多组件的时候，会先看容器中有没有用户自己配置的组件，如果没有的话会使用默认提供的组件，如果有用户自定义的配置，则将会将其与默认配置组合在一起。
2. 在 SpringBoot 中会有非常多的 xxxConfigurer 帮助我们进行扩展配置。



