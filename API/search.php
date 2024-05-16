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
    "result" => "success",
    "errorDetails" => null
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);

    define("USER_SEARCH", "1");
    define("WHISPER_SEARCH", "2");

    switch ($postData['section']) {
        case USER_SEARCH:
            $userInfo = getUserInfo($pdo, $postData['string']);
            if ($userInfo !== false) {
                $response['userList'] = $userInfo;
            } else {
                $response = setError($response, "004");
            }
            break;
        case WHISPER_SEARCH:
            $whisperList = getWhisperInfo($pdo, $postData['string']);
            if ($whisperList != false) {
                $response['whisperList'] = $whisperList;
            } else {
                $response = setError($response, "004");
            }
            break;
        default:
            $response = setError($response, "004");
            break;
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>