<?php
include ("mysqlConnect.sql");
include("mysqlClose.sql");
include("errorMsgs.sql");

$response = [
    "result"  => "error", // Mặc định là error, chỉ đổi thành success khi có dữ liệu được trả về
    "errCode" => null,    // Mã lỗi (nếu có)
    "errMsg"  => null,    // Thông báo lỗi (nếu có)
    //"userData"    => $userData    
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);

    if (!isset($_POST['userId'])) {
        $response["errCode"] = "006"; // Ví dụ: "ユーザIDが指定されていません"
        $response["errMsg"] = $msgList[$errorNum];
        // http_response_code(400);
        echo json_encode($response);
        exit();
    } elseif (!isset($_POST['password'])) {
        $response["errCode"] = "007"; // 
        $response["errMsg"] = $msgList[$errorNum];
        // http_response_code(400);
        echo json_encode($response);
        exit();
    }

    // Lấy dữ liệu từ yêu cầu POST
    $userID = $_POST['userId'];
    $password = $_POST['password'];

    // Truy vấn kiểm tra người dùng tồn tại
    $sql = "SELECT * FROM users WHERE username='$userID' AND password='$password'";
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        // Người dùng tồn tại, trả về kết quả thành công
        $response['result'] = "success";
        echo json_encode($response);
    } else {
        // Người dùng không tồn tại, trả về mã lỗi 401
        // http_response_code(401);
        echo json_encode($response);
    }

    // Đóng kết nối đến cơ sở dữ liệu
    $conn->close();

    disconnect_db($pdo);

}
?>