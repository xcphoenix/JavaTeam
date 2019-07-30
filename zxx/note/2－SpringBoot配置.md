# **Spring** Boot 配置

配置文件、加载顺序、配置原理

[TOC]

## 配置文件

SpringBoot 使用一个全局的配置文件：

- application.properties
- application.yml

配置文件的作用：修改 SpringBoot 自动配置的默认值。



以前的配置文件：使用的大多是 xml 文件。

yml 是YAML（YAML Ain't Markup Language）语言的文件，<u>以数据为中心</u>，更适合做配置文件



## YAML 语法

### 基本语法

key: value ==> 表示一对键值对（空格不能省略），且对大小写敏感。

以空格的缩进来控制层级关系；只要是左对齐的一列数据都是同一层级的数据

```yaml
server:
  	port: 8081
  	path: /hello
# 这是注释..  	
```

### 值的写法

- **字面量：普通的值（数字、字符串、布尔）**

  字面量直接写，字符串默认不用加上单引号或者双引号；

  "": 不会转义字符串的特殊字符，特殊字符作为本身要表示的意思

  ''：会转义特殊字符，特殊字符只是一个普通的字符串数据

- **对象、Map（属性和值）**

  Key: Value 对象还是键值对的形式，在下一行写对象的KV，但需要注意缩进

  ```yaml
  friends:
  	lastName: zhangsan
  	age: 20
  ```

  行内写法：

  ```yaml
  friends: {lastName: zhangshan, age: 18}
  ```

- **数组（List、Set）**

  用 `- 值` 表示数组中的一个元素

  ```yaml
  pets:
   - cat
   - dog
   - pig
  ```

  行内写法：

  ```yaml
  pets: [cat, dog, pig]
  ```

上面三种格式可以互相嵌套。



## 获取配置文件值

### 一、使用 @ConfigurationProperties

配置文件 

```yaml
person:
  lastName: zhangsan
  # 驼峰命名可以转化为横杠：last-name
  age: 18
  boss: false
  birth: 2017/12/12
  maps: {k1: v1, k2: v2}
  lists:
    - lisi
    - zhaoliu
  dog:
    name: 小狗
    age: 2
```

Java Bean

```java
@Component
@ConfigurationProperties(prefix = "person")
public class Person {

    private String lastName;
    private Integer age;
    private Boolean boss;
    private Date birth;

    private Map<String, Object> maps;
    private List<Object> lists;
    private Dog dog;
    
```

还需要导入配置文件处理器，在 pom.xml 中加入：

```xml
		<!-- 配置文件处理器 -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-configuration-processor</artifactId>
			<optional>true</optional>
		</dependency>
```



同样，在 properties文件中也可以配置，但是在里面输入**中文会出现乱码**（Spring Boot 是以 iso-8859 的编码方式读取 application.properties 配置文件）。

解决方法：

> 1. 在 .properties 中加入:
>
>    ```properties
>    spring.http.encoding.force=true
>    spring.http.encoding.charset=UTF-8
>    spring.http.encoding.enabled=true
>    server.tomcat.uri-encoding=UTF-8
>    ```
>
> 2. 将 IDEA 设置中的 File Encodings 的 Transparent native-to-ascii conversion 打钩。



**属性名匹配规则**：

- person.firstName：使用标准方式
- person.first-name：大写用 -
- person.first_name：大写用\_
- PERSON_FIRST_NAME：
  - 推荐系统属性使用这种写法



### 二、使用 @Value

使用 Spring 底层注解 `@Value` 来实现，获取配置文件或环境变量的值，或使用 SpEL



### `@ConfigurationProperties` 和 `@Value` 区别

| Feature                    | `@ConfigurationProperties` | `@Value`                 |
| -------------------------- | -------------------------- | ------------------------ |
| 功能                       | 批量注入配置文件中的属性   | 一个个指定               |
| 松散绑定（属性名匹配规则） | 支持                       | 不支持                   |
| SpEL                       | 不支持                     | 支持                     |
| JSR303数据校验             | 支持（注入时校验数据）     | 不支持                   |
| 复杂类型封装               | 支持                       | 不支持（只支持基本类型） |

