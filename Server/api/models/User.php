<?php 
	class User {
		// db conn and table name
		private $conn;
		private $table_name = "users";

		// object properties
		public $id;
		public $firstname;
		public $lastname;
		public $email;
		public $password;
		public $phone;
		public $data_of_birth;
		public $gender_id;
		public $country_id;
		public $city;
		public $address;

		public $error = "";

		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		// create a user
		public function create() {
			// insert query
			$query = "INSERT INTO " . $this->table_name . " SET 
					firstname = :firstname,
					lastname = :lastname,
					email = :email,
					password = :password,
					phone = :phone,
					date_of_birth = :date_of_birth,
					gender_id = :gender_id,
					city = :city,
					address = :address,
					country_id = :country_id,
					credit_card_id = :credit_card_id
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->firstname = htmlspecialchars(strip_tags($this->firstname));
			$this->lastname = htmlspecialchars(strip_tags($this->lastname));
			$this->email = htmlspecialchars(strip_tags($this->email));
			$this->password = htmlspecialchars(strip_tags($this->password));
			$this->phone = htmlspecialchars(strip_tags($this->phone));
			$this->data_of_birth = htmlspecialchars(strip_tags($this->data_of_birth));
			$this->gender_id = htmlspecialchars(strip_tags($this->gender_id));
			$this->city = htmlspecialchars(strip_tags($this->city));
			$this->country_id = htmlspecialchars(strip_tags($this->country_id));
			$this->address = htmlspecialchars(strip_tags($this->address));
			$this->credit_card_id = htmlspecialchars(strip_tags($this->credit_card_id));

			// bind values to named params in query
			$stmt->bindParam(":firstname", $this->firstname);
			$stmt->bindParam(":lastname", $this->lastname);
			$stmt->bindParam(":email", $this->email);
			$stmt->bindParam(":phone", $this->phone);
			$stmt->bindParam(":date_of_birth", $this->date_of_birth);
			$stmt->bindParam(":gender_id", $this->gender_id);
			$stmt->bindParam(":city", $this->city);
			$stmt->bindParam(":address", $this->address);
			$stmt->bindParam(":credit_card_id", $this->credit_card_id);

			// hash the password first
			$pass_hash = password_hash($this->password, PASSWORD_BCRYPT);
			$stmt->bindParam(":password", $pass_hash);

			// bind country_id
			$stmt->bindParam(":country_id", $this->country_id);


			if($stmt->execute()) {
					return true;
			}

			return false;
		}

		public function email_exists() {
			$query = "SELECT id, firstname, lastname, password 
					FROM ". $this->table_name . " WHERE email = ? 
					LIMIT 0,1";
			
			$stmt = $this->conn->prepare($query);

			$this->email = htmlspecialchars(strip_tags($this->email));

			$stmt->bindParam(1, $this->email);

			$stmt->execute();

			// get number of rows
			$num = $stmt->rowCount();

			// if email exists, assign values to object properties for easy access and use for php sessions
			if($num > 0) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);

				$this->id = $row["id"];
				$this->firstname = $row["firstname"];
				$this->lastname = $row["lastname"];
				$this->password = $row["password"];

				return true;
			}

			return false;
		}

		public function get_user_name_by_id($id) {
			$query = "SELECT firstname, lastname FROM " . $this->table_name . " 
			WHERE id = :id";

			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":id", $id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->firstname = $row["firstname"];
				$this->lastname = $row["lastname"];

				return $this->firstname . " " . $this->lastname;
			}
			
			return "";
		}
	}
?>