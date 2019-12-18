<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "ViewUserInfo.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	$userId = NULL;
	if (isset($_POST['userId']))
	{
    	$userId = $_POST['userId'];
	}
	else if (isset($_GET['userId']))
	{
    	$userId = $_GET['userId'];
	}
	else
	{
		printError($scriptName, $startTime, "Viewing user information", "You must provide an item identifier!<br>");
		exit();
	}

    getDatabaseLink($link);
    begin($link);
    $userResult = mysqli_query($link, "SELECT * FROM users WHERE users.id=$userId");
	if (!$userResult)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM users WHERE users.id=$userId' failed: " + mysqli_error($link));
		die("ERROR: Query failed for user '$userId': " + mysqli_error($link));
	}
    if (mysqli_num_rows($userResult) == 0)
    {
      commit($link);
      die("<h3>ERROR: Sorry, but this user '$userId' does not exist.</h3><br>\n");
    }

    printHTMLheader("RUBiS: View user information");

      // Get general information about the user
    $userRow = mysqli_fetch_array($userResult);
    $firstname = $userRow["firstname"];
    $lastname = $userRow["lastname"];
    $nickname = $userRow["nickname"];
    $email = $userRow["email"];
    $creationDate = $userRow["creation_date"];
    $rating = $userRow["rating"];

    print("<h2>Information about ".$nickname."<br></h2>");
    print("Real life name : ".$firstname." ".$lastname."<br>");
    print("Email address  : ".$email."<br>");
    print("User since     : ".$creationDate."<br>");
    print("Current rating : <b>".$rating."</b><br>");

      // Get the comments about the user
    $commentsResult = mysqli_query($link, "SELECT * FROM comments WHERE comments.to_user_id=$userId");
	if (!$commentsResult)
	{
		error_log("[".__FILE__."] Query failed 'SELECT * FROM comments WHERE comments.to_user_id=$userId': " . mysqli_error($link));
		die("ERROR: Query failed for the list of comments: " . mysqli_error($link));
	}
    if (mysqli_num_rows($commentsResult) == 0)
      print("<h2>There is no comment for this user.</h2><br>\n");
    else
    {
	print("<DL>\n");
	while ($commentsRow = mysqli_fetch_array($commentsResult))
	{
	    $authorId = $commentsRow["from_user_id"];
	    $authorResult = mysqli_query($link, "SELECT nickname FROM users WHERE users.id=$authorId");
		if (!$authorResult)
		{
			error_log("[".__FILE__."] Query failed 'SELECT nickname FROM users WHERE users.id=$authorId': " . mysqli_error($link));
			die("ERROR: Query failed for the comment author '$authorId': " . mysqli_error($link));
		}
	    if (mysqli_num_rows($authorResult) == 0)
		{
			die("ERROR: This author '$authorId' does not exist.<br>\n");
		}
	    else
	    {
		$authorRow = mysqli_fetch_array($authorResult);
		$authorName = $authorRow["nickname"];
	    }
	    $date = $commentsRow["date"];
	    $comment = $commentsRow["comment"];
	    print("<DT><b><BIG><a href=\"/PHP/ViewUserInfo.php?userId=".$authorId."\">$authorName</a></BIG></b>"." wrote the ".$date."<DD><i>".$comment."</i><p>\n");
	    mysqli_free_result($authorResult);
	}
	print("</DL>\n");

    }
    commit($link);
    mysqli_free_result($userResult);
    mysqli_free_result($commentsResult);
    mysqli_close($link);

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
