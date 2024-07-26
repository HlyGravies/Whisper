//min


<?php
require_once 'mysqlConnect.php';

$pdo = connect_db();

$response = [
    "result"  => "success",
    "errorDetails" => null
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $commentData = json_decode(file_get_contents('php://input'), true);
    $sql = "INSERT INTO comment (whisperNo, userId, content) VALUES (:whisperNo, :userId, :content)";
    $updateSql = "UPDATE whisper SET commentCount = commentCount + 1 WHERE whisperNo = :whisperNo";

    try {
        // Begin transaction
        $pdo->beginTransaction();

        // Insert comment
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':whisperNo', $commentData['whisperNo']);
        $stmt->bindParam(':userId', $commentData['userId']);
        $stmt->bindParam(':content', $commentData['content']);
        $stmt->execute();

        // Update comment count in whisper
        $updateStmt = $pdo->prepare($updateSql);
        $updateStmt->bindParam(':whisperNo', $commentData['whisperNo']);
        $updateStmt->execute();

        // Commit transaction
        $pdo->commit();
        
        $response['result'] = "success";
    } catch (PDOException $e) {
        // Rollback transaction in case of error
        $pdo->rollBack();
        $response['result'] = "error";
        $response['errorDetails'] = $e->getMessage();
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

// Đóng kết nối cơ sở dữ liệu
$pdo = null;
?>
