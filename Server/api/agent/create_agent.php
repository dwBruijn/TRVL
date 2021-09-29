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
	include_once "../models/Gender.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init an agent object
	$agent = new Agent($db);
	// init a country object associated to this agent
	$country = new Country($db);
	// init a gender object associated to this agent
	$gender = new Gender($db);

	// read in POST data
	$data = json_decode(file_get_contents("php://input"));

	$agent->firstname = $data->firstname;
	$agent->lastname = $data->lastname;
	$agent->email = $data->email;
	$agent->password = $data->password;
	$agent->phone = $data->phone;
	$agent->date_of_birth = $data->date_of_birth;
	$agent->city = $data->city;
	$agent->address = $data->address;
	$agent->hire_date = $data->hire_date;
	$agent->salary = $data->salary;
	// fetch country_id using country name
	$agent->country_id = $country->get_country_id_by_name($data->country);
	// fetch gender_id using gender name
	$agent->gender_id = $gender->get_gender_id_by_gender_name($data->gender); 


	if($agent->email_exists()) {
		http_response_code(400);
		echo json_encode(array("message" => "Unable to create agent", 
			"error" => "Email already exists."));

		$database->close();
		exit;
	}

	// check POSTed data and create user
	if(
		!empty($agent->firstname) &&
		!empty($agent->lastname) &&
		!empty($agent->email) &&
		!empty($agent->password) &&
		!empty($agent->phone) &&
		!empty($agent->date_of_birth) &&
		!empty($agent->city) &&
		!empty($agent->address) && 
		$agent->country_id > 0 &&
		$agent->gender_id > 0 &&
		$agent->create()
	) {
		http_response_code(200);
		echo json_encode(array("message" => "Agent created"));
	} else {
		http_response_code(400);
		echo json_encode(array("message" => "Unable to create agent", "error" => "$agent->error"));
	}

	$database->close();

?>