<?php
//Quan
require_once 'mysqlConnect.php';
require_once 'errorMsgs.php';
require_once 'funciton.php';
$pdo = connect_db();



$response = [
    "result"  => "success", // Mặc định là error, chỉ đổi thành success khi có dữ liệu được trả về
    "errorDetails" => null
    // "errCode" => null,    // Mã lỗi (nếu có)
    // "errMsg"  => null,    // Thông báo lỗi (nếu có)
    //"userData"    => $userData    
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
    
    // Kiểm tra tất cả các biến trong $postData
    $errorNums = validateUserData($postData);
    if ($errorNums === null){
        $sql = "INSERT INTO user (userId, userName, password, profile, iconPath) VALUES (:userId, :userName, :password, :profile, :iconPath)";

        try {
            $stmt = $pdo->prepare($sql);
            // Bind parameters
            $stmt->bindParam(':userId', $postData['userId']);
            $stmt->bindParam(':userName', $postData['userName']);
            $stmt->bindParam(':password', $postData['password']);
            $stmt->bindParam(':profile', $postData['profile']);
            $stmt->bindParam(':iconPath', $postData['iconPath']);

            // Thực thi truy vấn
            $stmt->execute();

            // Lấy thông tin của người dùng vừa được thêm vào
            $userData = getUserInfo($pdo, $postData['userId']);;

            $response['result'] = "success";
            $response['userData'] = $userData;
        } catch (PDOException $e) {
            // Xử lý ngoại lệ
            echo "Lỗi: " . $e->getMessage();
        }
    }else{
        $response = setError($response, $errorNums);
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php'; // Include file chứa hàm ngắt kết nối
disconnect_db($pdo);

// if (!empty($postData['userId']) && !empty($postData['userName']) && !empty($postData['password'])) {
//     // Sử dụng prepared statements để tránh SQL Injection
//     $sql = "INSERT INTO user (userId, userName, password, profile, iconPath) VALUES (:userId, :userName, :password, :profile, :iconPath)";

//     try {
//         $stmt = $pdo->prepare($sql);
//         // Bind parameters
//         $stmt->bindParam(':userId', $postData['userId']);
//         $stmt->bindParam(':userName', $postData['userName']);
//         $stmt->bindParam(':password', $postData['password']);
//         $stmt->bindParam(':profile', $postData['profile']);
//         $stmt->bindParam(':iconPath', $postData['iconPath']);

//         // Thực thi truy vấn
//         $stmt->execute();

//         // Lấy thông tin của người dùng vừa được thêm vào
//         $userData = getUserInfo($pdo, $postData['userId']);;

//         $response['result'] = "success";
//         $response['userData'] = $userData;
//     } catch (PDOException $e) {
//         // Xử lý ngoại lệ
//         echo "Lỗi: " . $e->getMessage();
//     }
// } else {
//     // Xử lý khi có biến trống
//     if(empty($postData['userId'])){
//         $response['errMsg'] .= "\n" . getErrorMsgs(006);
//     }
//     if(empty($postData['userName'])){
//         $response['errMsg'] .= "\n" . getErrorMsgs(011);
//     }
//     if(empty($postData['password'])){
//         $response['errMsg'] .= "\n" . getErrorMsgs(007);
//     }        
// }

?>
