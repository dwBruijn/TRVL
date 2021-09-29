<?php 
	class TripType {

		// db conn and table name
		private $conn;
		private $table_name = "trip_type";

		public $id;
		public $type;

		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		// returns country_id on success and 0 if not found and -1 on failure
		public function get_trip_type_id_by_type($type) {
			$this->type = $type;

			$query = "
				SELECT id FROM " . $this->table_name . " 
				WHERE type = :type";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize country name
			$this->type = htmlspecialchars(strip_tags($this->type));

			$stmt->bindParam(":type", $this->type);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];
				return $this->id;
			}

			return -1;
		}

		// get trip typename by id
		public function get_trip_type_by_id($id) {
			$this->id = $id;

			$query = "
				SELECT type FROM " . $this->table_name . " 
				WHERE id = :id";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize country name
			$this->id = htmlspecialchars(strip_tags($this->id));

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