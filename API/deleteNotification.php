<?php
require 'database/database.php';
include("mysqlConnect.php");
include("mysqlClose.php");

header('Content-Type: application/json');
$pdo = connect_db();

$postData = json_decode(file_get_contents("php://input"), true);

if (!isset($postData['userId']) || !isset($postData['whisperNo']) || !isset($postData['type'])) {
    echo json_encode(["result" => "error", "errorDetails" => "Missing required parameters."]);
    exit();
}

$userId = $postData['userId'];
$whisperNo = $postData['whisperNo'];
$type = $postData['type'];

try {
    if ($type == "like") {
        $deleteSql = "DELETE FROM goodInfo WHERE userId = :userId AND whisperNo = :whisperNo";
    } else if ($type == "comment") {
        $deleteSql = "DELETE FROM comment WHERE userId = :userId AND whisperNo = :whisperNo";
    } else {
        echo json_encode(["result" => "error", "errorDetails" => "Invalid type parameter."]);
        exit();
    }

    $deleteStmt = $pdo->prepare($deleteSql);
    $deleteStmt->bindParam(':userId', $userId);
    $deleteStmt->bindParam(':whisperNo', $whisperNo);
    $deleteStmt->execute();

    if ($deleteStmt->rowCount() > 0) {
        echo json_encode(["result" => "success"]);
    } else {
        echo json_encode(["result" => "error", "errorDetails" => "Failed to delete notification."]);
    }

} catch (Exception $e) {
    echo json_encode(["result" => "error", "errorDetails" => $e->getMessage()]);
}
?>
