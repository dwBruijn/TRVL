<?php
	include_once '../libs/php-jwt-master/src/BeforeValidException.php';
	include_once '../libs/php-jwt-master/src/ExpiredException.php';
	include_once '../libs/php-jwt-master/src/SignatureInvalidException.php';
	include_once '../libs/php-jwt-master/src/JWT.php';
	use \Firebase\JWT\JWT;


	// defines vars used for JWT signing

	// show error reports
	error_reporting(E_ALL);

	// set your default time-zone
	date_default_timezone_set('Asia/Tokyo');
	 
	// variables used for jwt
	$key = "example_key";
	$issued_at = time();
	$expiration_time = $issued_at + (60 * 60); // valid for 1 hour
	$issuer = "http://localhost/trvl/";



	// generate JWT on login
	function create_jwt($id, $first_name, $last_name, $email) {
		// defined in core.php
		global $key;
		global $issued_at;
		global $issuer;
		global $expiration_time;

		$token = array(
		   "iat" => $issued_at,
		   "exp" => $expiration_time,
		   "iss" => $issuer,
		   "data" => array(
			   "id" => $id,
			   "firstname" => $first_name,
			   "lastname" => $last_name,
			   "email" => $email
		   )
		);

		// generate jwt
		$jwt = JWT::encode($token, $key);

		return $jwt;
	}

	// validate user JWT and return user_id
	function validate_user_jwt($jwt) {
		global $key;

		if($jwt){
			try {
				// decode jwt
				$decoded = JWT::decode($jwt, $key, array('HS256'));

				// get user id from JWT
				$user_id = $decoded->data->id;

				return $user_id;
			}catch (Exception $e){
				return -1;
			}
		} else {
			return -1;
		}
	}

	// validate agent JWT and return agent_id
	function validate_agent_jwt($jwt) {
		global $key;

		if($jwt){

			try {

				// decode JWT
				$decoded = JWT::decode($jwt, $key, array('HS256'));

				// get agent_id from JWT
				$agent_id = $decoded->data->id;

				return $agent_id;
			}catch (Exception $e){
				return -1;
			}
		} else {
			return -1;
		}

	}
?>