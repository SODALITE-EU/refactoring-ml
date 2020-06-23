<?php
require_once "../../php/pear/Predis/Autoloader.php";
$GLOBALS['conn'] = mysqli_connect("35.222.120.86","hameez","12345","rubis");
// Check connection
if (mysqli_connect_errno())
  {
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
  }
  //Connecting to Redis server on localhost
// $redis = new Redis();
// $redis->connect('127.0.0.1', 6379);
// echo "" .$redis->ping();
//
// echo "Connection to server sucessfully";

// Predis\Autoloader::register();
//
// $redis = new Predis\Client(array(
//     "scheme" => "tcp",
//     "host" => "35.223.183.135",
//     "port" => 6379));
// echo "Connected to Redis";
// echo "" .$redis->ping();
//
// echo "Connection to server sucessfully";
?>
