# Docker 初探

## Docker 是什么？

***Docker*** 是一个开源的应用容器引擎，开发者可以将应用及其依赖打包到一个轻量级而且可移植的容器中，来实现虚拟化。容器完全使用沙箱机制，相互之间没有任何接口，性能开销也很低，容器的启动是非常快的。



## 核心概念

**docker 主机（Host）**：安装了 Docker 程序的机器；

**docker 客户端（Client）**：客户端通过命令行或 GUI 程序连接 docker 主机进行操作；

**docker 仓库（Registry）**：用来保存各种打包好的软件镜像（搭建私有的或者公有的）；

**docker 镜像（images）**：软件打包好的镜像，放在 docker 仓库中；

**docker 容器（Container）**：镜像启动后的实例，是独立运行的一个或一组应用；



使用 docker 的步骤：

1. 安装 docker
2. 在 docker 仓库找到软件对应的镜像
3. 使用 docker 运行镜像，生成一个 docker 容器
4. 对容器的启动停止，就是对软件的启动停止



## Docker 的使用

[Docker 文档](https://docker-doc.readthedocs.io/zh_CN/latest/index.html)



### 镜像操作

| 操作         | 命令                                             | 说明                                                    |
| ------------ | :----------------------------------------------- | ------------------------------------------------------- |
| 检索         | `docker search 关键字` （`docker search redis`） | 在 docker hub 上检索镜像的信息                          |
| 拉取         | `docker pull 镜像名:tag `                        | 下载镜像，`:tag` 可选，多为软件的版本，默认是 `lastest` |
| 删除         | `docker rmi 镜像ID` 或 `docekr image rm 镜像ID`  | 删除镜像，可以使用镜像的短 ID （一般前三位以上）        |
| 查看本地镜像 | `docker images` 或 `docker image ls/list`        | 查看本地的镜像                                          |



### 容器操作

软件镜像 -- 运行镜像 -- 产生一个容器

| 操作     | 命令                                               | 说明                                                     |
| -------- | -------------------------------------------------- | -------------------------------------------------------- |
| 运行     | `docker run --name 容器名 -d 镜像`                 | `-d` 后台运行                                            |
| 列表     | `docker ps` 或 `docker container ls`               | 查看运行中的容器，`-a` 参数可以查看所有的容器            |
| 停止容器 | `docker stop 容器ID或容器名`                       | 停止运行中的容器                                         |
| 启动容器 | `docker start 容器ID`                              | 启动停止的容器                                           |
| 删除     | `docker rm 容器ID` 或 `docker container rm 容器ID` | 删除容器                                                 |
| 端口映射 | `docker run -d -p 8888:8080  --name 容器名 镜像ID` | 放主机的端口映射到容器的端口 `-p 主机端口｜容器内部端口` |
| 查看日志 | `docker logs 容器ID`                               | 查看容器日志                                             |

