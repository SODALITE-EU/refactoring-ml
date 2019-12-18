<?php 

$GLOBALS['conn'] = mysqli_connect("34.66.222.248","hameez","12345","rubis");
// Check connection
if (mysqli_connect_errno())
  {
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
  }

//Connecting to Redis server on localhost  
require "predis/autoload.php";
Predis\Autoloader::register();
$redis = new Predis\Client(array(
    "scheme" => "tcp",
    "host" => "35.202.160.242",
    "port" => 6379));
echo "Connected to Redis";
?>
