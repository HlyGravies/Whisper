<?php
// Kết nối đến cơ sở dữ liệu MySQL
$host = 'localhost';
$username = 'your_mysql_username';
$password = 'your_mysql_password';
$database = 'your_database_name';

$conn = new mysqli($host, $username, $password, $database);

// Kiểm tra kết nối
if ($conn->connect_error) {
    die("Kết nối đến cơ sở dữ liệu thất bại: " . $conn->connect_error);
}

// Lấy dữ liệu từ yêu cầu POST
$userID = $_POST['username'];
$password = $_POST['password'];

// Truy vấn kiểm tra người dùng tồn tại
$sql = "SELECT * FROM users WHERE username='$userID' AND password='$password'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // Người dùng tồn tại, trả về kết quả thành công
    $response = array("message" => "Đăng nhập thành công");
    echo json_encode($response);
} else {
    // Người dùng không tồn tại, trả về mã lỗi 401
    http_response_code(401);
    $response = array("message" => "Tên người dùng hoặc mật khẩu không chính xác");
    echo json_encode($response);
}

// Đóng kết nối đến cơ sở dữ liệu
$conn->close();
?>
