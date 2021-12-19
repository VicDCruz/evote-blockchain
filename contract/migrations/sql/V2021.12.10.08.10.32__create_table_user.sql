START TRANSACTION;

DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
                        `id` int unsigned NOT NULL AUTO_INCREMENT,
                        `registerId` varchar(50) NOT NULL,
                        `firstName` varchar(50) NOT NULL,
                        `lastName` varchar(50) DEFAULT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

COMMIT;