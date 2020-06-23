<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "PutBid.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	$nickname = NULL;
	if (isset($_POST['nickname']))
	{
    	$nickname = $_POST['nickname'];
	}
	else if (isset($_GET['nickname']))
	{
    	$nickname = $_GET['nickname'];
	}
	else
	{
		printError($scriptName, $startTime, "PutBid", "You must provide a nick name!<br>");
		exit();
	}
	$password = NULL;
	if (isset($_POST['password']))
	{
    	$password = $_POST['password'];
	}
	else if (isset($_GET['password']))
	{
    	$password = $_GET['password'];
	}
	else
	{
		printError($scriptName, $startTime, "PutBid", "You must provide a password!<br>");
		exit();
	}
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
		printError($scriptName, $startTime, "PutBid", "You must provide an item identifier!<br>");
		exit();
	}

    getDatabaseLink($link);

    commit($link);
    // Authenticate the user
    $userId = authenticate($nickname, $password, $link);
    if ($userId == -1)
    {
      printError($scriptName, $startTime, "Authentication", "You don't have an account on RUBiS!<br>You have to register first.<br>\n");
      commit($link);
      exit();
    }

    $result = mysqli_query($link, "SELECT * FROM items WHERE items.id=$itemId");
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM items WHERE items.id=$itemId' failed: " . mysqli_error($link));
		die("ERROR: Query failed for item '$itemId': " . mysqli_error($link));
	}
    if (mysqli_num_rows($result) == 0)
    {
      printError($scriptName, $startTime, "PutBid", "<h3>Sorry, but this item does not exist.</h3><br>");
      commit($link);
      exit();
    }

	$buyNow = 0;
    $row = mysqli_fetch_array($result);
    $maxBidResult = mysqli_query($link, "SELECT MAX(bid) AS bid FROM bids WHERE item_id=".$row["id"]);
	if (!$maxBidResult)
	{
		error_log("[".__FILE__."] Query failed 'SELECT MAX(bid) AS bid FROM bids WHERE item_id=".$row["id"]."': " . mysqli_error($link));
		die("ERROR: Max bid query failed for item '".$row["id"]."': " . mysqli_error($link));
	}
    $maxBidRow = mysqli_fetch_array($maxBidResult);
    $maxBid = $maxBidRow["bid"];
    if ($maxBid == 0)
    {
      $maxBid = $row["initial_price"];
      $buyNow = $row["buy_now"];
      $firstBid = "none";
      $nbOfBids = 0;
    }
    else
    {
      if ($row["quantity"] > 1)
      {
        $xRes = mysqli_query($link, "SELECT bid,qty FROM bids WHERE item_id=".$row["id"]." ORDER BY bid DESC LIMIT ".$row["quantity"]);
		if (!$xRes)
		{
			error_log("[".__FILE__."] Query failed 'SELECT bid,qty FROM bids WHERE item_id=".$row["id"]." ORDER BY bid DESC LIMIT ".$row["quantity"],"': " . mysqli_error($link));
			die("ERROR: Quantity query failed for item '".$row["id"]."' and quantity '".$row["quantity"]."': " . mysqli_error($link));
		}
        $nb = 0;
        while ($xRow = mysqli_fetch_array($xRes))
        {
          $nb = $nb + $xRow["qty"];
          if ($nb > $row["quantity"])
          {
            $maxBid = $xRow["bid"];
            break;
          }
        }
      }
      $firstBid = $maxBid;
      $nbOfBidsResult = mysqli_query($link, "SELECT COUNT(*) AS bid FROM bids WHERE item_id=".$row["id"]);
	  if (!$nbOfBidsResult)
	  {
		error_log("[".__FILE__."] Query failed 'SELECT COUNT(*) AS bid FROM bids WHERE item_id=".$row["id"]."': " . mysqli_error($link));
		die("ERROR: Nb of bids query failed for item '".$row["id"]."': " . mysqli_error($link));
	  }
      $nbOfBidsRow = mysqli_fetch_array($nbOfBidsResult);
      $nbOfBids = $nbOfBidsRow["bid"];
      mysqli_free_result($nbOfBidsResult);
    }

    printHTMLheader("RUBiS: Bidding");
    printHTMLHighlighted("You are ready to bid on: ".$row["name"]);
    print("<TABLE>\n".
          "<TR><TD>Currently<TD><b><BIG>$maxBid</BIG></b>\n");

    // Check if the reservePrice has been met (if any)
    $reservePrice = $row["reserve_price"];
    if ($reservePrice > 0)
    {
	if ($maxBid >= $reservePrice)
	{
	  print("(The reserve price has been met)\n");
	}
	else
	{
          print("(The reserve price has NOT been met)\n");
	}
    }

    $sellerNameResult = mysqli_query($link, "SELECT users.nickname FROM users WHERE id=".$row["seller"]);
	if (!$sellerNameResult)
	{
		error_log("[".__FILE__."] Query failed 'SELECT users.nickname FROM users WHERE id=".$row["seller"]."': "  . mysqli_error($link));
		die("ERROR: Seller name query failed for seller '".$row["seller"]."': "  . mysqli_error($link));
	}
    $sellerNameRow = mysqli_fetch_array($sellerNameResult);
    $sellerName = $sellerNameRow["nickname"];
    mysqli_free_result($sellerNameResult);

    print("<TR><TD>Quantity<TD><b><BIG>".$row["quantity"]."</BIG></b>\n");
    print("<TR><TD>First bid<TD><b><BIG>$firstBid</BIG></b>\n");
    print("<TR><TD># of bids<TD><b><BIG>$nbOfBids</BIG></b> (<a href=\"/PHP/ViewBidHistory.php?itemId=".$row["id"]."\">bid history</a>)\n");
    print("<TR><TD>Seller<TD><a href=\"/PHP/ViewUserInfo.php?userId=".$row["seller"]."\">$sellerName</a> (<a href=\"/PHP/PutCommentAuth.php?to=".$row["seller"]."&itemId=".$row["id"]."\">Leave a comment on this user</a>)\n");
    print("<TR><TD>Started<TD>".$row["start_date"]."\n");
    print("<TR><TD>Ends<TD>".$row["end_date"]."\n");
    print("</TABLE>\n");

    // Can the user by this item now ?
    if ($buyNow > 0)
	print("<p><a href=\"/PHP/BuyNowAuth.php?itemId=".$row["id"]."\">".
              "<IMG SRC=\"/PHP/buy_it_now.jpg\" height=22 width=150></a>".
              "  <BIG><b>You can buy this item right now for only \$$buyNow</b></BIG><br><p>\n");

    printHTMLHighlighted("Item description");
    print($row["description"]);
    print("<br><p>\n");

    printHTMLHighlighted("Bidding");
    $minBid = $maxBid+1;
    print("<form action=\"/PHP/StoreBid.php\" method=POST>\n".
          "<input type=hidden name=minBid value=$minBid>\n".
          "<input type=hidden name=userId value=$userId>\n".
          "<input type=hidden name=itemId value=".$row["id"].">\n".
          "<input type=hidden name=maxQty value=".$row["quantity"].">\n".
          "<center><table>\n".
          "<tr><td>Your bid (minimum bid is $minBid):</td>\n".
          "<td><input type=text size=10 name=bid></td></tr>\n".
          "<tr><td>Your maximum bid:</td>\n".
          "<td><input type=text size=10 name=maxBid></td></tr>\n");
    if ($row["quantity"] > 1)
      print("<tr><td>Quantity:</td><td><input type=text size=5 name=qty></td></tr>\n");
    else
      print("<input type=hidden name=qty value=1>\n");
    print("</table><p><input type=submit value=\"Bid now!\"></center><p>\n");

    commit($link);
    mysqli_free_result($maxBidResult);
    mysqli_free_result($result);
    mysqli_close($link);

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
