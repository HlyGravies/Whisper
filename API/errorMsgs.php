<?php

$msgList = array(
  "001" => "データベース処理が異常終了しました",
  "002" => "変更内容がありません",
  "003" => "ユーザIDまたはパスワードが違います",
  "004" => "対象データが見つかりませんでした",
  "005" => "ささやき内容がありません",
  "006" => "ユーザIDが入力されていません",
  "007" => "パスワードが入力されていません",
  "008" => "ささやき管理番号が入力されていません",
  "009" => "検索区分が入力されていません",
  "010" => "検索文字列が入力されていません",
  "011" => "ユーザ名が入力されていません",
  "012" => "フォロユーザIDが入力されていません",
  "013" => "フォローフラグが入力されていません",
  "014" => "イイねフラグが入力されていません",
  "015" => "ログインユーザIDが入力されていません",
  "ERR_USERID_TOOLONG"        => "USERIDを30文字以内で入力してください",
  "ERR_USERNAME_TOOLONG"      => "USERNAMEを30文字以内で入力してください",
  "ERR_PASSWORD_TOOLONG"      => "パスワードを64文字以内で入力してください",
  "ERR_PROFILE_TOOLONG"       => "プロフィールを200文字以内で入力してください",
  "ERR_CONTENT_TOOLONG"       => "ささやき内容を256文字以内で入力してください",
  "USERID_ALREADY_EXISTS"     => "このUSERIDは既に使われています",
  "ERR_EMPTY_FOLLOWUSERID"    => "フォロユーザIDが入力されていません",
  "ERR_FOLLOWUSERID_TOOLONG"  => "FOLLOWUSERIDを30文字以内で入力してください",
  "ERR_USERID_NOT_FOUND"                => "USERIDが見つかりません",
  "ERR_WHISPERNO__NOT_FOUND"        => "WHISPERNOが見つかりません",
  // "ERR_ICONPATH_TOOLONG" => "検索区分が不正です",
);

function setError($response, $errorNums){
  global $msgList;
  $response["result"] = "error";
  $errorMap = [];

  // Kiểm tra nếu $errorNums không phải là mảng hoặc đối tượng
  if (!is_array($errorNums)) {
    // Nếu không phải, tạo một mảng mới chỉ chứa phần tử $errorNums
    $errorNums = [$errorNums];
  }
  
  foreach ($errorNums as $errorNum) {
    $errorMap[$errorNum] = $msgList[$errorNum];
  }
  $response['errorDetails'] = $errorMap;
  return $response;
}

?>