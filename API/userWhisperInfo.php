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
    $postData = json_decode(file_get_contents('php://input'), true); 
    
    if (isUserIdExist($pdo, $postData['userId']) != false && isUserIdExist($pdo, $postData['loginUserId']) != false){
        $response['data'] = getUserWhisperInfo($pdo, $postData);
    }else{
        $response = setError($response, ["ERR_USERID_NOT_FOUND"]);
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>