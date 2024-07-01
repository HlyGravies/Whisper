<?php
require_once 'mysqlConnect.php';
include("database/database.php");

$pdo = connect_db();

$response = [
    "result"  => "success",
    "errorDetails" => null
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $requestData = json_decode(file_get_contents('php://input'), true);

    // Lấy thông tin từ request
    $whisperNo = $requestData['whisperNo'];

    try {
        $comments = getCommentsByWhisperNo($pdo, $whisperNo);
        $response['result'] = "success";
        $response['comments'] = $comments;
    } catch (PDOException $e) {
        $response['result'] = "error";
        $response['errorDetails'] = $e->getMessage();
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

$pdo = null;
?>
