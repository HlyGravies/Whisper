<?php 
    /* 
        製作者：QUAN 
    */

function getUserInfo($pdo, $userId) {
    $getUserSql = "SELECT userId, userName, profile, iconPath FROM user WHERE userId = :userId";
    $getUserStmt = $pdo->prepare($getUserSql);
    $getUserStmt->bindParam(':userId', $userId);
    $getUserStmt->execute();
    return $getUserStmt->fetch(PDO::FETCH_ASSOC);
}

function getTimeLineByUserId($pdo, $userId) {
    $getFollowUserIdSql = "SELECT followUserId FROM follow WHERE userId = :userId";
    $getFolllowUserIdStmt = $pdo->prepare($getFollowUserIdSql);
    $getFolllowUserIdStmt->bindParam(':userId', $userId);
    $getFolllowUserIdStmt->execute();

    $followUserIds = $getFolllowUserIdStmt->fetchAll(PDO::FETCH_COLUMN);
    $followUserIdString = implode(', ', $followUserIds);
    $placeholders = implode(', ', array_fill(0, count($followUserIds) + 1, '?'));

    $getTimeLineInfoSql = 
        "SELECT 
            whisper.whisperNo, 
            whisper.userId, 
            user.userName, 
            user.iconPath, 
            whisper.postDate, 
            whisper.content,
            whisper.commentCount, -- Thêm commentCount vào SELECT
            COUNT(DISTINCT goodInfo.userId) AS goodCount
        FROM 
            whisper
        INNER JOIN 
            user ON whisper.userId = user.userId
        LEFT JOIN
            goodInfo ON whisper.whisperNo = goodInfo.whisperNo
        WHERE 
            whisper.userId IN ($placeholders)
        GROUP BY
            whisper.whisperNo, 
            whisper.userId, 
            user.userName, 
            user.iconPath, 
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
            user.iconPath,
            whisper.postDate, 
            whisper.content,
            whisper.commentCount,
            COUNT(DISTINCT goodInfo.userId) AS goodCount
        FROM 
            whisper
        INNER JOIN 
            user ON whisper.userId = user.userId
        LEFT JOIN
            goodInfo ON whisper.whisperNo = goodInfo.whisperNo
        WHERE 
            whisper.userId = :userId
        GROUP BY
            whisper.whisperNo, 
            whisper.userId, 
            user.userName, 
            user.iconPath,
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

    if($whisperList == null){
        $userWhipserInfo['whisperList'] = null;
    }else{
        $userWhipserInfo['whisperList'] = $whisperList;
    }

    //Get いいねList
    $sql = "SELECT whisperNo FROM goodInfo WHERE userId = :userId";
    $stmt = $pdo -> prepare($sql);
    $stmt -> bindParam(':userId', $postData['userId']);
    $stmt -> execute();
    $results = $stmt ->fetchAll(PDO::FETCH_ASSOC);

    $likedWhisperNos = array();
    foreach ($results as $result) {
        $likedWhisperNos[] = $result['whisperNo'];
    }
    $likedWhisperNos = implode(',', $likedWhisperNos);

    if($likedWhisperNos != null){
    $getAllLikedWhisperSql = 
        "SELECT 
            whisper.whisperNo, 
            whisper.userId, 
            user.userName, 
            user.iconPath,
            whisper.postDate, 
            whisper.content,
            whisper.commentCount,
            COUNT(DISTINCT goodInfo.userId) AS goodCount
            FROM 
                whisper
            INNER JOIN 
                user ON whisper.userId = user.userId
            LEFT JOIN
                goodInfo ON whisper.whisperNo = goodInfo.whisperNo
            WHERE 
                goodInfo.whisperNo IN ($likedWhisperNos)
            GROUP BY
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                user.iconPath,
                whisper.postDate, 
                whisper.content
            ORDER BY
                whisper.postDate DESC";
        $getAllLikedWhisperStmt = $pdo -> prepare($getAllLikedWhisperSql);
        $getAllLikedWhisperStmt -> execute();
        $AllLikedWhisperList = $getAllLikedWhisperStmt -> fetchAll(PDO::FETCH_ASSOC);

        foreach ($AllLikedWhisperList as &$whisper) {
            $whisper['goodFlg'] = getGoodFlag($pdo, $postData['loginUserId'], $whisper['whisperNo']);
        }
        unset($whisper);
        $userWhipserInfo['allLikedWhisperList'] = $AllLikedWhisperList;
        return $userWhipserInfo;
    }else{
        $userWhipserInfo['allLikedWhisperList'] = null;
        return $userWhipserInfo;
    }
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

function isWhisperNoExist($pdo, $whisperNo){
    $sql = "SELECT whisperNo FROM whisper WHERE whisperNo = :whisperNo";
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':whisperNo', $whisperNo);
    $stmt->execute();
    return $stmt->fetch(PDO::FETCH_ASSOC);
}

