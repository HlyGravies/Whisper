<?php 
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

    function getTimeLineByUserId($pdo, $userId){
        $getFollowUserIdSql = "SELECT followUserId FROM follow WHERE userId = :userId";
        $getFolllowUserIdStmt = $pdo->prepare($getFollowUserIdSql);
        $getFolllowUserIdStmt->bindParam(':userId', $userId);
        $getFolllowUserIdStmt->execute();
    
        $followUserIds = $getFolllowUserIdStmt->fetchAll(PDO::FETCH_COLUMN);
        $followUserIdString = implode(', ', $followUserIds);
        $placeholders = implode(', ', array_fill(0, count($followUserIds)+1, '?'));
    
        $getTimeLineInfoSql = 
            "SELECT 
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content
            FROM 
                whisper
            INNER JOIN 
                user ON whisper.userId = user.userId
            WHERE 
                whisper.userId IN ($placeholders)
            GROUP BY
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content
            ORDER BY
                whisper.postDate DESC";
        $getTimelineInfoStmt = $pdo->prepare($getTimeLineInfoSql);
        
        foreach ($followUserIds as $key => $value) {
            $getTimelineInfoStmt->bindValue($key + 1, $value);
        }
        $getTimelineInfoStmt->bindValue(count($followUserIds) + 1, $userId);
        $getTimelineInfoStmt->execute();
        $whisperList = $getTimelineInfoStmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($whisperList as $key => $whisper) {
            $whisperList[$key]['goodFlg'] = getGoodFlag($pdo, $userId, $whisper['whisperNo']);
        }        
        return $whisperList;
    }
    
    function getGoodFlag($pdo, $userId, $whisperNo){
        $sql = "SELECT * FROM goodInfo WHERE userId =:userId AND whisperNo= :whisperNo";
        $stmtSql = $pdo->prepare($sql);
        $stmtSql -> bindParam('userId',$userId);
        $stmtSql -> bindParam('whisperNo',$whisperNo);
        $stmtSql -> execute();
        if(empty($stmtSql->fetch(PDO::FETCH_ASSOC))){
            return false;
        }else{
            return true;
        }
    }


    function getUserWhisperInfo($pdo, $postData){
        //Get WhisperList
        $getAllWhisperSql = 
            "SELECT 
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content
            FROM 
                whisper
            INNER JOIN 
                user ON whisper.userId = user.userId
            WHERE 
                whisper.userId = :userId
            GROUP BY
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content
            ORDER BY
                whisper.postDate DESC";

        $getAllWhisperstmt = $pdo -> prepare($getAllWhisperSql);
        $getAllWhisperstmt -> bindParam(':userId', $postData['userId']);
        $getAllWhisperstmt -> execute();
        $whisperList = $getAllWhisperstmt->fetchAll(PDO::FETCH_ASSOC);

        foreach ($whisperList as &$whisper) {
            $whisper['goodFlg'] = getGoodFlag($pdo, $postData['loginUserId'], $whisper['whisperNo']);
        }
        unset($whisper);

        //Get いいねList
        $sql = "SELECT whisperNo FROM whisper WHERE userId = :userId";
        $stmt = $pdo -> prepare($sql);
        $stmt -> bindParam(':userId', $postData['userId']);
        $getAllLikedWhisperSql = "SELECT 
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content
            FROM 
                whisper
            INNER JOIN 
                user ON whisper.userId = user.userId
            WHERE 
                goodInfo.userId IN ()
            GROUP BY
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                whisper.postDate, 
                whisper.content
            ORDER BY
                whisper.postDate DESC";
        $getAllLikedWhisperStmt = $pdo -> prepare($getAllLikedWhisperSql);
        $getAllLikedWhisperStmt -> bindParam(':userId', $postData['userId']);
        $getAllLikedWhisperStmt -> execute();
        $getAllLikedWhisperList = $getAllLikedWhisperStmt -> fetchAll(PDO::FETCH_ASSOC);;
        return $getAllLikedWhisperList;
    }


    function userAuthentication($pdo, $loginData){
        $getUserSql = "SELECT password FROM user WHERE userId = :userId";
        $getUserStmt = $pdo->prepare($getUserSql);
        $getUserStmt->bindParam(':userId', $loginData["userId"]);
        $getUserStmt->execute(); 
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