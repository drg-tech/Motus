<?php
	include("include_db_conn.php");
	
	//Check to see if auth_token is included
	//check device token
	//if device token is validated then proceed to return client key
	$device_valid = false;
	$auth_token_valid = false;
	if(isset($_GET['auth_token']) and isset($_GET['device_token'])){
		//both criteria are set. Proceed with calls
		$device_type_query = "SELECT * FROM 'cura_device_types' WHERE cura_device_token='" . $_GET['device_token'] . "'";
		$result = mysqli_query($conn,$device_type_query);
			if(mysql_num_rows($result) > 0){
				$device_valid = true;
			}
			else{
				die("Invalid device call");
			}
		$auth_token_query = "SELECT 'client_access_key' FROM 'cura_clients' WHERE client_auth_token='" . $_GET['auth_token'] . "'";
		$result_auth = mysqli_query($conn,$auth_token_query);
			if(mysqli_num_rows($result_auth) > 0){
				$auth_token_valid = true;
			}
			else{
				die("Invalid authorization");
			}
		if($device_valid && $auth_token_valid){
			$data = ['access_key' => $result_auth['client_access_key']];
			header('Content-type: application/json');
			echo json_encode( $data );
		}
	}
	else{
		die("Invalid Request");
	}
?>