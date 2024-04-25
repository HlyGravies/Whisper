
<?php
//Quan
function connect_db() {
    $host = "localhost";
    $database = "2024shisukai";
    $username = "root";
    $password = "root";

    // Tạo chuỗi kết nối DSN
    $dsn = "mysql:host={$host};dbname={$database};charset=utf8mb4";
    $options = [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ];

    try {
        // Thực hiện kết nối PDO
        $pdo = new PDO($dsn, $username, $password, $options);
        return $pdo;
    } catch (PDOException $e) {
        // Xử lý ngoại lệ nếu có lỗi xảy ra
        throw new PDOException($e->getMessage(), (int)$e->getCode());
    }
}
?>