数据校验：

```java
@ConfigurationProperties(prefix = "person")
@Validated
public class Person {

    @Email
    private String lastName;
```

这样，如果 lastName 不是一个有效的 email 的话会抛出异常。



### 加载指定的配置文件

使用 `@PropertySource` 和 `@ImportResource` 可以记载指定的配置文件。

使用 `@ConfigurationProperties`  默认是从全局配置文件中获取值。



 **`@PropertySource`**： 加载指定的配置文件

```java
@PropertySource(value = {"classpath:person.properties"})
@Component
@ConfigurationProperties(prefix = "person")
// @Validated
public class Person {

```

**`@ImportResource`**：导入 Spring 的配置文件，让配置文件里面的内容生效

SpringBoot 里面没有 Spring 的配置文件，自己编写的配置文件也不能自动识别，想让 Spring 的配置文件生效，加载进来，需要把 `@ImportResource` 标注在配置类上：

```java
@ImportResource(locations = {"classpath:beans.xml"})
@SpringBootApplication
public class SpringBoot02ConfigApplication {

```



SpringBoot 推荐的给容器添加组件的方式：

1. 编写配置类

2. 使用 `@Bean` 给容器中添加组件

   ```java
   @Configuration
   public class MyAppConfig {
   
       // bean 的 id 与方法名相同
       @Bean
       public HelloService helloService() {
           return new HelloService();
       }
   
   }
   ```

   

## 配置文件占位符

- RandomValuePropertySource：配置文件中可以使用随机数

  `${random.value}、${random.int}、${random.long}、${random.int(10)、${random.int[1024,65536]}`

- 属性配置占位符

  可以在配置文件中引用前面配置过的属性（优先级前面配置过得这里都能用）

  `${app.name: 默认值} `来指定找不到属性的默认值

  ```properties
  #person.last-name=张三${random.uuid}
  person.age=${random.int}
  person.birth=2017/12/12
  person.boss=false
  person.maps.k1=v1
  person.maps.k2=14
  person.lists=a,b,c,d
  person.dog.name=${person.last-name}_dog
  person.dog.age=15
  ```

  如果属性不存在，则会将占位符当做字符串处理



## Profile

Profile 是 Spring 对不同环境提供不同配置功能的支持，可以通过激活、指定参数等方式快速切换环境

### 多 profile 文件形式

格式：`application-{profile}.properties`

默认使用 application.properties 配置文件。

### YAML 文档块模式

YAML 中如果使用 `---` 可以将文件划分为不同的文档块，多个 profile 可以写在同一个文件中

```yaml
# Document1
server:
  port: 8081

person:
  lastName: zhangsan
  age: 18
  boss: false
  birth: 2017/12/12
  maps: {k1: v1, k2: v2}
  lists:
    - lisi
    - zhaoliu
  dog:
    name: 小狗
    age: 2

spring:
  profiles:
    active: dev # 指定激活哪一个 profile
---
# Document2
server:
  port: 8011
spring:
  profiles: dev # 指定文档块属于哪个 profile
---
# Document3
server:
  port: 8022
spring:
  profiles: prod
```



### 使用其他环境的配置文件的方式：

- application.properties 或 application.yml 指定

  > YAML 的优先级比 .properties 要低

  `spring.profiles.active={profile}`

  `spring:
    profiles:
      active: {profile}`

- 命令行方式（优先级较高）：

  idea 的 *program arguments* 设置 `-- spring.profiles.active={profile}`

  maven 打包后，在命令行执行并加上参数

- 虚拟机参数

  idea 的 *VM options*: `-Dspring.profiles.active={profile}` 



## 配置文件的加载位置

SpringBoot 启动会扫描以下位置的 application.properties或者 application.yml 文件作为 SpringBoot 的默认配置文件：（Idea 中类路径即 *classpath:*对应 **resources** 目录）

