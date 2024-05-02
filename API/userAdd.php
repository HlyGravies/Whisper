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
    $userData = json_decode(file_get_contents('php://input'), true);
    
    $errorNums = validateUserData($pdo, $userData);
    if ($errorNums === null){
        $sql = "INSERT INTO user (userId, userName, password, profile, iconPath) VALUES (:userId, :userName, :password, :profile, :iconPath)";
        try {
            $stmt = $pdo->prepare($sql);
            $stmt->bindParam(':userId', $userData['userId']);
            $stmt->bindParam(':userName', $userData['userName']);
            $stmt->bindParam(':password', $userData['password']);
            $stmt->bindParam(':profile', $userData['profile']);
            $stmt->bindParam(':iconPath', $userData['iconPath']);
            $stmt->execute();
            $userData = getUserInfo($pdo, $userData['userId']);;
            $response['result'] = "success";
            $response['userData'] = $userData;
        } catch (PDOException $e) {
            echo "Lỗi: " . $e->getMessage();
        }
    } else {
        $response = setError($response, $errorNums);
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>
