<?php 
	class FlightType {
		private $conn;
		private $table_name = "flight_type";

		public $id;
		public $type;

		public function __construct($db) {
			$this->conn = $db;
		}

		public function get_flightype_id_by_type($type) {
			$this->type = $type;
			
			if(empty($this->type)) {
				return -1;
			}

			$query = "SELECT id FROM " . $this->table_name . " 
					WHERE type = :flight_type";

			$stmt = $this->conn->prepare($query);

			$this->type = htmlspecialchars(strip_tags($this->type));

			$stmt->bindParam(":flight_type", $this->type);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];

				if($this->id > 0) {
					return $this->id;
				}
			}

			return -1;
		}

		public function get_flighttype_by_id($id) {
			$this->id = $id;

			if(empty($this->id)) {
				return "";
			}

			$query = "SELECT type FROM " . $this->table_name . " 
					WHERE id = :id";

			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->type = $row["type"];

				return $this->type;
			}

			return "";
		}
	}
?>