- file:/config/
- file:/
- classpath:/config
- classpath:/

以上按照优先级从高到低的顺序，所有位置的文件都会被加载，高优先级配置的内容会覆盖低优先级配置的内容，也可以通过配置 `spring.config.location` 来改变默认内容。

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190720084452.png)

SpringBoot 会加载上述的所有文件，只是如果配置冲突，高优先级的配置文件中的设置会覆盖低优先级配置文件的设置，低优先级的其他配置依然会生效。

例如在上述四个配置文件中分别定义不同的端口号，在 /resource/application.properties 文件中定义项目的访问路径，那么运行后低优先级配置的项目访问路径仍然有效：

```properties
server.port=8081

# 配置项目的访问路径，必须以 / 开始
server.servlet.context-path=/springboot-demo
# 新版本的配置改为：server.servlet.context-path，而不是 server.context-path
```

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190725104310.png)

### 指定配置文件路径

项目打包好后，可以使用**命令行参数**的形式，在启动项目时指定配置文件的新位置，指定的配置文件和默认加载的配置文件会共同启动作用，形成互补配置。

直接在上述的四个配置文件中指定 `spring.config.location` 并不会生效。



## 外部配置的加载顺序

[官网文档中文手册](https://qbgbook.gitbooks.io/spring-boot-reference-guide-zh/content/IV.%20Spring%20Boot%20features/24.%20Externalized%20Configuration.html)上提到的外部配置的加载顺序：

> Spring Boot允许将配置外部化（externalize），这样你就能够在不同的环境下使用相同的代码。你可以使用properties文件，YAML文件，环境变量和命令行参数来外部化配置。使用@Value注解，可以直接将属性值注入到beans中，然后通过Spring的`Environment`抽象或通过`@ConfigurationProperties`[绑定到结构化对象](http://docs.spring.io/spring-boot/docs/1.4.1.RELEASE/reference/htmlsingle/#boot-features-external-config-typesafe-configuration-properties)来访问。
>
> Spring Boot设计了一个非常特别的`PropertySource`顺序，以允许对属性值进行合理的覆盖，属性会以如下的顺序进行设值：（优先级从高到低，高优先级覆盖低优先级，形成互补）
>
> 1. home目录下的[devtools全局设置属性](http://docs.spring.io/spring-boot/docs/1.4.1.RELEASE/reference/htmlsingle/#using-boot-devtools-globalsettings)（`~/.spring-boot-devtools.properties`，如果devtools激活）。
> 2. 测试用例上的[@TestPropertySource](http://docs.spring.io/spring/docs/4.3.3.RELEASE/javadoc-api/org/springframework/test/context/TestPropertySource.html)注解。
> 3. 测试用例上的[@SpringBootTest#properties](http://docs.spring.io/spring-boot/docs/1.4.1.RELEASE/api/spring-boot-test/src/main/java/org/springframework/boot/test/context/SpringBootTest.html)注解。
> 4. **命令行参数**（多个参数可以用空格分隔开）
> 5. 来自`SPRING_APPLICATION_JSON`的属性（环境变量或系统属性中内嵌的内联JSON）。
> 6. `ServletConfig`初始化参数。
> 7. `ServletContext`初始化参数。
> 8. **来自于`java:comp/env`的JNDI属性。**
> 9. **Java系统属性（System.getProperties()）。**
> 10. **操作系统环境变量。**
> 11. **RandomValuePropertySource，只包含`random.`中的属性。**
> 12. **没有打进jar包的[Profile-specific应用属性](http://docs.spring.io/spring-boot/docs/1.4.1.RELEASE/reference/htmlsingle/#boot-features-external-config-profile-specific-properties)（`application-{profile}.properties`和YAML变量）。**
> 13. **打进jar包中的[Profile-specific应用属性](http://docs.spring.io/spring-boot/docs/1.4.1.RELEASE/reference/htmlsingle/#boot-features-external-config-profile-specific-properties)（`application-{profile}.properties`和YAML变量）。**
> 14. **没有打进jar包的应用配置（`application.properties`和YAML变量）。**
> 15. **打进jar包中的应用配置（`application.properties`和YAML变量）。**
> 16. **`@Configuration`类上的[`@PropertySource`注解](http://docs.spring.io/spring/docs/4.3.3.RELEASE/javadoc-api/org/springframework/context/annotation/PropertySource.html)。**
> 17. **默认属性（使用`SpringApplication.setDefaultProperties`指定）。**



## 自动配置的原理

配置文件可配置属性的范围：

- [官方文档](https://qbgbook.gitbooks.io/spring-boot-reference-guide-zh/content/X.%20Appendices/A.%20Common%20application%20properties.html)



自动配置原理：

1. SpringBoot 启动时，加载主配置类，开启了自动配置功能 `@EnableAutoConfiguration`

2. `@EnableAutoConfiguration` 作用

   利用 `AutoConfigurationImportSelector.class` 给容器中导入组件，里面的 `public String[] selectImports(AnnotationMetadata annotationMetadata) { ... }` 方法中，

   调用了 `protected AutoConfigurationImportSelector.AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata) {...}` 方法，这里面有一句代码是这样的：

   ```java
   List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
   ```

   通过这行代码来获取候选的配置。

   ```java
   protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
       List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
           this.getSpringFactoriesLoaderFactoryClass(),
           this.getBeanClassLoader());
       Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you are using a custom packaging, make sure that file is correct.");
       return configurations;
   }
   ```

   里面的 `loadFactoryNames` 方法的源码为：

   ```java
   public static List<String> loadFactoryNames(Class<?> factoryClass, @Nullable ClassLoader classLoader) {
       String factoryClassName = factoryClass.getName();
       return loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
   }
   
   private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
       MultiValueMap<String, String> result = cache.get(classLoader);
       if (result != null) {
           return result;
       }
   
       try {
           // 扫描所有jar包类路径下的 META-INF/spring.factories 文件
           Enumeration<URL> urls = (classLoader != null ?
                                    classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :                        ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
           result = new LinkedMultiValueMap<>();
           // 获取 url 后对每个 url 进行遍历
           while (urls.hasMoreElements()) {
               // 把扫描到的文件内容包装为 properties 对象
               URL url = urls.nextElement();
               UrlResource resource = new UrlResource(url);
               Properties properties = 
                   PropertiesLoaderUtils.loadProperties(resource);
               for (Map.Entry<?, ?> entry : properties.entrySet()) {
                   // 从 properties 对象中获取全类名
                   String factoryClassName = ((String) entry.getKey()).trim();
                   for (String factoryName : 
                        StringUtils.
                        commaDelimitedListToStringArray((String) 
                                                        entry.getValue())) {
                       // 添加
                       result.add(factoryClassName, factoryName.trim());
                   }
               }
           }
           cache.put(classLoader, result);
           return result;
       }
       catch (IOException ex) {
           throw new 
               IllegalArgumentException("Unable to load factories from location [" +　FACTORIES_RESOURCE_LOCATION + "]", ex);
       }
   }
   
   // 返回 EnableAutoConfiguration.class 类
   protected Class<?> getSpringFactoriesLoaderFactoryClass() {
       return EnableAutoConfiguration.class;
   }
   
   ```

   

   所以总的来说，是将 META-INF/spring.factories 里面的所有 EnableAutoConfiguration 的值加入到容器中。使用这些类来做自动配置：

   ![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190720100554.png)

   

3. 每一个自动配置类进行自动配置功能

4. 以 `HttpEncodingAutoConfiguration` 为例

   ```java
   @Configuration
   // 表示这是一个配置类
   @EnableConfigurationProperties(HttpProperties.class)
   // 启用指定类的 ConfigurationProperties 功能，将配置文件中对应的值与这个properties类绑定在一起，并把 HttpProperties 加入到 Ioc 容器中
   @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
   // Spring 底层注解，根据不同的条件决定配置是否生效；
   // 判断当前应用是否为 web 应用，如果是则生效
   @ConditionalOnClass(CharacterEncodingFilter.class)
   // 判断当前项目有没有这个类(Spring MVC 解决乱码中的过滤器)
   @ConditionalOnProperty(prefix = "spring.http.encoding", value = "enabled", matchIfMissing = true)
// 判断在配置文件中是否启用，如果配置这个属性，也生效
   public class HttpEncodingAutoConfiguration {
   
       // 已近与 SpringBoot 的配置文件映射了
       private final HttpProperties.Encoding properties;
   
       // 只有一个有参构造器的情况下，参数的值会从容器中获取
       public HttpEncodingAutoConfiguration(HttpProperties properties) {
           this.properties = properties.getEncoding();
       }
   
       // 给容器中添加组件，有些值需要从 properties 中获取
       @Bean
       // 容器中没有这个组件时候才会执行
       @ConditionalOnMissingBean
       public CharacterEncodingFilter characterEncodingFilter() {
           CharacterEncodingFilter filter = new 
               OrderedCharacterEncodingFilter();
           filter.setEncoding(this.properties.getCharset().name());
           filter.setForceRequestEncoding(
               this.properties.shouldForce(Type.REQUEST));
           filter.setForceResponseEncoding(
               this.properties.shouldForce(Type.RESPONSE));
           return filter;
       }
   ```
   
   根据当前不同的条件判断。决定配置类是否生效，一旦配置类生效，这个配置类就会在容器中添加各种组件，这些组件的属性是从对应的 propertie 类中获取的，而这些类的每一个属性又是和配置文件绑定的。
   
5. 所有在配置文件中能配置的属性都是在 xxxxProperties 类中封装着；**配置文件能配置什么就可以参照某个功能对应的这个属性类**

   ```java
   @ConfigurationProperties(prefix = "spring.http")
   // 从配置文件中获取指定的值和 bean 的属性进行绑定
   public class HttpProperties {
   
   ```

   

SpingBoot 的精髓：

1. SpringBoot 启动会加载大量的自动配置类
2. 我们需要的功能有没有 SpringBoot 默认写好的自动配置类
3. 在看这个自动配置类到底配置了哪些组件，如果有，就不需要自动配置了
4. 给容器中自动配置类添加组件的时候，会从 properties 类中获取某些属性，可以在配置文件中指定属性的值



### `@Conditional` 扩展注解

| @Conditional扩展注解            | 作用（判断是否满足当前指定条件）                 |
| :------------------------------ | :----------------------------------------------- |
| @ConditionalOnJava              | 系统的java版本是否符合要求                       |
| @ConditionalOnBean              | 容器中存在指定Bean；                             |
| @ConditionalOnMissingBean       | 容器中不存在指定Bean；                           |
| @ConditionalOnExpression        | 满足SpEL表达式指定                               |
| @ConditionalOnClass             | 系统中有指定的类                                 |
| @ConditionalOnMissingClass      | 系统中没有指定的类                               |
| @ConditionalOnSingleCandidate   | 容器中只有一个指定的Bean，或者这个Bean是首选Bean |
| @ConditionalOnProperty          | 系统中指定的属性是否有指定的值                   |
| @ConditionalOnResource          | 类路径下是否存在指定资源文件                     |
| @ConditionalOnWebApplication    | 当前是web环境                                    |
| @ConditionalOnNotWebApplication | 当前不是web环境                                  |
| @ConditionalOnJndi              | JNDI存在指定项                                   |

**自动配置类必须在一定的条件下才能生效；**

查看哪些配置类自动生效：

通过启用 debug 属性来打印自动配置报告：

```properties
# 开启 SpringBoot 的 debug 模式
debug=true
```

![](https://gitee.com/PhoenixBM/FigureBed/raw/picgo/img/20190720112722.png)

