<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "ViewBidHistory.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	$itemId = NULL;
	if (isset($_POST['itemId']))
	{
    	$itemId = $_POST['itemId'];
	}
	else if (isset($_GET['itemId']))
	{
    	$itemId = $_GET['itemId'];
	}
	else
	{
		printError($scriptName, $startTime, "Bid history", "You must provide an item identifier!<br>");
		exit();
	}

    getDatabaseLink($link);
    begin($link);

    // Get the item name
    $itemNameResult = mysqli_query($link,"SELECT name FROM items WHERE items.id=$itemId");
	if (!$itemNameResult)
	{
		error_log("[".__FILE__."] Query 'SELECT name FROM items WHERE items.id=$itemId' failed: " . mysqli_error($link));
		die("ERROR: Query failed for item '$itemId': " . mysqli_error($link));
	}
    if (mysqli_num_rows($itemNameResult) == 0)
	{
      $itemNameResult = mysqli_query($link,"SELECT name FROM old_items WHERE old_items.id=$itemId");
	  if (!$itemNameResult)
	  {
		error_log("[".__FILE__."] Query 'SELECT name FROM old_items WHERE old_items.id=$itemId' failed: " . mysqli_error($link));
		die("ERROR: Query failed: " . mysqli_error($link));
	  }
	}
    if (mysqli_num_rows($itemNameResult) == 0)
    {
      commit($link);
      die("<h3>ERROR: Sorry, but this item '$itemId' does not exist.</h3><br>\n");
    }
    $itemNameRow = mysqli_fetch_array($itemNameResult);
    $itemName = $itemNameRow["name"];


    // Get the list of bids for this item
    $bidsListResult = mysqli_query($link,"SELECT * FROM bids WHERE item_id=$itemId ORDER BY date DESC");
	if (!$bidsListResult)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM bids WHERE item_id=$itemId ORDER BY date DESC' failed: "  . mysqli_error($link));
		die("ERROR: Bids list query failed for item '$itemId': "  . mysqli_error($link));
	}
    if (mysqli_num_rows($bidsListResult) == 0)
      print ("<h2>There is no bid for $itemName. </h2><br>");
    else
      print ("<h2><center>Bid history for $itemName</center></h2><br>");

    printHTMLheader("RUBiS: Bid history for $itemName.");
    print("<TABLE border=\"1\" summary=\"List of bids\">\n".
                "<THEAD>\n".
                "<TR><TH>User ID<TH>Bid amount<TH>Date of bid\n".
                "<TBODY>\n");

    while ($bidsListRow = mysqli_fetch_array($bidsListResult))
    {
    	$bidAmount = $bidsListRow["bid"];
    	$bidDate = $bidsListRow["date"];
    	$userId = $bidsListRow["user_id"];
	// Get the bidder nickname
    	if ($userId != 0)
	{
	  $userNameResult = mysqli_query($link,"SELECT nickname FROM users WHERE id=$userId");
	  if (!$userNameResult)
	  {
		error_log("[".__FILE__."] Query 'SELECT nickname FROM users WHERE id=$userId' failed: " . mysqli_error($link));
		die("User nickname query failed: " . mysqli_error($link));
	  }
	  $userNameRow = mysqli_fetch_array($userNameResult);
	  $nickname = $userNameRow["nickname"];
	  mysqli_free_result($userNameResult);
   	}
    else
	{
	    print("Cannot lookup the user!<br>");
	    printHTMLfooter($scriptName, $startTime);
	    exit();
	}
    print("<TR><TD><a href=\"/PHP/ViewUserInfo.php?userId=".$userId."\">$nickname</a>"
		  ."<TD>".$bidAmount."<TD>".$bidDate."\n");
    }
    print("</TABLE>\n");

    commit($link);

    mysqli_free_result($bidsListResult);
    mysqli_free_result($itemNameResult);
    mysqli_close($link);

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
