<?php
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
    $userData = $_POST;
    
    $errorNums = validateUserUpdateData($pdo, $userData);
    if ($errorNums === null){
        $iconPath = "";
        if (isset($_FILES['iconPath'])){
            if ($_FILES['iconPath']['error'] == 0) {
            $targetDir = "images/";
            if (!file_exists($targetDir)) {
                mkdir($targetDir, 0777, true);
            }

            $fileName = basename($_FILES['iconPath']['name']);
            $targetFilePath = $targetDir . $fileName;
            $fileType = pathinfo($targetFilePath, PATHINFO_EXTENSION);
            $allowTypes = array('jpg', 'png', 'jpeg');
            
            if (in_array($fileType, $allowTypes)) {
                if (move_uploaded_file($_FILES['iconPath']['tmp_name'], $targetFilePath)) {
                    $iconPath = $targetFilePath;
                } else {
                    $response['result'] = "error";
                    $response['errorDetails'] = "Không thể tải lên hình ảnh.";
                }
            } else {
                $response['result'] = "error";
                $response['errorDetails'] = "Chỉ cho phép các định dạng JPG, JPEG, PNG.";
            }
        } else {
            // Có lỗi xảy ra, cập nhật errorDetails
            switch ($_FILES['iconPath']['error']) {
                case UPLOAD_ERR_INI_SIZE:
                case UPLOAD_ERR_FORM_SIZE:
                    $response['errorDetails'] = "Kích thước file quá lớn.";
                    break;
                case UPLOAD_ERR_PARTIAL:
                    $response['errorDetails'] = "File chỉ được tải lên một phần.";
                    break;
                case UPLOAD_ERR_NO_FILE:
                    $response['errorDetails'] = "Không có file nào được tải lên.";
                    break;
                case UPLOAD_ERR_NO_TMP_DIR:
                    $response['errorDetails'] = "Thiếu thư mục tạm.";
                    break;
                case UPLOAD_ERR_CANT_WRITE:
                    $response['errorDetails'] = "Không thể ghi file vào đĩa.";
                    break;
                case UPLOAD_ERR_EXTENSION:
                    $response['errorDetails'] = "Một extension PHP đã ngăn chặn việc tải file lên.";
                    break;
                default:
                    $response['errorDetails'] = "Lỗi không xác định.";
                    break;
            }
            $response['result'] = "error";
        }
    } else {
        $response['result'] = "error";
        $response['errorDetails'] = "Không tìm thấy file 'iconPath'.";
    }

        if ($response['result'] === "success") {
            $sql = "UPDATE user
                SET userName = :userName,
                    profile = :profile,
                    iconPath = :iconPath    
                WHERE userId = :userId;
                ";
            try {
                $stmt = $pdo->prepare($sql);
                $stmt->bindParam(':userId', $userData['userId']);
                $stmt->bindParam(':userName', $userData['userName']);
                $stmt->bindParam(':profile', $userData['profile']);
                $stmt->bindParam(':iconPath', $iconPath);
                $stmt->execute();
                $userData = getUserInfo($pdo, $userData['userId']);;
                $response['result'] = "success";
                $response['userData'] = $userData;
            } catch (PDOException $e) {
                $response['result'] = "error";
                $response['errorDetails'] = "Lỗi: " . $e->getMessage();
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