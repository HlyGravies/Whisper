<!-- 
    製作者：QUAN 
-->
<?php
    function checkUserId($pdo, $userId){
        $errorNums;
        if (empty($userId)) {
            $errorNums[] = "006";
        } elseif (strlen($userId) > 30) {
            $errorNums[] = "ERR_USERID_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        }else{
            return null;
        }
    }

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
        // if (empty($userUpdateData['password'])) {
        //     $errorNums[] = "007";
        // } elseif (strlen($userUpdateData['password']) > 64) {
        //     $errorNums[] = "ERR_PASSWORD_TOOLONG";
        // }
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
        $errorNums;
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

    function validateGoodCtl($pdo, $goodData){
        $errorNums;
        if (empty($goodData['userId'])){
            $errorNums = "006";
        }
        if(empty($goodData['whisperNo'])){
            $errorNums = "008";
        }
        if(!isset($goodData['goodFlg'])){
            $errorNums = "014";
        }
        if(!empty($errorNums)){
            return $errorNums;
        }else{
            return null;
        }   
    }

?>