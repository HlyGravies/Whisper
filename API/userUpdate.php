<?php
/*
    製作者：QUAN 
*/

require_once 'mysqlConnect.php';
require_once 'errorMsgs.php';
include("database/database.php");
include("validation/validation.php");
$pdo = connect_db();
$response = [
    "result"  => "success",
    "errorDetails" => null
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $userData = json_decode(file_get_contents('php://input'), true);
    
    $errorNums = validateUserUpdateData($pdo, $userData);
    if ($errorNums === null){
        $iconPath = "";
        if (isset($_FILES['image']) && $_FILES['image']['error'] == 0) {
            $targetDir = "images/";
            if (!file_exists($targetDir)) {
                mkdir($targetDir, 0777, true);
            }

            $fileName = basename($_FILES['image']['name']);
            $targetFilePath = $targetDir . $fileName;
            $fileType = pathinfo($targetFilePath, PATHINFO_EXTENSION);
            $allowTypes = array('jpg', 'png', 'jpeg');
            
            if (in_array($fileType, $allowTypes)) {
                if (move_uploaded_file($_FILES['image']['tmp_name'], $targetFilePath)) {
                    $iconPath = $targetFilePath;
                } else {
                    $response['result'] = "error";
                    $response['errorDetails'] = "Không thể tải lên hình ảnh.";
                }
            } else {
                $response['result'] = "error";
                $response['errorDetails'] = "Chỉ cho phép các định dạng JPG, JPEG, PNG.";
            }
        }

        if ($response['result'] === "success") {
            $sql = "UPDATE user
                SET userName = :userName,
                    -- password = :password,
                    profile = :profile,
                    iconPath = :iconPath    
                WHERE userId = :userId;
                ";
            try {
                $stmt = $pdo->prepare($sql);
                $stmt->bindParam(':userId', $userData['userId']);
                $stmt->bindParam(':userName', $userData['userName']);
                // $stmt->bindParam(':password', $userData['password']);
                $stmt->bindParam(':profile', $userData['profile']);
                $stmt->bindParam(':iconPath', $iconPath);
                $stmt->execute();
                $userData = getUserInfo($pdo, $userData['userId']);;
                $response['result'] = "success";
                $response['userData'] = $userData;
            } catch (PDOException $e) {
                echo "Lỗi: " . $e->getMessage();
            }
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