function getFollowerInfo($pdo, $userId) {
    $userData = []; 
    // getFollowList
    $getFollowUserIdSql = "SELECT followUserId FROM follow WHERE userId = :userId";
    $getFollowUserIdStmt = $pdo->prepare($getFollowUserIdSql);
    $getFollowUserIdStmt->bindParam(':userId', $userId);
    $getFollowUserIdStmt->execute();
    $followUserIds = $getFollowUserIdStmt->fetchAll(PDO::FETCH_COLUMN);
    
    $userData['followList'] = [];
    foreach ($followUserIds as $followUserId) {
        $userData['followList'][] = getUserAndFollowInfo($pdo, $followUserId);
    }

    // getFollowerList
    $getFollowerUserIdSql = "SELECT userId FROM follow WHERE followUserId = :userId"; 
    $getFollowerUserIdStmt = $pdo->prepare($getFollowerUserIdSql);
    $getFollowerUserIdStmt->bindParam(':userId', $userId);
    $getFollowerUserIdStmt->execute();
    $followerUserIds = $getFollowerUserIdStmt->fetchAll(PDO::FETCH_COLUMN);

    $userData['followerList'] = [];
    foreach ($followerUserIds as $followerUserId) {
        $userData['followerList'][] = getUserAndFollowInfo($pdo, $followerUserId);
    }

    return $userData;
}

function getUserAndFollowInfo($pdo, $userId){
    $sql = 
        "SELECT 
            user.userId,
            user.userName,
            user.iconPath,
            (SELECT COUNT(whisper.whisperNo) FROM whisper WHERE whisper.userId = user.userId) AS whisperCount,
            (SELECT COUNT(follow.followUserId) FROM follow WHERE follow.userId = user.userId) AS followCount, -- số người mình follow
            (SELECT COUNT(follow.userId) FROM follow WHERE follow.followUserId = user.userId) AS followerCount -- số người follow mình
        FROM 
            user
        WHERE
            user.userId = :userId
    ";
    $stmt = $pdo -> prepare($sql);
    $stmt -> bindParam('userId',$userId);
    $stmt -> execute();
    return $stmt->fetch(PDO::FETCH_ASSOC);
}

function getUserByUserName($pdo, $userName) {
    $sql = 
        "SELECT 
            user.userId,
            user.userName,
            user.iconPath,
            (SELECT COUNT(whisper.whisperNo) FROM whisper WHERE whisper.userId = user.userId) AS whisperCount,
            (SELECT COUNT(follow.followUserId) FROM follow WHERE follow.userId = user.userId) AS followCount, -- số người mình follow
            (SELECT COUNT(follow.userId) FROM follow WHERE follow.followUserId = user.userId) AS followerCount -- số người follow mình
        FROM 
            user
        WHERE
            user.userName LIKE :userName
        ";
    $stmt = $pdo->prepare($sql);
    $userName = '%' . $userName . '%';
    $stmt->bindParam(':userName', $userName); 
    $stmt->execute();    
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}


