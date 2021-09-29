<?php 
	class Country {

		// db conn and table name
		private $conn;
		private $table_name = "countries";

		public $id;
		public $name;

		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		// returns country_id on success and 0 if not found and -1 on failure
		public function get_country_id_by_name($name) {
			$this->name = $name;

			$query = "
				SELECT id FROM " . $this->table_name . " 
				WHERE name = :name";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize country name
			$this->name = htmlspecialchars(strip_tags($this->name));

			// bind name param
			$stmt->bindParam(":name", $this->name);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];
				return $this->id;
			}

			return -1;
		}

		public function get_country_name_by_id($id) {
			$this->id = $id;

			$query = "SELECT name FROM " . $this->table_name ." 
			WHERE id = :id";

			$stmt = $this->conn->prepare($query);
			
			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->name = $row["name"];
				return $this->name;
			}

			return "";
		}
	}
?>