<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Agent.php";
	include_once "../models/Country.php";
	include_once "../models/Inquiry.php";
	include_once "../config/core.php";
	include_once "../helpers/helpers.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init a agent object
	$agent = new Agent($db);

	// array to store data of the inquiries
	$inquiries_data = array();

	// read POSTed JWT
	$data = json_decode(file_get_contents("php://input"));

	// validate JWT
	$jwt=isset($data->jwt) ? $data->jwt : "";
	
	$agent->id = validate_agent_jwt($jwt);

	if($agent->id < 0) {
		$database->close();
		errorExit("You must login first.", "Invalid JWT", 401);
	}

	// array of inquiry ids assocaited to this user
	$inquiry_ids = Inquiry::get_inquiries_id_by_agent_id($agent->id, $db);

	foreach($inquiry_ids as $inquiry_id) {
		// add returned inquiry data array to the inquiries data array with key inquiry_id
		$inquiries_data["$inquiry_id"] = Inquiry::get_agent_inquiry_details_by_id($inquiry_id, $db);
	}

	http_response_code(200);
	echo json_encode($inquiries_data);

	$database->close();

?>