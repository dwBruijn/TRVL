<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Inquiry.php";

	include_once "../config/core.php";
	include_once "../helpers/helpers.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init new inquiry object
	$inquiry = new Inquiry($db);

	// read in POST data
	$data = json_decode(file_get_contents("php://input"));

	// validate JWT
	$jwt = isset($data->jwt) ? $data->jwt : "";

	$inquiry->user_id = validate_user_jwt($jwt);
	if($inquiry->user_id < 0) {
		$database->close();
		errorExit("Failed", "You must login first.", 401);
	}

	$inquiry->id = $data->id;

	// check if the inquiry belongs to the user that is trying to delete it
	$user_inquiry_ids = Inquiry::get_inquiries_id_by_user_id($inquiry->user_id, $db);

	if(!in_array($inquiry->id, $user_inquiry_ids)) {
		$database->close();
		errorExit("Failed", "Unauthorized.", 401);
	}

	if(
		!empty($inquiry->id) &&
		$inquiry->delete()
	) {
		http_response_code(200);
		echo json_encode(array("message" => "Inquiry deleted", "error" => ""));
	} else {
		$database->close();
		errorExit("Failed", "Something went wrong.", 401);
	}

?>