function getWhisperByContent($pdo, $content) {
    $Sql = 
        "SELECT 
            whisper.whisperNo, 
            whisper.userId, 
            user.userName, 
            user.iconPath,
            whisper.postDate, 
            whisper.content, 
            COALESCE(goodCounts.goodCount, 0) AS goodCount
        FROM 
            whisper
        INNER JOIN 
            user ON whisper.userId = user.userId
        LEFT JOIN 
            (SELECT whisperNo, COUNT(DISTINCT userId) AS goodCount 
            FROM goodInfo 
            GROUP BY whisperNo) AS goodCounts 
        ON whisper.whisperNo = goodCounts.whisperNo
        WHERE 
            whisper.content LIKE :content";

    $stmt = $pdo->prepare($Sql);
    $content = '%' . $content . '%';
    $stmt->bindParam(':content', $content);
    $stmt->execute();
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getCommentsByWhisperNo($pdo, $whisperNo) {
    $sql = "
        SELECT 
            comment.commentId,
            comment.whisperNo,
            comment.userId,
            user.userName,
            user.iconPath,
            comment.content,
            comment.commentDate,
            COUNT(comment_like.userId) AS likeCount
        FROM 
            comment
        INNER JOIN 
            user ON comment.userId = user.userId
        LEFT JOIN 
            comment_like ON comment.commentId = comment_like.commentId
        WHERE 
            comment.whisperNo = :whisperNo
        GROUP BY 
            comment.commentId,
            comment.whisperNo,
            comment.userId,
            user.userName,
            user.iconPath,
            comment.content,
            comment.commentDate
        ORDER BY 
            comment.commentDate DESC";

    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':whisperNo', $whisperNo);
    $stmt->execute();
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getWhisperDetail($pdo, $whisperNo, $loginUserId) {
    $sql = "SELECT 
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                user.iconPath,
                whisper.postDate, 
                whisper.content,
                whisper.commentCount,
                COUNT(DISTINCT goodInfo.userId) AS goodCount
            FROM 
                whisper
            INNER JOIN 
                user ON whisper.userId = user.userId
            LEFT JOIN
                goodInfo ON whisper.whisperNo = goodInfo.whisperNo
            WHERE 
                whisper.whisperNo = :whisperNo
            GROUP BY
                whisper.whisperNo, 
                whisper.userId, 
                user.userName, 
                user.iconPath,
                whisper.postDate, 
                whisper.content,
                whisper.commentCount
            ORDER BY
                whisper.postDate DESC";

    try {
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':whisperNo', $whisperNo);
        $stmt->execute();
        $whisper = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($whisper) {
            $whisper['goodFlg'] = getGoodFlag($pdo, $loginUserId, $whisperNo);
        }

        return $whisper;
    } catch (PDOException $e) {
        throw new Exception("Database error: " . $e->getMessage());
    }
}
function getGoodFlagWhisper($pdo, $userId, $whisperNo) {
    $sql = "SELECT COUNT(*) FROM goodInfo WHERE userId = :userId AND whisperNo = :whisperNo";
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':userId', $userId);
    $stmt->bindParam(':whisperNo', $whisperNo);
    $stmt->execute();
    $count = $stmt->fetchColumn();

    return $count > 0 ? true : false;
}

function get_likes_notifications($pdo, $userId) {
    $likesSql = "
        SELECT 
            goodInfo.whisperNo, 
            user.userName AS likerName,
            user.iconPath AS likerIcon,
            whisper.content AS whisperContent,
            whisper.imagePath AS whisperImage,
            user.userId AS likerId,
            goodInfo.isRead AS isRead
        FROM goodInfo
        INNER JOIN user ON goodInfo.userId = user.userId
        INNER JOIN whisper ON goodInfo.whisperNo = whisper.whisperNo
        WHERE whisper.userId = :userId AND goodInfo.userId != whisper.userId";
    $likesStmt = $pdo->prepare($likesSql);
    $likesStmt->bindParam(':userId', $userId);
    $likesStmt->execute();
    return $likesStmt->fetchAll(PDO::FETCH_ASSOC);
}

function get_comments_notifications($pdo, $userId) {
    $commentsSql = "
        SELECT 
            comment.whisperNo,
            comment.userId,
            user.userName AS commenterName,
            user.iconPath AS commenterIcon,
            comment.content AS commentContent,
            comment.commentDate,
            whisper.content AS whisperContent,
            whisper.imagePath AS whisperImage,
            user.userId AS commenterId,
            comment.isRead AS isRead
        FROM comment
        INNER JOIN user ON comment.userId = user.userId
        INNER JOIN whisper ON comment.whisperNo = whisper.whisperNo
        WHERE whisper.userId = :userId AND comment.userId != whisper.userId";
    $commentsStmt = $pdo->prepare($commentsSql);
    $commentsStmt->bindParam(':userId', $userId);
    $commentsStmt->execute();
    return $commentsStmt->fetchAll(PDO::FETCH_ASSOC);
}

function update_notification_status($pdo, $userId, $whisperNo, $type) {
    if ($type == "like") {
        $updateSql = "UPDATE goodInfo SET isRead = TRUE WHERE userId = :userId AND whisperNo = :whisperNo";
    } else if ($type == "comment") {
        $updateSql = "UPDATE comment SET isRead = TRUE WHERE userId = :userId AND whisperNo = :whisperNo";
    } else {
        throw new Exception("Invalid type parameter.");
    }

    $updateStmt = $pdo->prepare($updateSql);
    $updateStmt->bindParam(':userId', $userId);
    $updateStmt->bindParam(':whisperNo', $whisperNo);
    $updateStmt->execute();

    if ($updateStmt->rowCount() > 0) {
        return true;
    } else {
        throw new Exception("Failed to update isRead status.");
    }
}


?> 