<?php
include ("mysqlConnect.php");
include("mysqlClose.php");
include("errorMsgs.php");
include("function.php");
$pdo = connect_db();

$response = [
    "result"  => "error", // Mặc định là error, chỉ đổi thành success khi có dữ liệu được trả về
    "errCode" => null,    // Mã lỗi (nếu có)
    "errMsg"  => null,    // Thông báo lỗi (nếu có)
    //"userData"    => $userData    
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);

    $errorNums = validateLoginData($postData);

    if($errorNums == null){
        
    }else{
        $response = setError($response, $errorNums);
    }
    header('Content-Type: application/json');
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    
    require_once 'mysqlClose.php';


    disconnect_db($pdo);

}
?>