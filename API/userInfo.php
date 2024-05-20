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
    $userId = json_decode(file_get_contents('php://input'), true)['userId'];

    $errorNums = null;
    if(isUserIdExist($pdo, $userId) == false){
        $errorNums = "006";
    }
    if ($errorNums === null){
        try {
            $userData = getUserInfo($pdo, $userId);
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
