<?php 

	class Offer {
		// db conn and table name
		private $conn;
		private $table_name = "offers";

		public $id;
		public $flight_type;
		public $flight_accommodation;
		public $ticket_price;
		public $ground_transp_price_per_person;
		public $hotel_name;
		public $hotel_address;
		public $number_of_rooms;
		public $room_price_per_night;
		public $tour_guide;
		public $price_tour_guide;
		public $activities_details;
		public $activities_price_per_person;
		public $suggestions;
		public $requested_changes;


		// constructor
		public function __construct($db) {
			$this->conn = $db;
		}

		public function create() {
			// insert query
			$query = "INSERT INTO " . $this->table_name . " SET 
					flight_type = :flight_type,
					flight_accommodation = :flight_accommodation,
					ticket_price = :ticket_price,
					ground_transp_price_per_person = :ground_transp_price_per_person,
					hotel_name = :hotel_name,
					hotel_address = :hotel_address,
					number_of_rooms = :number_of_rooms,
					room_price_per_night = :room_price_per_night,
					tour_guide = :tour_guide,
					price_tour_guide = :price_tour_guide,
					activities_details = :activities_details,
					activities_price_per_person = :activities_price_per_person,
					suggestions = :suggestions
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->flight_type = htmlspecialchars(strip_tags($this->flight_type));
			$this->flight_accommodation = htmlspecialchars(strip_tags($this->flight_accommodation));
			$this->ticket_price = htmlspecialchars(strip_tags($this->ticket_price));
			$this->ground_transp_price_per_person = htmlspecialchars(strip_tags($this->ground_transp_price_per_person));
			$this->hotel_name = htmlspecialchars(strip_tags($this->hotel_name));
			$this->hotel_address = htmlspecialchars(strip_tags($this->hotel_address));
			$this->number_of_rooms = htmlspecialchars(strip_tags($this->number_of_rooms));
			$this->room_price_per_night = htmlspecialchars(strip_tags($this->room_price_per_night));
			$this->tour_guide = htmlspecialchars(strip_tags($this->tour_guide));
			$this->price_tour_guide = htmlspecialchars(strip_tags($this->price_tour_guide));
			$this->activities_details = htmlspecialchars(strip_tags($this->activities_details));
			$this->activities_price_per_person = htmlspecialchars(strip_tags($this->activities_price_per_person));
			$this->suggestions = htmlspecialchars(strip_tags($this->suggestions));

			$stmt->bindParam(":flight_type", $this->flight_type);
			$stmt->bindParam(":flight_accommodation", $this->flight_accommodation);
			$stmt->bindParam(":ticket_price", $this->ticket_price);
			$stmt->bindParam(":ground_transp_price_per_person", $this->ground_transp_price_per_person);
			$stmt->bindParam(":hotel_name", $this->hotel_name);
			$stmt->bindParam(":hotel_address", $this->hotel_address);
			$stmt->bindParam(":number_of_rooms", $this->number_of_rooms);
			$stmt->bindParam(":room_price_per_night", $this->room_price_per_night);
			$stmt->bindParam(":tour_guide", $this->tour_guide);
			$stmt->bindParam(":price_tour_guide", $this->price_tour_guide);
			$stmt->bindParam(":activities_details", $this->activities_details);
			$stmt->bindParam(":activities_price_per_person", $this->activities_price_per_person);
			$stmt->bindParam(":suggestions", $this->suggestions);

			if($stmt->execute()) {
				$this->id = $this->conn->lastInsertId();
				return $this->id;
					
			}
			return -1;
		}


		// get offer details by id
		public function read() {
			$query = "SELECT 
					flight_type, flight_accommodation, ticket_price, 
					ground_transp_price_per_person, hotel_name, hotel_address, 
					number_of_rooms, room_price_per_night, tour_guide, price_tour_guide, 
					activities_details, activities_price_per_person, suggestions, requested_changes 
					FROM " . $this->table_name . " WHERE id = :id
			";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->id = htmlspecialchars(strip_tags($this->id));

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				$this->flight_type = $row["flight_type"];
				$this->flight_accommodation = $row["flight_accommodation"];
				$this->ticket_price = $row["ticket_price"];
				$this->ground_transp_price_per_person = $row["ground_transp_price_per_person"];
				$this->hotel_name = $row["hotel_name"];
				$this->hotel_address = $row["hotel_address"];
				$this->number_of_rooms = $row["number_of_rooms"];
				$this->room_price_per_night = $row["room_price_per_night"];
				$this->tour_guide = $row["tour_guide"];
				$this->price_tour_guide = $row["price_tour_guide"];
				$this->activities_details = $row["activities_details"];
				$this->activities_price_per_person = $row["activities_price_per_person"];
				$this->suggestions = $row["suggestions"];
				$this->requested_changes = $row["requested_changes"];
				
				return true;
			}
			return false;
		}

		public function update() {
			$query = "UPDATE " . $this->table_name . " SET 
					flight_type = :flight_type, flight_accommodation = :flight_accommodation, 
					ticket_price = :ticket_price, ground_transp_price_per_person = :ground_transp_price_per_person, 
					hotel_name = :hotel_name, hotel_address = :hotel_address, 
					number_of_rooms = :number_of_rooms, room_price_per_night = :room_price_per_night, 
					tour_guide = :tour_guide, price_tour_guide = :price_tour_guide, 
					activities_details = :activities_details, activities_price_per_person = :activities_price_per_person, 
					suggestions = :suggestions, flag_offer_updated = 1, flag_offer_declined = 0 
					WHERE id = :id";

			// prepare the query
			$stmt = $this->conn->prepare($query);

			// sanitize POST data
			$this->flight_type = htmlspecialchars(strip_tags($this->flight_type));
			$this->flight_accommodation = htmlspecialchars(strip_tags($this->flight_accommodation));
			$this->ticket_price = htmlspecialchars(strip_tags($this->ticket_price));
			$this->ground_transp_price_per_person = htmlspecialchars(strip_tags($this->ground_transp_price_per_person));
			$this->hotel_name = htmlspecialchars(strip_tags($this->hotel_name));
			$this->hotel_address = htmlspecialchars(strip_tags($this->hotel_address));
			$this->number_of_rooms = htmlspecialchars(strip_tags($this->number_of_rooms));
			$this->room_price_per_night = htmlspecialchars(strip_tags($this->room_price_per_night));
			$this->tour_guide = htmlspecialchars(strip_tags($this->tour_guide));
			$this->price_tour_guide = htmlspecialchars(strip_tags($this->price_tour_guide));
			$this->activities_details = htmlspecialchars(strip_tags($this->activities_details));
			$this->activities_price_per_person = htmlspecialchars(strip_tags($this->activities_price_per_person));
			$this->suggestions = htmlspecialchars(strip_tags($this->suggestions));

			$stmt->bindParam(":flight_type", $this->flight_type);
			$stmt->bindParam(":flight_accommodation", $this->flight_accommodation);
			$stmt->bindParam(":ticket_price", $this->ticket_price);
			$stmt->bindParam(":ground_transp_price_per_person", $this->ground_transp_price_per_person);
			$stmt->bindParam(":hotel_name", $this->hotel_name);
			$stmt->bindParam(":hotel_address", $this->hotel_address);
			$stmt->bindParam(":number_of_rooms", $this->number_of_rooms);
			$stmt->bindParam(":room_price_per_night", $this->room_price_per_night);
			$stmt->bindParam(":tour_guide", $this->tour_guide);
			$stmt->bindParam(":price_tour_guide", $this->price_tour_guide);
			$stmt->bindParam(":activities_details", $this->activities_details);
			$stmt->bindParam(":activities_price_per_person", $this->activities_price_per_person);
			$stmt->bindParam(":suggestions", $this->suggestions);
			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}

		// set an offer as accepted
		public function accept_offer() {
			$query = "UPDATE " . $this->table_name . " 
					SET flag_offer_accepted = 1 WHERE id = :id";

			$stmt = $this->conn->prepare($query);

			$this->id = htmlspecialchars(strip_tags($this->id));

			$stmt->bindParam(":id", $this->id);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}

		// set offer to declined and add requested changes
		public function request_changes() {
			$query = "UPDATE " . $this->table_name . " SET 
					requested_changes = :requested_changes, flag_offer_declined = 1, flag_offer_accepted = 0, flag_offer_updated = 0 WHERE id = :id";

			$stmt = $this->conn->prepare($query);

			$this->id = htmlspecialchars(strip_tags($this->id));
			$this->requested_changes = htmlspecialchars(strip_tags($this->requested_changes));

			$stmt->bindParam(":id", $this->id);
			$stmt->bindParam(":requested_changes", $this->requested_changes);

			if($stmt->execute()) {
				return true;
			}

			return false;
		}
	}


?>