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

