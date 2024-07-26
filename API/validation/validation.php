
<?php
     /* 
        製作者：QUAN 
    */
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
    
    function validateUserUpdateData($pdo, $userUpdateData){
        $errorNums = array(); // Ensure $errorNums is always initialized as an array.
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
        // Check if 'profile' is set and not null before using it in mb_strlen
        if(isset($userUpdateData['profile']) && mb_strlen($userUpdateData['profile'], 'UTF-8') > 200){
            $errorNums[] = "ERR_PROFILE_TOOLONG";
        }
        if(!empty($errorNums)){
            return $errorNums;
        } else {
            return null;
        }  
    }

    function validateUserData($pdo, $userData) {
        $errorNums = [];
        
        // Check if userId is empty
        if (empty($userData['userId'])) {
            $errorNums[] = "001"; // Example error code for empty userId
        }
        
        // Check if userName is empty
        if (empty($userData['userName'])) {
            $errorNums[] = "002"; // Example error code for empty userName
        }
        
        // Check if password is empty
        if (empty($userData['password'])) {
            $errorNums[] = "003"; // Example error code for empty password
        }
        
        // Check if userId already exists in the database
        $stmt = $pdo->prepare("SELECT COUNT(*) FROM user WHERE userId = :userId");
        $stmt->bindParam(':userId', $userData['userId']);
        $stmt->execute();
        if ($stmt->fetchColumn() > 0) {
            $errorNums[] = "004"; // Example error code for duplicate userId
        }
        
        return empty($errorNums) ? null : $errorNums;
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

    function isCommentIdExist($pdo, $commentId) {
        $sql = "SELECT COUNT(*) FROM comment WHERE commentId = :commentId";
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':commentId', $commentId);
        $stmt->execute();
        return $stmt->fetchColumn() > 0;
    }
    function isCommentOwner($pdo, $commentId, $userId) {
        $sql = "SELECT COUNT(*) FROM comment WHERE commentId = :commentId AND userId = :userId";
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':commentId', $commentId);
        $stmt->bindParam(':userId', $userId);
        $stmt->execute();
        return $stmt->fetchColumn() > 0;
    }
    
    function isWhisperOwner($pdo, $whisperNo, $userId) {
        $sql = "SELECT COUNT(*) FROM whisper WHERE whisperNo = :whisperNo AND userId = :userId";
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':whisperNo', $whisperNo);
        $stmt->bindParam(':userId', $userId);
        $stmt->execute();
        return $stmt->fetchColumn() > 0;
    }
    
    

?>