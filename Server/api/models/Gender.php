<?php 
	class Gender {
		private $conn;
		private $table_name = "gender";

		public $id;
		public $gender_name;

		public function __construct($db) {
			$this->conn = $db;
		}

		public function get_gender_id_by_gender_name($gender_name) {
			$this->gender_name = $gender_name;
			
			if(empty($this->gender_name)) {
				return -1;
			}

			$query = "SELECT id FROM " . $this->table_name . " 
					WHERE gender_name = :gender_name";

			$stmt = $this->conn->prepare($query);

			$this->gender_name = htmlspecialchars(strip_tags($this->gender_name));

			$stmt->bindParam(":gender_name", $this->gender_name);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];

				if($this->id > 0) {
					return $this->id;
				}
			}

			return -1;
		}
	}
?>