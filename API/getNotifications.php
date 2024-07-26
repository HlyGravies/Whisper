//min

<?php
include("database/database.php");
include("mysqlConnect.php");
include("mysqlClose.php");

header('Content-Type: application/json');
$pdo = connect_db();

$postData = json_decode(file_get_contents("php://input"), true);

if (!isset($postData['userId'])) {
    echo json_encode(["result" => "error", "errorDetails" => "Missing required parameters."]);
    exit();
}

$userId = $postData['userId'];

try {
    $likes = get_likes_notifications($pdo, $userId);
    $comments = get_comments_notifications($pdo, $userId);

    echo json_encode(["result" => "success", "likes" => $likes, "comments" => $comments]);

} catch (Exception $e) {
    echo json_encode(["result" => "error", "errorDetails" => $e->getMessage()]);
}
?>
