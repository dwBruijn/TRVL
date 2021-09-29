<?php 
	class InquiryStatus {

		// db conn and table name
		private $conn;
		private $table_name = "inquiry_status";

		public $id;
		public $status;

		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		// returns status_id on success and 0 if not found and -1 on failure
		public function get_inquiry_status_id_by_status($status) {
			$this->status = $status;

			$query = "
				SELECT id FROM " . $this->table_name . " 
				WHERE status = :status";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize country name
			$this->status = htmlspecialchars(strip_tags($this->status));

			$stmt->bindParam(":status", $this->status);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->id = $row["id"];
				return $this->id;
			}

			return -1;
		}

		public function get_inquiry_status_by_id($id) {
			$this->id = $id;

			$query = "
				SELECT status FROM " . $this->table_name . " 
				WHERE id = :id";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->status = $row["status"];
				return $this->status;
			}

			return "";
		}
	}

?>