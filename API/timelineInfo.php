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
    "result"  => "error",
    "errCode" => null, 
    "errMsg"  => null,  
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
    $errorNums = checkUserId($pdo, $postData['userId']);
    if ($errorNums === null){
            $response['whisperList'] = getTimeLineByUserId($pdo, $postData['userId']);
        }
        else{
            $response = setError($response, $errorNums);
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>
