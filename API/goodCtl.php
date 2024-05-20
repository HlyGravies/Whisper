<?php
include ("mysqlConnect.php");
include("mysqlClose.php");
include("errorMsgs.php");
include("database/database.php");
include("validation/validation.php");
$pdo = connect_db();

$response = [
  "result" => "error",
  "errCode" => null,
  "errMsg" => null,
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  $goodData = json_decode(file_get_contents('php://input'), true);
  
  $errorNums = validateGoodCtl($pdo, $goodData);
  if ($errorNums === null){
      if (isUserIdExist($pdo, $goodData["userId"]) != false){
        if(!empty($goodData['whisperNo'])){
          if($goodData['goodFlg'] == true){
            $sql = "INSERT INTO goodInfo (userId, whisperNo) VALUES (:userId, :whisperNo)";
              try {
                  $stmt = $pdo->prepare($sql);
                  $stmt->bindParam(':userId', $goodData['userId']);
                  $stmt->bindParam(':whisperNo', $goodData['whisperNo']);
                  $stmt->execute();
                  $response['result'] = "success";
              } catch (PDOException $e) {
                  echo "Lỗi: " . $e->getMessage();
              }
          }elseif($goodData['goodFlg'] == false){
              $sql = "DELETE FROM goodInfo WHERE userId = :userId AND whisperNo = :whisperNo";
              try {
                  $stmt = $pdo->prepare($sql);
                  $stmt->bindParam(':userId', $goodData['userId']);
                  $stmt->bindParam(':whisperNo', $goodData['whisperNo']);
                  $stmt->execute();
                  
              } catch (PDOException $e) {
                  echo "Lỗi: " . $e->getMessage();
              }
          }else{
            $response = setError($response, "014");
          }
          $response['result'] = "success";
        }else{
          $response = setError($response, "008");
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