<?php
/*
    製作者：Min 
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
    
    if (isset($postData['whisperNo']) && isset($postData['loginUserId'])) {  // Check if both whisperNo and loginUserId are set
        $whisperNo = $postData['whisperNo'];
        $loginUserId = $postData['loginUserId'];  // Get loginUserId from POST data

        $whisper = getWhisperDetail($pdo, $whisperNo, $loginUserId);  // Pass loginUserId to the function

        if ($whisper) {
            $response['data'] = $whisper;
        } else {
            $response = setError($response, "004");
        }
    } else {
        $response = setError($response, "Missing required parameters.");
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>