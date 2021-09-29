<?php 

	function errorExit($message, $error, $errorCode) {
		http_response_code($errorCode);
		echo json_encode(array(
			"message" => "$message", 
			"error" => "$error"));
		exit;
	}

	function validate_password($password, $confirm_password) {
		if($password == $confirm_password) {
			return true;
		}
		return false;
	}

	function check_email_format($email) {
		if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
	 		return false;
		}
		return true;
	}

	function check_date_format($date) {
		if(!preg_match("/\d{4}-\d{2}-\d{2}/", $date)) {
			return false;
		}
		return true;
	}

	function check_date_in_past($date) {
		$dt = new DateTime($date);
		if($dt->getTimestamp() < time()) {
			return true;
		}
		return false;
	}

	function check_cvv_foramt($cvv) {
		if(!preg_match("/^\d{3}$/", $cvv)) {
			return false;
		}
		return true;
	}

	function check_is_numeric($value) {
		if(is_numeric($value)) {
			return true;
		}
		return false;
	}

	function validate_password_strength($password) {
		if(!preg_match("/[A-Z]/", $password)) {
			return "Password must contain at least one uppercase letter";
		}
		if(!preg_match("/[a-z]/", $password)) {
			return "Password must contain at least one lowercase letter";
		}
		if(!preg_match("/[0-9]/", $password)) {
			return "Password must contain at least one digit";
		}
		if(strlen($password) < 8) {
			return "Password must be at least 8 characters long";
		}

		return "";
	}
?>