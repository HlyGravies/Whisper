<?php
//Quan
include ("mysqlConnect.php");
include("mysqlClose.php");
include("errorMsgs.php");
include("function.php");
$pdo = connect_db();

$response = [
    "result"  => "error",
    "errCode" => null, 
    "errMsg"  => null,  
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $followData = json_decode(file_get_contents('php://input'), true);
    
    $errorNums = validateFollowData($pdo, $followData);
    if ($errorNums === null){
        if (isUserIdExist($pdo, $followData["userId"])){
            if($followData["followFlg"] == true){
                $sql = "INSERT INTO follow (userId, followUserId) VALUES (:userId, :followUserId)";
                try {
                    $stmt = $pdo->prepare($sql);
                    $stmt->bindParam(':userId', $followData['userId']);
                    $stmt->bindParam(':followUserId', $followData['followUserId']);
                    $stmt->execute();
                    $response['result'] = "success";
                } catch (PDOException $e) {
                    echo "Lỗi: " . $e->getMessage();
                }
            }elseif($followData["followFlg"] == false){
                $sql = "DELETE FROM follow WHERE userId = :userId";
                try {
                    $stmt = $pdo->prepare($sql);
                    $stmt->bindParam(':userId', $followData['userId']);
                    $stmt->execute();
                    $response['result'] = "success";
                } catch (PDOException $e) {
                    echo "Lỗi: " . $e->getMessage();
                }
            }
        } else {
            $response = setError($response, "006");
        }
    }else{
        $response = setError($response, $errorNums);
    }


    header('Content-Type: application/json');
    echo json_encode($response, JSON_UNESCAPED_UNICODE);

    require_once 'mysqlClose.php';
    disconnect_db($pdo);

}
?>