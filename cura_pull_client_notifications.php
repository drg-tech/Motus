<?php
	include("include_db_conn.php");
	
	//Check to see if access_key is included
	//check device token
	//if device token is validated then proceed to return client notifications
	
	if(isset($_GET['client_access_key'])){
		$client_notification_query = "SELECT * FROM 'cura_notifications' WHERE client_access_key='" . $_GET['client_access_key'] ."'";
		$result = mysqli_query($conn,$client_notification_query);
		if(mysql_num_rows($result) > 0){
			$data = array();
			while ($row = mysql_fetch_array($result)) 
			{
				$data[] = $row;	
			}
			header('Content-type: application/json');
			echo json_encode( $data );
		}
		else{
		die("No Notifications");
	}
	else{
		die("Invalid Request");
	}
?>