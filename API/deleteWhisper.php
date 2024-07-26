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
    
    if($data["whisperNo"] == null){
        $errorNums[] = '008';
    }
    if(!isWhisperNoExist($pdo, $data["whisperNo"])){
        $errorNums[] = 'ERR_WHISPERNO__NOT_FOUND';
    }
    if (!isset($errorNums)){
        $sql_delete_comments = "DELETE FROM comment WHERE whisperNo = :whisperNo";
        $sql_delete_goodInfo = "DELETE FROM goodInfo WHERE whisperNo = :whisperNo";
        $sql_delete_whisper = "DELETE FROM whisper WHERE whisperNo = :whisperNo";
        
        try {
            $pdo->beginTransaction();

            $stmt = $pdo->prepare($sql_delete_comments);
            $stmt->bindParam('whisperNo', $data['whisperNo']);
            $stmt->execute();

            $stmt = $pdo->prepare($sql_delete_goodInfo);
            $stmt->bindParam('whisperNo', $data['whisperNo']);
            $stmt->execute();

            $stmt = $pdo->prepare($sql_delete_whisper);
            $stmt->bindParam('whisperNo', $data['whisperNo']);
            $stmt->execute();

            $pdo->commit();
            
            $response['result'] = "success";
        } catch (PDOException $e) {
            $pdo->rollBack();
            echo "Lỗi: " . $e->getMessage();
        }
    } else {
        $response = setError($response, $errorNums);
    }
    header('Content-Type: application/json');
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    require_once 'mysqlClose.php';
    disconnect_db($pdo);
}
?>
