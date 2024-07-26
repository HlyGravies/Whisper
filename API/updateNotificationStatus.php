//min

<?php
include("database/database.php");
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
    update_notification_status($pdo, $userId, $whisperNo, $type);
    echo json_encode(["result" => "success"]);
} catch (Exception $e) {
    echo json_encode(["result" => "error", "errorDetails" => $e->getMessage()]);
}
?>
