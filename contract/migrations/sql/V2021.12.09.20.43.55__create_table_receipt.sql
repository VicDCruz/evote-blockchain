START TRANSACTION;

DROP TABLE IF EXISTS `Receipt`;
CREATE TABLE `Receipt` (
                           `id` int unsigned NOT NULL AUTO_INCREMENT,
                           `voterId` varchar(100) NOT NULL,
                           `value` varchar(100) NOT NULL,
                           `timestamp` datetime NOT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

COMMIT;