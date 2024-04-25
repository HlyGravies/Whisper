<?php
    function validateUserData($postData){
    $errorNums;
    if (empty($postData['userId'])) {
        $errorNums[] = "006";
    } elseif (strlen($postData['userId']) > 30) {
        $errorNums[] = "ERR_USERID_TOOLONG";
    }

    if (empty($postData['userName'])) {
        $errorNums[] = "011";
    } elseif (strlen($postData['userName']) > 20) {
        $errorNums[] = "ERR_USERNAME_TOOLONG";
    }

    if (empty($postData['password'])) {
        $errorNums[] = "007";
    } elseif (strlen($postData['password']) > 64) {
        $errorNums[] = "ERR_PASSWORD_TOOLONG";
    }

    if(mb_strlen($postData['profile'], 'UTF-8') > 200){
        $errorNums[] = "ERR_PROFILE_TOOLONG";
    }

    // if(strlen($postData['iconPath']) > 100){
    //     $errorNums[] = "ERR_ICONPATH_TOOLONG";
    // }

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
?>