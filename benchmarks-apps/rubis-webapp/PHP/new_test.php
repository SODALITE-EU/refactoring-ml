<?php
// use function GuzzleHttp\json_encode;
require_once 'redis_connect.php';
require_once 'db.php';?>


<?php

$value = $redis->get("users");
var_dump($value);
?>
