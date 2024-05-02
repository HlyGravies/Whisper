<?php
//Quan
require_once 'mysqlConnect.php';
require_once 'errorMsgs.php';
require_once 'function.php';
$pdo = connect_db();



$response = [
    "result"  => "success",
    "errorDetails" => null
];
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $whisperData = json_decode(file_get_contents('php://input'), true); 
    
    $errorNums = validateWhisperData($pdo,$whisperData);
    if ($errorNums === null){
        $sql = "INSERT INTO whisper (userId, postDate, content, imagePath) VALUES (:userId, :postDate, :content, :imagePath)";
        try {
            $stmt = $pdo->prepare($sql);
            $stmt->bindParam(':userId', $whisperData['userId']);
            $stmt->bindParam(':postDate', $whisperData['postDate']);
            $stmt->bindParam(':content', $whisperData['content']);
            $stmt->bindParam(':imagePath', $whisperData['imagePath']);
            $stmt->execute();
        } catch (PDOException $e) {
            echo "Lỗi: " . $e->getMessage();
        }
    }else{
        $response = setError($response, $errorNums);
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>
