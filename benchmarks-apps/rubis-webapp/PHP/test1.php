<?php
require_once "../../php/pear/Predis/Autoloader.php";
Predis\Autoloader::register();

$redis = new Predis\Client(array(
    "scheme" => "tcp",
    "host" => "35.222.201.236",
    "port" => 6379));
echo "Connected to Redis";
// $redis->set('foo', 'cowabunga');
// $response = $redis->get('foo');
// echo $response;
?>
