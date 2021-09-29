<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Agent.php";
	// used to generate JSON web token
	include_once "../config/core.php";
	include_once "../helpers/helpers.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init an agnet object
	$agent = new Agent($db);

	$data = json_decode(file_get_contents("php://input"));

	$agent->email = $data->email;

	// check if email is associated with an accout, if so fetch agent id, firstname, lastname, and password hash
	$email_exists = $agent->email_exists();

	// check if email exists and if password is correct
	// using the build-in password_verify function to compare hashes
	if($email_exists && password_verify($data->password, $agent->password)){
	 
		$jwt = create_jwt($agent->id, $agent->firstname, $agent->lastname, $agent->email);

		// get account name in order to send it to the browser
		$account_name = $agent->get_agent_name_by_id($agent->id);
	 
		// set response code
		http_response_code(200);
		echo json_encode(
			array(
				"message" => "Login successful",
				"jwt" => $jwt,
				"account_name" => $account_name
			)
		);
	 
	} else {
		errorExit("Login Failed", "", 401);
	}

	$database->close();
?>