//min
<?php
require 'database/database.php';
include("mysqlConnect.php");
include("mysqlClose.php");

header('Content-Type: application/json');

$userId = $_GET['userId'] ?? null;

if (!$userId) {
    echo json_encode(["error" => "Missing userId"]);
    exit();
}

$pdo = connect_db();

$likes = get_likes_notifications($pdo, $userId);
$comments = get_comments_notifications($pdo, $userId);

$unreadCount = 0;

foreach ($likes as $like) {
    if ($like['isRead'] == 0) {
        $unreadCount++;
    }
}

foreach ($comments as $comment) {
    if ($comment['isRead'] == 0) {
        $unreadCount++;
    }
}

echo json_encode(["notificationCount" => $unreadCount]);
?>
