# 周报​

## 本周进度
- SpringBoot 看到了异常处理
- 开始重看 MySQL
- 写了一些算法题

## 遇到的问题
- favixon.ico 在设置了 `server.servlet.context-path` 之后不生效，开始刷新缓存好多次也没用，最后发现是浏览器获取图标的默认uri不会加上设置的项目名。只能手动在 html 文件里指定图标。
  
  > 很迷的一点是，在处理拦截器对静态资源的处理时，如果把 `/static/**`映射到 resource 下面的 static 目录，static 根目录下存放有 favicon.ico 图标，那么访问 `http://localhost:8080/favicon.ico` 和 `http://localhost:8080/static/favicon.ico` 都可以访问到。

- 异常处理那部分，和视频上不同的是没有显示异常名 和 异常堆栈信息，model 里面的相关值都是 FALSE，发现需要设置一些属性，但是看ErrorProperties 源码看不到配置是怎么注入进来的…… 类也没加注释从配置文件获取值……迷的很

## 下周计划
- 继续看 SpringBoot 和数据库
- 争取每天在 leetcode 刷题（以前算法题都是用c写……）
- 打算看看 tomcat ，了解下内部原理



