<?php
require_once 'mysqlConnect.php';

$pdo = connect_db();

$response = [
    "result"  => "success",
    "errorDetails" => null
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $likeData = json_decode(file_get_contents('php://input'), true);
    $sql = "UPDATE comment SET likes = likes + :like WHERE commentId = :commentId";
    
    try {
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':like', $likeData['like']);
        $stmt->bindParam(':commentId', $likeData['commentId']);
        $stmt->execute();
        
        $response['result'] = "success";
    } catch (PDOException $e) {
        $response['result'] = "error";
        $response['errorDetails'] = $e->getMessage();
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

disconnect_db($pdo);
?>
