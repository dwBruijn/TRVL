<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: GET'); // allow only GET requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/Offer.php";
	include_once "../models/Inquiry.php";
	include_once "../models/FlightType.php";
	include_once "../helpers/helpers.php";


	// start a db connection
	$database = new Database();
	$db = $database->connect();

	$offer = new Offer($db);
	$inquiry = new Inquiry($db);
	$flight_type = new FlightType($db);

	// get inquiry id from GET request
	// ex http://localhost/trvl/api/offer/read_offer.php?inquiry_id=28
	$inquiry->id = isset($_GET['inquiry_id']) ? $_GET['inquiry_id'] : die();

	// get offer id associated with this inquiry id
	$offer->id = $inquiry->get_offer_id_by_inquiry_id();
	if($offer->id < 0) {
		$database->close();
		errorExit("Failed", "Offer not found.", 401);
	}

	// get offer details
	if(!$offer->read()) {
		$database->close();
		errorExit("Failed", "Unable to read offer.", 401);
	}

	if($offer->tour_guide) {
		$offer->tour_guide = "Yes";
	} else {
		$offer->tour_guide = "No";
	}

	// fetch flight type by type_id
	$flight_type_string = $flight_type->get_flighttype_by_id($offer->flight_type);

	// calculate total costs
	$inquiry->number_of_persons = $inquiry->get_number_of_persons_by_id();
	$inquiry->trip_length = $inquiry->get_trip_length_by_id();
	$total_price = (int)$inquiry->number_of_persons * ((int)$offer->ticket_price + (int)$offer->ground_transp_price_per_person + (int)$offer->activities_price_per_person) + (int)$offer->price_tour_guide + (int)$offer->number_of_rooms * ((int)$offer->room_price_per_night * ((int)$inquiry->trip_length * 7));

	$offer_data = array(
		"offer_id" => $offer->id,
		"flight_type" => $flight_type_string,
		"flight_accommodation" => $offer->flight_accommodation,
		"ticket_price" => $offer->ticket_price,
		"ground_transp_price_per_person" => $offer->ground_transp_price_per_person,
		"hotel_name" => $offer->hotel_name,
		"hotel_address" => $offer->hotel_address,
		"number_of_rooms" => $offer->number_of_rooms,
		"room_price_per_night" => $offer->room_price_per_night,
		"tour_guide" => $offer->tour_guide,
		"price_tour_guide" => $offer->price_tour_guide,
		"activities_details" => $offer->activities_details,
		"activities_price_per_person" => $offer->activities_price_per_person,
		"suggestions" => $offer->suggestions,
		"total_price" => "$total_price",
		"requested_changes" => $offer->requested_changes
	);

	http_response_code(200);
	echo json_encode($offer_data);
?>