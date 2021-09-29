<?php 

	include_once "Country.php";
	include_once "InquiryStatus.php";
	include_once "Agent.php";
	include_once "User.php";

	class Inquiry {
		// db conn and table name
		private $conn;
		private $table_name = "inquiries";

		public $id;
		public $user_id;
		public $agent_id;
		public $country_id;
		public $travel_date;
		public $trip_length;
		public $number_of_persons;
		public $level_of_accommodation;
		public $number_of_hotel_rooms;
		public $type_id;
		public $serv_activities;
		public $serv_interflights;
		public $serv_incountry_transportation;
		public $serv_tour_guide;
		public $requested_activities;
		public $budget_per_person;
		public $budget_inter_flights;
		public $budget_flexibility;
		public $specific_requests;
		public $status_id;
		public $offer_id;


		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		public function create() {
			// insert query
			$query = "INSERT INTO " . $this->table_name . " SET 
					user_id = :user_id,
					agent_id = :agent_id,
					country_id = :country_id,
					travel_date = :travel_date,
					trip_length = :trip_length,
					number_of_persons = :number_of_persons,
					level_of_accommodation = :level_of_accommodation,
					number_of_hotel_rooms = :number_of_hotel_rooms,
					type_id = :type_id,
					serv_activities = :serv_activities,
					serv_interflights = :serv_interflights,
					serv_incountry_transportation = :serv_incountry_transportation,
					serv_tour_guide = :serv_tour_guide,
					requested_activities = :requested_activities,
					budget_per_person = :budget_per_person,
					budget_inter_flights = :budget_inter_flights,
					budget_flexibility = :budget_flexibility,
					specific_requests = :specific_requests,
					status_id = :status_id
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->travel_date = htmlspecialchars(strip_tags($this->travel_date));
			$this->trip_length = htmlspecialchars(strip_tags($this->trip_length));
			$this->number_of_persons = htmlspecialchars(strip_tags($this->number_of_persons));
			$this->level_of_accommodation = htmlspecialchars(strip_tags($this->level_of_accommodation));
			$this->number_of_hotel_rooms = htmlspecialchars(strip_tags($this->number_of_hotel_rooms));
			$this->serv_activities = htmlspecialchars(strip_tags($this->serv_activities));
			$this->serv_interflights = htmlspecialchars(strip_tags($this->serv_interflights));
			$this->serv_incountry_transportation = htmlspecialchars(strip_tags($this->serv_incountry_transportation));
			$this->serv_tour_guide = htmlspecialchars(strip_tags($this->serv_tour_guide));
			$this->requested_activities = htmlspecialchars(strip_tags($this->requested_activities));
			$this->budget_per_person = htmlspecialchars(strip_tags($this->budget_per_person));
			$this->budget_inter_flights = htmlspecialchars(strip_tags($this->budget_inter_flights));
			$this->budget_flexibility = htmlspecialchars(strip_tags($this->budget_flexibility));
			$this->specific_requests = htmlspecialchars(strip_tags($this->specific_requests));

			$stmt->bindParam(":travel_date", $this->travel_date);
			$stmt->bindParam(":trip_length", $this->trip_length);
			$stmt->bindParam(":number_of_persons", $this->number_of_persons);
			$stmt->bindParam(":level_of_accommodation", $this->level_of_accommodation);
			$stmt->bindParam(":number_of_hotel_rooms", $this->number_of_hotel_rooms);
			$stmt->bindParam(":serv_activities", $this->serv_activities);
			$stmt->bindParam(":serv_interflights", $this->serv_interflights);
			$stmt->bindParam(":serv_incountry_transportation", $this->serv_incountry_transportation);
			$stmt->bindParam(":serv_tour_guide", $this->serv_tour_guide);
			$stmt->bindParam(":requested_activities", $this->requested_activities);
			$stmt->bindParam(":budget_per_person", $this->budget_per_person);
			$stmt->bindParam(":budget_inter_flights", $this->budget_inter_flights);
			$stmt->bindParam(":budget_flexibility", $this->budget_flexibility);
			$stmt->bindParam(":specific_requests", $this->specific_requests);

			$stmt->bindParam(":type_id", $this->type_id);
			$stmt->bindParam(":user_id", $this->user_id);
			$stmt->bindParam(":agent_id", $this->agent_id);
			$stmt->bindParam(":country_id", $this->country_id);
			$stmt->bindParam(":status_id", $this->status_id);

			if($stmt->execute()) {
					return true;
			}
			return false;
		}

		// get inquiry details by id
		public function read() {
			$query = "SELECT 
					country_id, travel_date, trip_length, number_of_persons,
					level_of_accommodation, number_of_hotel_rooms, type_id,
					serv_activities, serv_interflights, serv_incountry_transportation,
					serv_tour_guide, requested_activities, budget_per_person,
					budget_inter_flights, budget_flexibility, specific_requests 
					FROM " . $this->table_name . " WHERE id = :id
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->id = htmlspecialchars(strip_tags($this->id));

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->country_id = $row["country_id"];
				$this->travel_date = $row["travel_date"];
				$this->trip_length = $row["trip_length"];
				$this->number_of_persons = $row["number_of_persons"];
				$this->level_of_accommodation = $row["level_of_accommodation"];
				$this->number_of_hotel_rooms = $row["number_of_hotel_rooms"];
				$this->type_id = $row["type_id"];
				$this->serv_activities = $row["serv_activities"];
				$this->serv_interflights = $row["serv_interflights"];
				$this->serv_incountry_transportation = $row["serv_incountry_transportation"];
				$this->serv_tour_guide = $row["serv_tour_guide"];
				$this->requested_activities = $row["requested_activities"];
				$this->budget_per_person = $row["budget_per_person"];
				$this->budget_inter_flights = $row["budget_inter_flights"];
				$this->budget_flexibility = $row["budget_flexibility"];
				$this->specific_requests = $row["specific_requests"];

				return true;
			}
			return false;
		}

		public function delete() {
			$query = "DELETE FROM " . $this->table_name . " 
			WHERE id = :id";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->id = htmlspecialchars(strip_tags($this->id));

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}


		// get offer_id of the offer associted to an inquiry
		public function get_offer_id_by_inquiry_id() {
			$this->id = htmlspecialchars(strip_tags($this->id));

			$query = "SELECT offer_id FROM " . $this->table_name . " 
					WHERE id = :id";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($row["offer_id"] > 0) {
					return $row["offer_id"];
				}
			}

			return -1;
		}

		// assign an offer_id to an inquiry
		public function set_offer_id_by_inquiry_id($offer_id) {
			// sanitize inqury_id from POSTed data
			$this->id = htmlspecialchars(strip_tags($this->id));

			// check if inquiry with inquiry_id exists
			$select_query = "SELECT id FROM " . $this->table_name . " 
				WHERE id = :id";
			$stmt = $this->conn->prepare($select_query);
			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($row["id"] <= 0) {
					return false;
				}
			} else {
				return false;
			}

			// update the inquiry
			$update_query = "
				UPDATE " . $this->table_name . " 
				SET offer_id = :offer_id WHERE id = :id
			";

			// prepare the query
			$stmt = $this->conn->prepare($update_query);

			$stmt->bindParam(":offer_id", $offer_id);
			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}

		// assign status id to inquiry
		public function set_inquiry_status_id_by_id($status_id) {
			// sanitize inqury_id from POSTed data
			$this->id = htmlspecialchars(strip_tags($this->id));

			$query = "
				UPDATE " . $this->table_name . " 
				SET status_id = :status_id WHERE id = :id
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":status_id", $status_id);
			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}

		// get number of persons by inquiry id
		// used to calculate the total cost in offer
		public function get_number_of_persons_by_id() {
			$this->id = htmlspecialchars(strip_tags($this->id));

			$query = "SELECT number_of_persons FROM " . $this->table_name . " 
			WHERE id = :id";

			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($row["number_of_persons"] > 0) {
					$this->number_of_persons = $row["number_of_persons"];
					return $this->number_of_persons;
				}
			}

			return -1;
		}

		// get trip length in weeks
		// used to calculate total cost in offer
		public function get_trip_length_by_id() {
			$this->id = htmlspecialchars(strip_tags($this->id));

			$query = "SELECT trip_length FROM " . $this->table_name . " 
			WHERE id = :id";

			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($row["trip_length"] > 0) {
					$this->trip_length = $row["trip_length"];
					return $this->trip_length;
				}
			}

			return -1;
		}

		// checks if a user with user_id owns an inquiry to which an offer with id offer_id is associated
		// used when validating if a user can accept or decline an offer
		public function user_owns_offer($offer_id) {
			$query = "SELECT id FROM " . $this->table_name . " 
					WHERE user_id = :user_id AND offer_id = :offer_id";

			$this->user_id = htmlspecialchars(strip_tags($this->user_id));
			$offer_id = htmlspecialchars(strip_tags($offer_id));

			$stmt = $this->conn->prepare($query);

			$stmt->bindParam(":user_id", $this->user_id);
			$stmt->bindParam(":offer_id", $offer_id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($row["id"] > 0) {
					$this->id = $row["id"];
					return true;
				}
			}

			return false;
		}

		// checks if a agent with agent_id is handling an inquiry to which an offer with id offer_id is associated
		// used when validating if an agent can update an offer
		public function agent_owns_offer($offer_id) {
			$query = "SELECT id FROM " . $this->table_name . " 
					WHERE offer_id = :offer_id AND agent_id = :agent_id";

			$stmt = $this->conn->prepare($query);

			$this->agent_id = htmlspecialchars(strip_tags($this->agent_id));
			$offer_id = htmlspecialchars(strip_tags($offer_id));

			$stmt->bindParam(":agent_id", $this->agent_id);
			$stmt->bindParam(":offer_id", $offer_id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($row["id"] > 0) {
					// check if the inquiry id matches the one POSTed
					if($this->id == $row["id"]) {
						return true;
					}
				}
			}

			return false;
		}

		// checks if a given agent_id is associated to a given inquiry_id
		public function agent_associated_to_inquiry() {
			$query = "SELECT EXISTS(SELECT * FROM " . $this->table_name . " 
					WHERE agent_id = :agent_id AND id = :inquiry_id)";

			$stmt = $this->conn->prepare($query);

			$this->agent_id = htmlspecialchars(strip_tags($this->agent_id));
			$this->id = htmlspecialchars(strip_tags($this->id));

			$stmt->bindParam(":agent_id", $this->agent_id);
			$stmt->bindParam(":inquiry_id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if($stmt->rowCount() > 0) {
					return true;
				}
			}

			return false;
		}
		

		public static function get_inquiries_id_by_user_id($user_id, $db) {
			$inquiries = array();

			$query = "SELECT id FROM inquiries 
					WHERE user_id = :user_id 
					ORDER BY created_at DESC";

			// prepare the query
			$stmt = $db->prepare($query);

			// sanitize the user_id extracted from the JWT
			$user_id = htmlspecialchars(strip_tags($user_id));

			$stmt->bindParam(":user_id", $user_id);

			if($stmt->execute()) {
				// fetch all inquiry ids and store them in array based on creation date
				while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
					$inquiries[] = $row["id"];
				}
			}

			return $inquiries;
		}

		public static function get_inquiries_id_by_agent_id($agent_id, $db) {
			$inquiries = array();

			$query = "SELECT id FROM inquiries 
					WHERE agent_id = :agent_id 
					ORDER BY created_at DESC";

			// prepare the query
			$stmt = $db->prepare($query);

			// sanitize the agent_id extracted from the JWT
			$agent_id = htmlspecialchars(strip_tags($agent_id));

			$stmt->bindParam(":agent_id", $agent_id);

			if($stmt->execute()) {
				// fetch all inquiry ids and store them in array based on creation date
				while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
					$inquiries[] = $row["id"];
				}
			}

			return $inquiries;
		}

		// used to get inquiry details in user_home and AccountHomeActivity (android)
		public static function get_inquiry_details_by_id($id, $db) {

			$query = "SELECT country_id, created_at, status_id, agent_id 
					FROM inquiries WHERE id = :id";
			$country_name = "";
			$created_at = "";
			$inquiry_status_value = "";
			$agent_name = "";

			// country object associated with this inquiry
			$country = new Country($db);
			// inquirystatus object associated with this inquiry
			$inquiry_status = new InquiryStatus($db);
			// agent object associated with this inquiry
			$agent = new Agent($db);

			// prepare the query
			$stmt = $db->prepare($query);

			$stmt->bindParam(":id", $id);

			$stmt->execute();

			$row = $stmt->fetch(PDO::FETCH_ASSOC);

			// get country name based on the country id in inquiry
			$country_name = $country->get_country_name_by_id($row["country_id"]);
			$created_at = $row["created_at"];
			$inquiry_status_value = $inquiry_status->get_inquiry_status_by_id($row["status_id"]);
			$agent_name = $agent->get_agent_name_by_id($row["agent_id"]);

			return array($country_name, $created_at, $inquiry_status_value, $agent_name);
		}

		// used to get inquiry details in agent_home
		public static function get_agent_inquiry_details_by_id($id, $db) {

			$query = "SELECT created_at, status_id, user_id 
					FROM inquiries WHERE id = :id";
			$created_at = "";
			$inquiry_status_value = "";
			$user_name = "";

			// inquirystatus object associated with this inquiry
			$inquiry_status = new InquiryStatus($db);
			// agent object associated with this inquiry
			$user = new User($db);

			// prepare the query
			$stmt = $db->prepare($query);

			$stmt->bindParam(":id", $id);

			$stmt->execute();

			$row = $stmt->fetch(PDO::FETCH_ASSOC);

			$created_at = $row["created_at"];
			$inquiry_status_value = $inquiry_status->get_inquiry_status_by_id($row["status_id"]);
			$user_name = $user->get_user_name_by_id($row["user_id"]);

			return array($user_name, $created_at, $inquiry_status_value);
		}

	}
?>