# 🏨 Hotel Booking System

A full-stack desktop application for managing hotel room bookings, built with **Java**, **JavaFX**, and **MySQL**.

---

## 📌 Overview

This application allows hotel staff to manage room availability, make bookings, and handle CRUD operations through an intuitive desktop interface. It follows a clean **MVC (Model-View-Controller)** architecture and uses **Maven** for build automation.

---

## 🚀 Features

- 🛏️ Real-time room availability tracking
- 📋 Full booking management (Create, Read, Update, Delete)
- 🎨 Interactive UI with dynamic room status updates and custom CSS styling
- 🗄️ MySQL database integration via JDBC
- ⚙️ Maven-managed dependencies and build lifecycle

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java |
| UI Framework | JavaFX |
| Database | MySQL |
| DB Connectivity | JDBC |
| Build Tool | Maven |
| Architecture | MVC |

---

## 🗂️ Project Structure

```
HotelBookingSystem/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/     # MVC Controllers
│   │   │   ├── model/          # Data models (Room, Booking, Guest)
│   │   │   └── view/           # JavaFX FXML views
│   │   └── resources/
│   │       ├── fxml/           # UI layout files
│   │       └── css/            # Custom stylesheets
├── database/
│   └── schema.sql              # MySQL schema setup script
└── pom.xml                     # Maven configuration
```

---

## 🗄️ Database Schema

The MySQL database consists of the following tables:

- **rooms** — room details, type, price, status
- **guests** — guest personal information
- **bookings** — booking records linking guests to rooms
- **staff** — hotel staff accounts
- **payments** — payment records per booking

---

## ⚙️ Setup & Installation

### Prerequisites
- Java JDK 17+
- MySQL 8.0+
- Maven 3.6+
- JavaFX SDK

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/shreyanshbhaik/hotel-booking-system.git
   cd hotel-booking-system
   ```

2. **Set up the database**
   ```sql
   -- Run the schema file in MySQL
   source database/schema.sql;
   ```

3. **Configure database credentials**
   
   Update `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/hotel_db
   db.user=your_username
   db.password=your_password
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn javafx:run
   ```

---

## 📸 Screenshots

> _Add screenshots of your application here_

---

## 🧠 What I Learned

- Implementing **MVC architecture** in a desktop Java application
- Designing **normalized relational schemas** for real-world booking workflows
- Using **JDBC** for efficient database operations
- Building dynamic UIs with **JavaFX** and custom CSS

---

## 👤 Author

**Shreyansh Bhaik**  
[LinkedIn](https://linkedin.com/in/shreyansh-bhaik) • [GitHub](https://github.com/shreyanshbhaik)
