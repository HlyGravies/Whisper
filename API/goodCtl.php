<?php
include ("mysqlConnect.php");
include ("mysqlClose.php");
include ("errorMsgs.php");
include ("function.php");

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  // Kiểm tra nếu request không chứa dữ liệu JSON
  $inputJSON = file_get_contents('php://input');
  $input = json_decode($inputJSON, true);
}else{
  
}
?>