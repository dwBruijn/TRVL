<?php 
	class CreditCard {
		private $conn;
		private $table_name = "credit_cards";

		public $id;
		public $name_on_card;
		public $card_number;
		public $expiration_date;
		public $cvv;
		public $provider_id;

		public function __construct($db) {
			$this->conn = $db;
		}

		public function create() {
			$query = "
					INSERT INTO " . $this->table_name . " SET 
					name_on_card = :name_on_card,
					card_number = :card_number,
					expiration_date = :expiration_date,
					cvv = :cvv,
					provider_id = :provider_id
			";

			$stmt = $this->conn->prepare($query);

			$this->name_on_card = htmlspecialchars(strip_tags($this->name_on_card));
			$this->card_number = htmlspecialchars(strip_tags($this->card_number));
			$this->expiration_date = htmlspecialchars(strip_tags($this->expiration_date));
			$this->cvv = htmlspecialchars(strip_tags($this->cvv));
			$this->provider_id = htmlspecialchars(strip_tags($this->provider_id));

			$stmt->bindParam(":name_on_card", $this->name_on_card);
			$stmt->bindParam(":card_number", $this->card_number);
			$stmt->bindParam(":expiration_date", $this->expiration_date);
			$stmt->bindParam(":cvv", $this->cvv);
			$stmt->bindParam(":provider_id", $this->provider_id);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}

		// use the newly created credit card's number to get its id and associate it with its user
		public function get_card_id_by_card_number($card_number) {
			$query = "
					SELECT id FROM " . $this->table_name . " 
					WHERE card_number = :card_number
			";

			$stmt = $this->conn->prepare($query);

			$this->card_number = htmlspecialchars(strip_tags($this->card_number));

			$stmt->bindParam(":card_number", $this->card_number);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];
				return $this->id;
			}

			return -1;
		}
	}
?>