<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	include_once "../config/Database.php";
	include_once "../models/User.php";
	include_once "../models/Country.php";
	include_once "../models/Gender.php";
	include_once "../models/CreditCard.php";
	include_once "../models/CreditCardProvider.php";
	include_once "../helpers/helpers.php";

	// start a db connection
	$database = new Database();
	$db = $database->connect();

	// init a user object
	$user = new User($db);
	// init a country object associated to this user
	$country = new Country($db);
	// init a gender object associated to this user
	$gender = new Gender($db);
	// init a creditcard object assocated to this user
	$credit_card = new CreditCard($db);
	// init a creditcardprovider object associated with the user's credit card
	$credit_card_provider = new CreditCardProvider($db);

	// read in POST data
	$data = json_decode(file_get_contents("php://input"));

	// set user obj properties based on submitted data
	$user->firstname = $data->firstname;
	$user->lastname = $data->lastname;
	$user->email = $data->email;
	$user->password = $data->password;
	$confirm_password = $data->confirm_password;
	$user->phone = $data->phone;
	$user->date_of_birth = $data->date_of_birth;
	$user->city = $data->city;
	$user->address = $data->address;
	// fetch country_id using country name
	$user->country_id = $country->get_country_id_by_name($data->country);
	// fetch gender_id using gender name
	$user->gender_id = $gender->get_gender_id_by_gender_name($data->gender);
	if($user->gender_id < 0) {
		$database->close();
		errorExit("Unable to create account", "Invalid gender value.", 400);
	}

	// check if the provided email address is associated to an account
	if($user->email_exists()) {
		$database->close();
		errorExit("Unable to create account", "Email already exists.", 400);
	}

	// country check
	if($user->country_id < 1) {
		$database->close();
		errorExit("Failed", "Country not supported.", 400);
	}

	// password check
	if(!validate_password($user->password, $confirm_password)) {
		$database->close();
		errorExit("Failed", "Passwords don't match.", 400);
	}

	// email check
	if (!check_email_format($user->email)) {
	 	$database->close();
		errorExit("Failed", "Invalid email format.", 400);
	}

	// date of birth check
	if(!check_date_format($user->date_of_birth)) {
		$database->close();
		errorExit("Failed", "Invalid date of birth format.", 400);
	}

	// read in credit card details
	$credit_card->name_on_card = $data->name_on_card;
	$credit_card->card_number = $data->card_number;
	$credit_card->expiration_date = $data->expiration_date;
	$credit_card->cvv = $data->cvv;

	// expiration date check
	if(!check_date_format($credit_card->expiration_date)) {
		$database->close();
		errorExit("Failed", "Invalid expiration date format.", 400);
	}
	// cvv check
	if(!check_cvv_foramt($credit_card->cvv)) {
		$database->close();
		errorExit("Failed", "Invalid CVV.", 400);
	}

	// get the credit card provider name
	$provider_name = $credit_card_provider->get_provider_name_by_number($credit_card->card_number);
	// if supported
	if($provider_name != "") {
		// get the provider's id
		$credit_card->provider_id = $credit_card_provider->get_provider_id_by_name($provider_name);
	} else {
		$database->close();
		errorExit("Failed", "Credit card provider not supported.", 400);
	}
	// check POSTed data and create creditcard first
	if(
		!empty($credit_card->name_on_card) &&
		!empty($credit_card->card_number) &&
		!empty($credit_card->expiration_date) &&
		!empty($credit_card->cvv) &&
		!empty($credit_card->provider_id) &&
		$credit_card->create()
	) {
		// get id by card_number
		$user->credit_card_id = $credit_card->get_card_id_by_card_number($credit_card->card_number);
	} else {
		$database->close();
		errorExit("Failed", "Unable to create credit card.", 400);
	}

	// check POSTed data and create user
	if(
		!empty($user->firstname) && 
		!empty($user->lastname) && 
		!empty($user->email) && 
		!empty($user->password) && 
		!empty($user->phone) && 
		!empty($user->date_of_birth) && 
		!empty($user->city) && 
		!empty($user->address) && 
		$user->country_id > 0 && 
		$user->gender_id > 0 && 
		$user->credit_card_id > 0 &&
		$user->create()
	) {
		http_response_code(200);
		echo json_encode(array("message" => "Account created successfully.", "error" => ""));
	} else {
		errorExit("Failed", "$user->error", 400);
	}

	$database->close();
?>