//min

<?php
require_once 'mysqlConnect.php';
require_once 'errorMsgs.php';
include ("database/database.php");
include ("validation/validation.php");
$pdo = connect_db();

$response = [
    "result" => "success",
    "errorDetails" => null
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);

    if (isset($postData['query'])) {
        $query = $postData['query'];
        $page = isset($postData['page']) ? (int)$postData['page'] : 1;
        $limit = 10;
        $offset = ($page - 1) * $limit;

        $stmt = $pdo->prepare('SELECT DISTINCT userName FROM user WHERE userName LIKE :query LIMIT :limit OFFSET :offset');
        $stmt->bindValue(':query', "%$query%", PDO::PARAM_STR);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $suggestions = $stmt->fetchAll(PDO::FETCH_COLUMN);

        if ($suggestions !== false) {
            $response['suggestions'] = $suggestions;
        } else {
            $response = setError($response, "004");
        }
    } else {
        $response = setError($response, "004");
    }
}

header('Content-Type: application/json');
echo json_encode($response, JSON_UNESCAPED_UNICODE);

require_once 'mysqlClose.php';
disconnect_db($pdo);
?>
