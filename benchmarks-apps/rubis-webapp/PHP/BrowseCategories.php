<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<body>
<?php
$scriptName = "BrowseCategories.php";
include("PHPprinter.php");
require_once 'redis_connect.php';

$isRedisEnabled = true;

$startTime = getMicroTime();
$isRedisEnabled = true;
$region = NULL;
if (isset($_POST['region'])) {
    $region = $_POST['region'];
} else if (isset($_GET['region'])) {
    $region = $_GET['region'];
}
$username = NULL;
if (isset($_POST['nickname'])) {
    $username = $_POST['nickname'];
} else if (isset($_GET['nickname'])) {
    $username = $_GET['nickname'];
}
$password = NULL;
if (isset($_POST['password'])) {
    $password = $_POST['password'];
} else if (isset($_GET['password'])) {
    $password = $_GET['password'];
}

if ($isRedisEnabled) {
    global $redis;
    if ($redis->exists("categories")) {
        $data = json_decode($redis->get('categories'), true);
        print("<h3>Exist in Redis</h3><br>");

        getDatabaseLink($link);
        $userId = -1;
        if ((!is_null($username) && $username != "") || (!is_null($password) && $password != "")) { // Authenticate the user
            $userId = authenticate($username, $password, $link);
            if ($userId == -1) {
                printError($scriptName, $startTime, "Authentication", "You don't have an account on RUBiS!<br>You have to register first.<br>\n");
                exit();
            }
        }
        foreach($data as $row)
        {
            if (!is_null($region))
                print("<a href=\"/PHP/SearchItemsByRegion.php?category=" . $row["id"] . "&categoryName=" . urlencode($row["name"]) . "&region=$region\">" . $row["name"] . "</a><br>\n");
            else if ($userId != -1)
                print("<a href=\"/PHP/SellItemForm.php?category=" . $row["id"] . "&user=$userId\">" . $row["name"] . "</a><br>\n");
            else
                print("<a href=\"/PHP/SearchItemsByCategory.php?category=" . $row["id"] . "&categoryName=" . urlencode($row["name"]) . "\">" . $row["name"] . "</a><br>\n");
        }
    } else {
        getDatabaseLink($link);
        begin($link);
        $result = mysqli_query($link, "SELECT * FROM categories");
        if (!$result) {
            error_log("[" . __FILE__ . "] Query 'SELECT * FROM categories' failed: " . mysqli_error($link));
            die("ERROR: Query failed: " . mysqli_error($link));
        }
        commit($link);
        if (mysqli_num_rows($result) == 0)
            print("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
        else
            print("<h2>Currently available categories</h2><br>\n");
            print("<h3>Not in Redis</h3><br>");
        $rows = array();
        while ($row = mysqli_fetch_array($result)) {
            $rows[] = $row;
            if (!is_null($region))
                print("<a href=\"/PHP/SearchItemsByRegion.php?category=" . $row["id"] . "&categoryName=" . urlencode($row["name"]) . "&region=$region\">" . $row["name"] . "</a><br>\n");
            else if ($userId != -1)
                print("<a href=\"/PHP/SellItemForm.php?category=" . $row["id"] . "&user=$userId\">" . $row["name"] . "</a><br>\n");
            else
                print("<a href=\"/PHP/SearchItemsByCategory.php?category=" . $row["id"] . "&categoryName=" . urlencode($row["name"]) . "\">" . $row["name"] . "</a><br>\n");
        }
        mysqli_free_result($result);
        mysqli_close($link);
        $redis->set('categories', json_encode($rows));
        #error_log("Redis-Mysql===>" . json_encode($rows));
    }
} else {
    getDatabaseLink($link);
    $userId = -1;
    if ((!is_null($username) && $username != "") || (!is_null($password) && $password != "")) { // Authenticate the user
        $userId = authenticate($username, $password, $link);
        if ($userId == -1) {
            printError($scriptName, $startTime, "Authentication", "You don't have an account on RUBiS!<br>You have to register first.<br>\n");
            exit();
        }
    }

    printHTMLheader("RUBiS available categories");
    begin($link);
    $result = mysqli_query($link, "SELECT * FROM categories");
    if (!$result) {
        error_log("[" . __FILE__ . "] Query 'SELECT * FROM categories' failed: " . mysqli_error($link));
        die("ERROR: Query failed: " . mysqli_error($link));
    }
    commit($link);
    if (mysqli_num_rows($result) == 0)
        print("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
    else
        print("<h2>Currently available categories</h2><br>\n");
        print("<h3>Main DB</h3><br>");

    while ($row = mysqli_fetch_array($result)) {
        if (!is_null($region))
            print("<a href=\"/PHP/SearchItemsByRegion.php?category=" . $row["id"] . "&categoryName=" . urlencode($row["name"]) . "&region=$region\">" . $row["name"] . "</a><br>\n");
        else if ($userId != -1)
            print("<a href=\"/PHP/SellItemForm.php?category=" . $row["id"] . "&user=$userId\">" . $row["name"] . "</a><br>\n");
        else
            print("<a href=\"/PHP/SearchItemsByCategory.php?category=" . $row["id"] . "&categoryName=" . urlencode($row["name"]) . "\">" . $row["name"] . "</a><br>\n");
    }
    mysqli_free_result($result);
    mysqli_close($link);
}
printHTMLfooter($scriptName, $startTime);
?>
</body>
</html>
