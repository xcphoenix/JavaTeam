冯圆圆第二周周报

### 本周进度：

1.继续配合视频学习Spring Boot的知识；

2.每天做布置的算法题；

3.学习《Java学习笔记》以前没有看完的几章。

### 遇到的问题：

1.算法题有的难的要做一两个小时。

2.正在学习的小项目用的H2数据库，默认用户名sa,第一次的密码可以随便设置，可是我就都连不上，出现 Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is org.h2.jdbc.JdbcSQLInvalidAuthorizationSpecException: Wrong user name or password 【28000-199】.

唉，最后实在不行，就新建用户并给它授权：

```mysql
CREATE USER IF NOT EXISTS sa PASSWORD 'sa';
ALTER USER sa admin true;
```

终于成功了。

！H2数据库只支持一个连接（缺点之一）。

### 下周计划：

1.继续学习《Spring实战》，继续配合视频学习Spring Boot。