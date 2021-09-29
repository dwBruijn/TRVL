<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Offer.php";
	include_once "../models/Inquiry.php";
	include_once "../config/core.php";
	include_once "../helpers/helpers.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	$offer = new Offer($db);
	// used to assign offer_id in corresponding inquiry
	$inquiry = new Inquiry($db);
	// used to update inquiry status
	$inquiry_status = new InquiryStatus($db);

	// read in POST data
	$data = json_decode(file_get_contents("php://input"));

	// validate JWT
	$jwt = isset($data->jwt) ? $data->jwt : "";

	$inquiry->user_id = validate_user_jwt($jwt);
	if($inquiry->user_id < 0) {
		$database->close();
		errorExit("Failed", "You must login first.", 401);
	}

	$offer->id = $data->offer_id;
	$offer->requested_changes = $data->requested_changes;

	if(empty($offer->id) || !check_is_numeric($offer->id)) {
		$database->close();
		errorExit("Failed", "Invalid offer id.", 401);
	}

	if(empty($offer->requested_changes)) {
		$database->close();
		errorExit("Failed", "No changes were requested.", 401);
	}

	// confirm that user_id and offer_id are associated to exactly one inquiry and sets the $inquiry->id to that inquiry id
	if(!$inquiry->user_owns_offer($offer->id)) {
		$database->close();
		errorExit("Failed", "Unauthorized.", 401);
	}

	// request changes in offer
	if(!$offer->request_changes()) {
		$database->close();
		errorExit("Failed", "Something went wrong.", 401);
	}

	// get awaiting updated offer status id
	$status_id = $inquiry_status->get_inquiry_status_id_by_status("awaiting updated offer");
	if($status_id > 0) {
		// update status_id for inquiry
		if($inquiry->set_inquiry_status_id_by_id($status_id)) {
			http_response_code(200);
			echo json_encode(array("message" => "Changes Requested", "error" => ""));
		}
	} else {
		errorExit("Failed", "Unknown error.", 401);
	}

	$database->close();
?>