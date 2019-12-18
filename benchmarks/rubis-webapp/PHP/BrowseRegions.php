<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "BrowseRegions.php";
    require_once 'redis_connect.php';
    include("PHPprinter.php");
    $startTime = getMicroTime();
    $isRedisEnabled = true;
    printHTMLheader("RUBiS available regions");
    if ($isRedisEnabled) {
        global $redis;
        if ($redis->exists("regions")) {
            $data = json_decode($redis->get('regions'), true);
            print("<h3>Exist in Redis</h3><br>");
            foreach($data as $row)
            {
                print("<a href=\"/PHP/BrowseCategories.php?region=".$row["id"]."\">".$row["name"]."</a><br>\n");
            }
        } else {
            getDatabaseLink($link);
            begin($link);

            $result = mysqli_query($link, "SELECT * FROM regions");
            if (!$result)
            {
                error_log("[".__FILE__."] Query 'SELECT * FROM regions' failed: " . mysqli_error($link));
                die("ERROR: Query failed: " . mysqli_error($link));
            }
            commit($link);
            if (mysqli_num_rows($result) == 0)
                print("<h2>Sorry, but there is no region available at this time. Database table is empty</h2><br>");
            else
                print("<h2>Currently available regions</h2><br>");
                print("<h3>Not Redis</h3><br>");

            $rows_redis = array();
            while ($row = mysqli_fetch_array($result))
            {
                $rows_redis[] = $row;

                print("<a href=\"/PHP/BrowseCategories.php?region=".$row["id"]."\">".$row["name"]."</a><br>\n");
            }
            mysqli_free_result($result);
            mysqli_close($link);
            $redis->set('regions', json_encode($rows_redis));
          //  error_log("Redis-Mysql===>" . json_encode($rows_redis));
        }
    } else {
        getDatabaseLink($link);
        begin($link);

        $result = mysqli_query($link, "SELECT * FROM regions");
        if (!$result)
        {
            error_log("[".__FILE__."] Query 'SELECT * FROM regions' failed: " . mysqli_error($link));
            die("ERROR: Query failed: " . mysqli_error($link));
        }
        commit($link);
        if (mysqli_num_rows($result) == 0)
            print("<h2>Sorry, but there is no region available at this time. Database table is empty</h2><br>");
        else
            print("<h2>Currently available regions</h2><br>");
            print("<h3>Main DB</h3><br>");
        while ($row = mysqli_fetch_array($result))
        {
            print("<a href=\"/PHP/BrowseCategories.php?region=".$row["id"]."\">".$row["name"]."</a><br>\n");
        }
        mysqli_free_result($result);
        mysqli_close($link);
    }
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
