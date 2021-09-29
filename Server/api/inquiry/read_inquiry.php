<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: GET'); // allow only GET requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Inquiry.php";
	include_once "../models/Agent.php";
	include_once "../models/Country.php";
	include_once "../models/TripType.php";
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

	// get inquiry id from GET request
	$inquiry->id = isset($_GET['id']) ? $_GET['id'] : die();

	// get inquiry details
	if(!$inquiry->read()) {
		$database->close();
		errorExit("Failed", "Cannot read inquiry.", 400);
	}

	$country = $country->get_country_name_by_id($inquiry->country_id);
	$type = $trip_type->get_trip_type_by_id($inquiry->type_id);

	if($country == "" || $type == "") {
		$database->close();
		errorExit("Failed", "Unknown error.", 400);
	}

	if($inquiry->serv_activities) {
		$serv_activities = "Yes";
	} else {
		$serv_activities = "No";
	}
	if($inquiry->serv_interflights) {
		$serv_interflights = "Yes";
	} else {
		$serv_interflights = "No";
	}
	if($inquiry->serv_incountry_transportation) {
		$serv_incountry_transportation = "Yes";
	} else {
		$serv_incountry_transportation = "No";
	}
	if($inquiry->serv_tour_guide) {
		$serv_tour_guide = "Yes";
	} else {
		$serv_tour_guide = "No";
	}
	if($inquiry->budget_inter_flights) {
		$budget_inter_flights = "Yes";
	} else {
		$budget_inter_flights = "No";
	}

	$inquiry_data = array(
		"country" => $country,
		"travel_date" => $inquiry->travel_date,
		"trip_length" => $inquiry->trip_length,
		"number_of_persons" => $inquiry->number_of_persons,
		"level_of_accommodation" => $inquiry->level_of_accommodation,
		"type" => $type,
		"number_of_hotel_rooms" => $inquiry->number_of_hotel_rooms,
		"serv_activities" => $serv_activities,
		"serv_interflights" => $serv_interflights,
		"serv_incountry_transportation" => $serv_incountry_transportation,
		"serv_tour_guide" => $serv_tour_guide,
		"requested_activities" => $inquiry->requested_activities,
		"budget_per_person" => $inquiry->budget_per_person,
		"budget_inter_flights" => $budget_inter_flights,
		"budget_flexibility" => $inquiry->budget_flexibility,
		"specific_requests" => $inquiry->specific_requests
	);

	http_response_code(200);
	echo json_encode($inquiry_data);

	$database->close();
?>