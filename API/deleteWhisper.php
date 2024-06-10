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
        $sql = "DELETE goodInfo FROM goodInfo WHERE whisperNo = :whisperNo";
        $sql2 = "DELETE whisper FROM whisper WHERE whisperNo = :whisperNo";
        try {
            $stmt = $pdo->prepare($sql);
            $stmt->bindParam('whisperNo', $data['whisperNo']);
            $stmt->execute();

            $stmt2 = $pdo->prepare($sql2);
            $stmt2->bindParam('whisperNo', $data['whisperNo']);
            $stmt2->execute();

            $response['result'] = "success";
        } catch (PDOException $e) {
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
