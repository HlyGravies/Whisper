<?php
/*
    製作者：QUAN 
*/

function connect_db() {
    // $host = "localhost";
    // $database = "2024shisukai";
    // $username = "root";
    // $password = "root";

    $host = "localhost"; 
    $database = "whisper24_a";
    $username = "whisper24_a";
    $password = "D8bJnWSX";

    $dsn = "mysql:host={$host};dbname={$database};charset=utf8mb4";
    $options = [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ];

    try {
        $pdo = new PDO($dsn, $username, $password, $options);
        return $pdo;
    } catch (PDOException $e) {
        throw new PDOException($e->getMessage(), (int)$e->getCode());
    }
}
?>
