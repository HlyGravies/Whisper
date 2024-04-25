<?php
//Quan
require_once 'mysqlConnect.php';
require_once 'errorMsgs.php';
require_once 'funciton.php';
$pdo = connect_db();



$response = [
    "result"  => "success",
    "errorDetails" => null
    // "errCode" => null,
    // "errMsg"  => null,
];
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
    
    $errorNums = validateUserData($pdo,$postData);
    if ($errorNums === null){
        $sql = "INSERT INTO user (userId, userName, password, profile, iconPath) VALUES (:userId, :userName, :password, :profile, :iconPath)";
        try {
            $stmt = $pdo->prepare($sql);
            $stmt->bindParam(':userId', $postData['userId']);
            $stmt->bindParam(':userName', $postData['userName']);
            $stmt->bindParam(':password', $postData['password']);
            $stmt->bindParam(':profile', $postData['profile']);
            $stmt->bindParam(':iconPath', $postData['iconPath']);
            $stmt->execute();
            $userData = getUserInfo($pdo, $postData['userId']);;
            $response['result'] = "success";
            $response['userData'] = $userData;
        } catch (PDOException $e) {
            echo "Lỗi: " . $e->getMessage();
        }
    }else{
        $response = setError($response, $errorNums);
    }
}

$response['asdfdsaf'] = $errorNums;
header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>
