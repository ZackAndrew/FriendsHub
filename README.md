# FriendsHub 🌍

**FriendsHub** is a REST API for a social networking platform focused on real-world interactions. The core feature of the application is a smart scheduling algorithm that helps friends find overlapping free time (**Common Slots**) for meetups and walks.

> **Course Project**
>
> **Author:** Andrii Zakordonskyi
> **Technology:** Java 21 / Spring Boot 3

---

## 🚀 Key Features

### 1. 🔐 Authentication & Security
* User registration and login.
* Endpoint protection using **JWT (JSON Web Tokens)**.
* Custom implementation of `UserDetailsService` and security filters.

### 2. 👥 Friend Management
* Sending friendship requests.
* Accepting or declining requests.
* Viewing the friends list.
* *Validation:* Prevents self-requests and duplicate relationships.

### 3. 📅 Time Planning (Availability)
* Users can define their "availability slots" (periods when they are free).
* **Overlapping Validation:** Prevents users from adding conflicting time slots (implemented at both business logic and database levels).
* Timezone support via ISO 8601 format.

### 4. 🤝 The "Common Walks" Algorithm (Matcher)
* The system automatically analyzes the schedules of two friends.
* It calculates the **Intersection** of time intervals.
* Returns the exact time window when both users are available to meet.

---

## 🛠 Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x (Web, Security, Data JPA)
* **Database:** PostgreSQL
* **Migrations:** Liquibase
* **Containerization:** Docker & Docker Compose
* **Documentation:** OpenApi (Swagger)
* **Tools:** Lombok, MapStruct

---

## ⚙️ Getting Started

The project is fully containerized. You need **Docker** and **Docker Compose** installed on your machine.

### 1. Clone the repository
```bash
git clone [https://github.com/your-username/friendshub.git](https://github.com/your-username/friendshub.git)
cd friendshub