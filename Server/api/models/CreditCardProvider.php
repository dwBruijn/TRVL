<?php 
	class CreditCardProvider {
		private $conn;
		private $table_name = "credit_card_providers";

		public $id;
		public $name;

		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		// attempts to match the card_number with a supported provider
		function get_provider_name_by_number($card_number) {
			// before you attempt to match remove all non-digits
			$card_number = preg_replace("/[^0-9]/", "", $card_number);

			if(preg_match("/^4\d{12}(?:\d{3})?$/", $card_number)) {
				return "Visa";
			} else if(preg_match("/^5[1-5]\d{2}(| |-)(?:\d{4}\1){2}\d{4}$/", $card_number)) {
				return "Mastercard";
			} else if(preg_match("/^3[47][0-9]{13}$/", $card_number)) {
				return "Amex";
			} else {
				"";
			}
		}

		// returns provider id by name and -1 on error
		public function get_provider_id_by_name($name) {
			$query = "
					SELECT id FROM " . $this->table_name . "
					WHERE name = :name
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize country name
			$this->name = htmlspecialchars(strip_tags($this->name));

			$stmt->bindParam(":name", $name);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];
				return $this->id;
			}

			return -1;
		}
	}
?>