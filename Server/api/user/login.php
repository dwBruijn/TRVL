<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/User.php";

	// used to generate JSON web token
	include_once "../config/core.php";
	include_once "../helpers/helpers.php";
	

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init a user object
	$user = new User($db);

	$data = json_decode(file_get_contents("php://input"));

	$user->email = $data->email;
	// check if email is associated with an accout, if so fetch user id, firstname, lastname, and password hash
	$email_exists = $user->email_exists();

	// check if email exists and if password is correct
	// using the build-in password_verify function to compare hashes
	if($email_exists && password_verify($data->password, $user->password)){

		$jwt = create_jwt($user->id, $user->firstname, $user->lastname, $user->email);

		// get account name
		$account_name = $user->get_user_name_by_id($user->id);
	 
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