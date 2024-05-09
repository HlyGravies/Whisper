<?php
//QUAN
    function validateUserData($pdo, $userData){
        $errorNums;
        if (isUserIdExist($pdo, $userData['userId'])) {
            $errorNums[] = "USERID_ALREADY_EXISTS";
            return $errorNums; 
        }
        
        if (empty($userData['userId'])) {
            $errorNums[] = "006";
        } elseif (strlen($userData['userId']) > 30) {
            $errorNums[] = "ERR_USERID_TOOLONG";
        }

        if (empty($userData['userName'])) {
            $errorNums[] = "011";
        } elseif (strlen($userData['userName']) > 20) {
            $errorNums[] = "ERR_USERNAME_TOOLONG";
        }

        if (empty($userData['password'])) {
            $errorNums[] = "007";
        } elseif (strlen($userData['password']) > 64) {
            $errorNums[] = "ERR_PASSWORD_TOOLONG";
        }

        if(mb_strlen($userData['profile'], 'UTF-8') > 200){
            $errorNums[] = "ERR_PROFILE_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        }else{
            return null;
        }  
    }

    function validateUserUpdateData($pdo, $userUpdateData){
        $errorNums = array();
        if (empty($userUpdateData['userId'])) {
            $errorNums[] = "006";
        } elseif (strlen($userUpdateData['userId']) > 30) {
            $errorNums[] = "ERR_USERID_TOOLONG";
        }
    
        if (empty($userUpdateData['userName'])) {
            $errorNums[] = "011";
        } elseif (strlen($userUpdateData['userName']) > 20) {
            $errorNums[] = "ERR_USERNAME_TOOLONG";
        }
    
        if (empty($userUpdateData['password'])) {
            $errorNums[] = "007";
        } elseif (strlen($userUpdateData['password']) > 64) {
            $errorNums[] = "ERR_PASSWORD_TOOLONG";
        }
    
        if(mb_strlen($userUpdateData['profile'], 'UTF-8') > 200){
            $errorNums[] = "ERR_PROFILE_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        } else {
            return null;
        }  
    }

    function validateLoginData($loginData){
        $errorNums = array();
        if (empty($loginData['userId'])) {
            $errorNums[] = "006";
        } elseif (strlen($loginData['userId']) > 30) {
            $errorNums[] = "ERR_USERID_TOOLONG";
        }
        if (empty($loginData['password'])) {
            $errorNums[] = "007";
        } elseif (strlen($loginData['password']) > 64) {
            $errorNums[] = "ERR_PASSWORD_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        } else {
            return null;
        }  
    }
    
    function validateWhisperData($pdo, $whisperData){
        $errorNums;
        if (empty($whisperData['userId'])) {
            $errorNums[] = "006";
        } elseif (strlen($whisperData['userId']) > 30) {
            $errorNums[] = "ERR_USERID_TOOLONG";
        }
    
        if (empty($whisperData['content'])) {
            $errorNums[] = "005";
        } elseif (strlen($whisperData['content']) > 256) {
            $errorNums[] = "ERR_CONTENT_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        }else{
            return null;
        }   
    }
    

    function validateFollowData($pdo, $followData){
        $errorNums;
        if (empty($followData['userId'])) {
            $errorNums[] = "006";
        } elseif (strlen($followData['userId']) > 30) {
            $errorNums[] = "ERR_USERID_TOOLONG";
        }
        if (empty($followData['followUserId'])) {
            $errorNums[] = "ERR_EMPTY_FOLLOWUSERID";
        } elseif (strlen($followData['userId']) > 30) {
            $errorNums[] = "ERR_FOLLOWUSERID_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        }else{
            return null;
        }   
    }

    function getUserInfo($pdo, $userId) {
        $getUserSql = "SELECT userId, userName, profile, iconPath FROM user WHERE userId = :userId";
        $getUserStmt = $pdo->prepare($getUserSql);
        $getUserStmt->bindParam(':userId', $userId);
        $getUserStmt->execute();
        return $getUserStmt->fetch(PDO::FETCH_ASSOC);
    }


    function getWhisperInfo($pdo, $whisperNo) {
        $getUserSql = 
            "SELECT 
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content, 
                COUNT(DISTINCT goodInfo.userId) AS goodCount
            FROM 
                whisper
            INNER JOIN 
                user ON whisper.userId = user.userId
            INNER JOIN 
                goodInfo ON whisper.whisperNo = goodInfo.whisperNo
            WHERE 
                whisper.whisperNo = :whisperNo
            GROUP BY
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content";
    
        $getUserStmt = $pdo->prepare($getUserSql);
        $getUserStmt->bindParam(':whisperNo', $whisperNo);
        $getUserStmt->execute();
        return $getUserStmt->fetch(PDO::FETCH_ASSOC);
    }
    
    function userAuthentication($pdo, $loginData){
        $getUserSql = "SELECT password FROM user WHERE userId = :userId";
        $getUserStmt = $pdo->prepare($getUserSql);
        $getUserStmt->bindParam(':userId', $loginData["userId"]);
        $getUserStmt->execute(); // Thực thi truy vấn
        $password = $getUserStmt->fetchColumn(); 

        if($loginData["password"] === $password){
            return true;
        }else{
            return false;
        }
        
    }

    function isUserIdExist($pdo, $userId){
        $getUserSql = "SELECT userId FROM user WHERE userId = :userId";
        $getUserStmt = $pdo->prepare($getUserSql);
        $getUserStmt->bindParam(':userId', $userId);
        $getUserStmt->execute();
        return $getUserStmt->fetch(PDO::FETCH_ASSOC);
    }
?>