<?php
/*
    製作者：QUAN 
*/

include ("mysqlConnect.php");
include("mysqlClose.php");
include("errorMsgs.php");
include("database/database.php");
include("validation/validation.php");
$pdo = connect_db();

$response = [
    "result" => "error",
    "errCode" => null,
    "errMsg" => null,
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
    $errorNums = validateLoginData($postData);
    if ($errorNums === null) {
        if (isUserIdExist($pdo, $postData['userId']) && userAuthentication($pdo, $postData)) {
            $response["result"] = "success";
            $response["userData"] = getUserInfo($pdo, $postData["userId"]);
        }else{
            $errorNums[] = "003";
            $response = setError($response, $errorNums);
        }
    } else {
        $response = setError($response, $errorNums);
    }
    header('Content-Type: application/json');
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    require_once 'mysqlClose.php';
    disconnect_db($pdo);
}
?>