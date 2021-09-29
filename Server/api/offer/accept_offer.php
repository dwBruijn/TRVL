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

	$offer->id = $data->offer_id;

	// validate JWT
	$jwt = isset($data->jwt) ? $data->jwt : "";

	$inquiry->user_id = validate_user_jwt($jwt);
	if($inquiry->user_id < 0) {
		$database->close();
		errorExit("Failed", "You must login first.", 401);
	}

	// confirm that user_id and offer_id are associated to exactly one inquiry
	if(!$inquiry->user_owns_offer($offer->id)) {
		$database->close();
		errorExit("Failed", "Unauthorized.", 401);
	}

	// set offer to accepted
	if(!$offer->accept_offer()) {
		$database->close();
		errorExit("Failed", "Unknown error.", 401);
	}

	// get offer accepted status id
	$status_id = $inquiry_status->get_inquiry_status_id_by_status("offer accepted");
	if($status_id > 0) {
		// update status_id for inquiry
		if($inquiry->set_inquiry_status_id_by_id($status_id)) {
			http_response_code(200);
			echo json_encode(array("message" => "Offer Accepted", "error" => ""));
		}
	} else {
		$database->close();
		errorExit("Failed", "Something went wrong.", 401);
	}

	$database->close();
?>