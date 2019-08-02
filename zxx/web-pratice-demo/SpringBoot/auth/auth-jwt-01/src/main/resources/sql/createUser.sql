CREATE DATABASE IF NOT EXISTS `jwt_test`;
USE `jwt_test`;
CREATE TABLE IF NOT EXISTS `user` (
						`id` bigint(20) NOT NULL AUTO_INCREMENT,
						`username` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
						`password` varchar(300) NOT NULL,
						`login_timestamp` timestamp NOT NULL,
						PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;