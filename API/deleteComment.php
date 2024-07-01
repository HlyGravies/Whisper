<?php
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
    $data = json_decode(file_get_contents('php://input'), true);

    if(!isset($data["commentId"]) || !isset($data["whisperNo"]) || !isset($data["userId"])) {
        $response['result'] = "error";
        $response['errorDetails'] = "Invalid parameters.";
    } else {
        $commentId = $data["commentId"];
        $whisperNo = $data["whisperNo"];
        $userId = $data["userId"]; // User ID của người yêu cầu xóa comment
        
        if(!isCommentIdExist($pdo, $commentId)) {
            $response['result'] = "error";
            $response['errorDetails'] = "ERR_COMMENTID_NOT_FOUND";
        } else {
            // Kiểm tra quyền sở hữu
            if (!isCommentOwner($pdo, $commentId, $userId) && !isWhisperOwner($pdo, $whisperNo, $userId)) {
                $response['result'] = "error";
                $response['errorDetails'] = "Permission denied.";
            } else {
                //$sql = "DELETE FROM goodInfo WHERE commentId = :commentId";
                $sql2 = "DELETE FROM comment WHERE commentId = :commentId";
                $sql3 = "UPDATE whisper SET commentCount = commentCount - 1 WHERE whisperNo = :whisperNo";
                
                try {
                    $pdo->beginTransaction();

                    // Xóa các lượt like của comment
                    // $stmt = $pdo->prepare($sql);
                    // $stmt->bindParam('commentId', $commentId);
                    // $stmt->execute();

                    // Xóa comment
                    $stmt2 = $pdo->prepare($sql2);
                    $stmt2->bindParam('commentId', $commentId);
                    $stmt2->execute();

                    // Cập nhật lại số lượng comment
                    if ($stmt2->rowCount() > 0) {
                        $stmt3 = $pdo->prepare($sql3);
                        $stmt3->bindParam('whisperNo', $whisperNo);
                        $stmt3->execute();
                    }

                    $pdo->commit();
                    $response['result'] = "success";
                } catch (PDOException $e) {
                    $pdo->rollBack();
                    $response['result'] = "error";
                    $response['errorDetails'] = $e->getMessage();
                }
            }
        }
    }
    header('Content-Type: application/json');
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    require_once 'mysqlClose.php';
    disconnect_db($pdo);
}
?>
