<?php
	include("include_db_conn.php");
	
	//Check to see if access_key is included and validate it
	//if its valid then update the notification to viewed
	
	if(isset($_GET['client_access_key']) and isset($_GET['notification_id'])){
		$notification_update_query = "UPDATE 'cura_notifications' SET client_notification_viewed=1 WHERE _id='" . $_GET['notification_id'] . "'";
		if ($conn->query($notification_update_query) === TRUE) {
		   $data = ["result" => "TRUE"];
		   header('Content-type: application/json');
		   echo json_encode( $data );
		} else {
		    echo "Error updating record: " . $conn->error;
		}
	}
	else{
		die("Invalid Request");
	}
	
?>