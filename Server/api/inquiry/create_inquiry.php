<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Inquiry.php";
	include_once "../models/Agent.php";
	include_once "../models/Country.php";
	include_once "../models/TripType.php";
	include_once "../models/InquiryStatus.php";

	include_once "../config/core.php";
	include_once "../helpers/helpers.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init new inquiry object
	$inquiry = new Inquiry($db);
	// init a country object associated to this inquiry
	$country = new Country($db);
	// init a triptype object associated to this inquiry
	$trip_type = new TripType($db);
	// init an inquirystatus object associated to this inquiry
	$inquiry_status = new InquiryStatus($db);

	// read inquiry POST data
	$data = json_decode(file_get_contents("php://input"));

	// validate JWT
	$jwt = isset($data->jwt) ? $data->jwt : "";

	$inquiry->user_id = validate_user_jwt($jwt);
	if($inquiry->user_id < 0) {
		$database->close();
		errorExit("Failed", "You must login first", 401);
	}

	// read and check POSTed data
	$inquiry->travel_date = $data->travel_date;
	// check travel date format
	if(!check_date_format($inquiry->travel_date)) {
		$database->close();
		errorExit("Failed", "Invalid travel date format.", 401);
	}

	// check if date is in the past
	if(check_date_in_past($inquiry->travel_date)) {
		$database->close();
		errorExit("Failed", "Invalid travel date.", 401);
	}

	$inquiry->trip_length = $data->trip_length;
	if(!check_is_numeric($inquiry->trip_length)) {
		$database->close();
		errorExit("Failed", "Invalid trip length format.", 401);
	}

	$inquiry->number_of_persons = $data->number_of_persons;
	if(!check_is_numeric($inquiry->number_of_persons)) {
		$database->close();
		errorExit("Failed", "Invalid number of persons.", 401);
	}

	$inquiry->level_of_accommodation = $data->level_of_accommodation;
	if(!check_is_numeric($inquiry->level_of_accommodation)) {
		$database->close();
		errorExit("Failed", "Invalid level of accommodation.", 401);
	}

	$inquiry->number_of_hotel_rooms = $data->number_of_hotel_rooms;
	if(!check_is_numeric($inquiry->number_of_hotel_rooms)) {
		$database->close();
		errorExit("Failed", "Invalid number of hotel rooms.", 401);
	}

	$inquiry->requested_activities = $data->requested_activities;
	$inquiry->budget_per_person = $data->budget_per_person;
	if(!check_is_numeric($inquiry->budget_per_person)) {
		$database->close();
		errorExit("Failed", "Invalid budget per person.", 401);
	}

	$inquiry->budget_flexibility = $data->budget_flexibility;
	$inquiry->specific_requests = $data->specific_requests;

	if(isset($data->serv_activities) && !empty($data->serv_activities)) {
		$inquiry->serv_activities = 1;
	} else {
		$inquiry->serv_activities = 0;
	}
	if(isset($data->serv_interflights) && !empty($data->serv_interflights)) {
		$inquiry->serv_interflights = 1;
	} else {
		$inquiry->serv_interflights = 0;
	}
	if(isset($data->serv_incountry_transportation) && !empty($data->serv_incountry_transportation)) {
		$inquiry->serv_incountry_transportation = 1;
	} else {
		$inquiry->serv_incountry_transportation = 0;
	}
	if(isset($data->serv_tour_guide) && !empty($data->serv_tour_guide)) {
		$inquiry->serv_tour_guide = 1;
	} else {
		$inquiry->serv_tour_guide = 0;
	}
	if(isset($data->budget_inter_flights) && !empty($data->budget_inter_flights)) {
		$inquiry->budget_inter_flights = 1;
	} else {
		$inquiry->budget_inter_flights = 0;
	}


	// fetch country_id using country name
	$inquiry->country_id = $country->get_country_id_by_name($data->country);
	if($inquiry->country_id < 1) {
		$database->close();
		errorExit("Failed", "Country not supported.", 401);
	}

	// fetch type_id based on the type chosen by the user
	$inquiry->type_id = $trip_type->get_trip_type_id_by_type($data->trip_type);
	if($inquiry->type_id < 1) {
		$database->close();
		errorExit("Failed", "Invalid trip type.", 401);
	}
	// assign status_id since this inquiry is just being created now
	$inquiry->status_id = $inquiry_status->get_inquiry_status_id_by_status("awaiting Offer");


	// assign new inquiry to an agent
	$inquiry->agent_id = Agent::get_assigned_agent_id($db);

	if(
		$inquiry->country_id > 0 &&
		$inquiry->user_id > 0 &&
		$inquiry->agent_id > 0 &&
		$inquiry->type_id > 0 &&
		$inquiry->status_id > 0 &&
		!empty($inquiry->travel_date) &&
		!empty($inquiry->trip_length) &&
		!empty($inquiry->number_of_persons) &&
		!empty($inquiry->level_of_accommodation) &&
		!empty($inquiry->number_of_hotel_rooms) &&
		isset($inquiry->serv_activities) &&
		isset($inquiry->serv_interflights) &&
		isset($inquiry->serv_incountry_transportation) &&
		isset($inquiry->serv_tour_guide) &&
		!empty($inquiry->requested_activities) &&
		!empty($inquiry->budget_per_person) &&
		isset($inquiry->budget_inter_flights) &&
		!empty($inquiry->budget_flexibility) &&
		!empty($inquiry->specific_requests) &&
		$inquiry->create()
	) {
		http_response_code(200);
		echo json_encode(array("message" => "Inquiry created", "error" => ""));
	} else {
		$database->close();
		errorExit("Failed", "Something went wrong.", 401);
	}

	$database->close();
?>