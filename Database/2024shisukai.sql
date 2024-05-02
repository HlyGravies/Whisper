-- MySQL dump 10.13  Distrib 5.7.24, for osx11.1 (x86_64)
--
-- Host: localhost    Database: 2024shisukai
-- ------------------------------------------------------
-- Server version	8.0.33

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `follow`
--

DROP TABLE IF EXISTS `follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `follow` (
  `userId` varchar(30) NOT NULL,
  `followUserId` varchar(30) NOT NULL,
  PRIMARY KEY (`userId`,`followUserId`),
  KEY `fk_follow_user` (`followUserId`),
  CONSTRAINT `fk_follow_user` FOREIGN KEY (`followUserId`) REFERENCES `user` (`userId`),
  CONSTRAINT `follow_user_FK` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `follow`
--

LOCK TABLES `follow` WRITE;
/*!40000 ALTER TABLE `follow` DISABLE KEYS */;
/*!40000 ALTER TABLE `follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goodInfo`
--

DROP TABLE IF EXISTS `goodInfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goodInfo` (
  `userId` varchar(30) NOT NULL,
  `whisperNo` bigint NOT NULL,
  PRIMARY KEY (`userId`,`whisperNo`),
  KEY `goodInfo_whisper_FK` (`whisperNo`),
  CONSTRAINT `goodInfo_user_FK` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`),
  CONSTRAINT `goodInfo_whisper_FK` FOREIGN KEY (`whisperNo`) REFERENCES `whisper` (`whisperNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goodInfo`
--

LOCK TABLES `goodInfo` WRITE;
/*!40000 ALTER TABLE `goodInfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `goodInfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `userId` varchar(30) NOT NULL,
  `userName` varchar(20) NOT NULL,
  `password` varchar(64) NOT NULL,
  `profile` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `iconPath` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('12345','john_doe','example_password','Lorem ipsum dolor sit amet, consectetur adipiscing elit.','path/to/icon.png'),('123453m663','john_doe','example_password','Lorem ipsum dolor sit amet, consectetur adipiscing elit.','path/to/icon.png');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `whisper`
--

DROP TABLE IF EXISTS `whisper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `whisper` (
  `whisperNo` bigint NOT NULL AUTO_INCREMENT,
  `userId` varchar(30) NOT NULL,
  `postDate` date NOT NULL,
  `content` varchar(256) NOT NULL,
  `imagePath` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`whisperNo`),
  KEY `whisper_user_FK` (`userId`),
  CONSTRAINT `whisper_user_FK` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `whisper`
--

LOCK TABLES `whisper` WRITE;
/*!40000 ALTER TABLE `whisper` DISABLE KEYS */;
/*!40000 ALTER TABLE `whisper` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-04-22 15:54:29
