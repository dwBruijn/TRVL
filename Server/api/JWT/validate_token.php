<?php 
	// Headers
	header('Access-Control-Allow-Origin: *');
	header('Content-Type: application/json');	// change to application/json to see any php errors
	header('Access-Control-Allow-Methods: POST'); // allow only POST requests
	// specify allowed header values
	header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, X-Requested-With, Authorization');

	// required to decode jwt
	include_once '../config/core.php';
	include_once '../libs/php-jwt-master/src/BeforeValidException.php';
	include_once '../libs/php-jwt-master/src/ExpiredException.php';
	include_once '../libs/php-jwt-master/src/SignatureInvalidException.php';
	include_once '../libs/php-jwt-master/src/JWT.php';
	use \Firebase\JWT\JWT;

	// get POSTed data
	$data = json_decode(file_get_contents("php://input"));
	
	// get jwt
	$jwt = isset($data->jwt) ? $data->jwt : "";

	// if jwt is not empty
	if($jwt){
	 
		// if decode succeed, show user details
		try {
			// decode jwt
			$decoded = JWT::decode($jwt, $key, array('HS256'));
	 
			// set response code
			http_response_code(200);
	 
			// show user details
			echo json_encode(array(
				"message" => "Access granted.",
				// "data" => $decoded->data
			));
	 
		} catch (Exception $e){
			// decoding failed so JWT is invalid
			http_response_code(401);
			echo json_encode(array(
				"message" => "Access denied.",
				"error" => $e->getMessage()
			));
		}
		
	} else{
		// JWT is empty
		http_response_code(401);
		echo json_encode(array("message" => "Access denied."));
	}

?>