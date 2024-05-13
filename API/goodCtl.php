<?php
include ("mysqlConnect.php");
include ("mysqlClose.php");
include ("errorMsgs.php");
include ("function.php");
$pdo = connect_db();

$response = [
  "result" => "error",
  "errCode" => null,
  "errMsg" => null,
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  // Kiểm tra nếu request không chứa dữ liệu JSON
  $inputJSON = file_get_contents('php://input');
  $input = json_decode($inputJSON, true);

  $userID = "nguyenhoang887984@gmail.com";
  $whisperNo = "1";
  

  // Chuẩn bị câu lệnh SQL chèn dữ liệu
$sql = "INSERT INTO goodinfo (userId, whisperNo) VALUES ('$userID', '$whisperNo')";

// Thực thi câu lệnh SQL
if ($pdo->query($sql) === TRUE) {
    echo "New record created successfully";
} else {
    echo "Error: " . $sql . "<br>" . $pdo->error;
}

// Đóng kết nối
// $pdo->close();e

}
?>