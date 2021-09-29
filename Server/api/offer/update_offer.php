<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Offer.php";
	include_once "../models/FlightType.php";
	include_once "../models/Inquiry.php";
	include_once "../models/InquiryStatus.php";
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
	// used to get flight_type id
	$flight_type = new FlightType($db);


	// read in POST data
	$data = json_decode(file_get_contents("php://input"));

	// validate JWT
	$jwt = isset($data->jwt) ? $data->jwt : "";
	
	$inquiry->agent_id = validate_agent_jwt($jwt);

	if($inquiry->agent_id < 0) {
		$database->close();
		errorExit("Failed", "You must login first", 400);
	}

	// get inquiry id from POSTed JSON
	$inquiry->id = $data->inquiry_id;

	$offer->flight_accommodation = $data->flight_accommodation;

	$offer->ticket_price = $data->ticket_price;
	if(!check_is_numeric($offer->ticket_price)) {
		$database->close();
		errorExit("Failed", "Invalid ticket price.", 400);
	}

	$offer->ground_transp_price_per_person = $data->ground_transp_price_per_person;
	if(!check_is_numeric($offer->ground_transp_price_per_person)) {
		$database->close();
		errorExit("Failed", "Invalid ground transportation price.", 400);
	}

	$offer->hotel_name = $data->hotel_name;
	$offer->hotel_address = $data->hotel_address;

	$offer->number_of_rooms = $data->number_of_rooms;
	if(!check_is_numeric($offer->number_of_rooms)) {
		$database->close();
		errorExit("Failed", "Invalid number of rooms.", 400);
	}

	$offer->room_price_per_night = $data->room_price_per_night;
	if(!check_is_numeric($offer->room_price_per_night)) {
		$database->close();
		errorExit("Failed", "Invalid room price.", 400);
	}

	$offer->tour_guide = $data->tour_guide;
	if(!check_is_numeric($offer->tour_guide)) {
		$database->close();
		errorExit("Failed", "Invalid tour guide value.", 400);
	}

	$offer->price_tour_guide = $data->price_tour_guide;
	if(!check_is_numeric($offer->price_tour_guide)) {
		$database->close();
		errorExit("Failed", "Invalid tour guide prie.", 400);
	}

	$offer->activities_details = $data->activities_details;

	$offer->activities_price_per_person = $data->activities_price_per_person;
	if(!check_is_numeric($offer->activities_price_per_person)) {
		$database->close();
		errorExit("Failed", "Invalid activities price.", 400);
	}

	$offer->suggestions = $data->suggestions;


	
	// get flight_type id by type
	$offer->flight_type = $flight_type->get_flightype_id_by_type($data->flight_type);
	if($offer->flight_type < 0) {
		$database->close();
		errorExit("Failed", "Unknown flight type.", 400);
	}

	// if tour guide is selected there must be a tour guide price
	if(!empty($offer->tour_guide)) {
		if(empty($offer->price_tour_guide) || $offer->price_tour_guide < 0) {
			$database->close();
			errorExit("Failed", "Invalid tour guide price.", 400);
		}
	} else {
		$offer->price_tour_guide = 0;
	}

	if(
		!empty($offer->flight_type) &&
		!empty($offer->flight_accommodation) &&
		!empty($offer->ticket_price) &&
		!empty($offer->hotel_name) &&
		!empty($offer->hotel_address) &&
		!empty($offer->number_of_rooms) &&
		!empty($offer->room_price_per_night) &&
		!empty($offer->activities_details) &&
		!empty($offer->activities_price_per_person) &&
		!empty($offer->suggestions)
	) {
		$offer->id = $inquiry->get_offer_id_by_inquiry_id();
		if($offer->id <= 0) {
			$database->close();
			errorExit("Failed", "Offer does not exist.", 400);
		}

		// check if the agent-inquiry-offer relationship holds
		if(!$inquiry->agent_owns_offer($offer->id)) {
			$database->close();
			errorExit("Failed", "Unauthorized.", 400);
		}
		
		if($offer->update()) {
			// update inquiry status
			$status_id = $inquiry_status->get_inquiry_status_id_by_status("offer updated");
			if($status_id > 0) {
				if($inquiry->set_inquiry_status_id_by_id($status_id)) {
					http_response_code(200);
					echo json_encode(array("message" => "Offer updated", "error" => ""));
					exit;
				}
			}
		}
	}

	$database->close();
	errorExit("Failed", "Something went wrong.", 400);
?>