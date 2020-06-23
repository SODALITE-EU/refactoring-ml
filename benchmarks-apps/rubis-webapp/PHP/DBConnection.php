<?php
$link = mysqli_connect("104.198.77.11","hameez","12345","rubis");
if (!$link)
{
error_log("[".__FILE__."] Could not connect to database: " . mysql_error());
die("ERROR: Could not connect to database: " . mysql_error());
}
mysqli_select_db($link,"test");
if (!$link)
{
error_log("[".__FILE__."] Couldn't select RUBiS database: " . mysql_error($link));
die("ERROR: Couldn't select RUBiS database: " . mysql_error($link));
}
?>
