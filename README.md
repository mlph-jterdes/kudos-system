# Peer Recognition & Kudos System

A full-stack peer recognition system where employees can send and receive kudos, leave comments, and view leaderboards.

## Project Overview
- This project allows employees to:
- Register and login.
- Send kudos and comments to other employees or teams.
- View kudos history (public and private).
- View leaderboards for top employees and teams.
- Admins can upload employees via CSV, reset kudos counts, and manage accounts.

## Tech Stack
### Frontend:
- React
- Material UI
- Axios
### Backend:
- Java 17 / Spring Boot
- Spring Data JPA
- MySQL database
### Build Tools:
- Maven (backend)
- Node.js / npm (frontend)

## Prerequisites
- Node.js + npm
- Java 17 or higher
- Maven
- MySQL server running locally

## Setup Instructions
### Database Setup
**1**. Start your MySQL server.
**2**. Create the database:
` CREATE DATABASE kudosdb; `
**3**. Check backend/src/main/resources/application.properties for your database credentials:

` spring.datasource.url=jdbc:mysql://localhost:3306/kudosdb `

` spring.datasource.username=root `

` spring.datasource.password=yourpassword `

_Adjust username and password if your MySQL credentials are different._

## Backend Setup

**1**. Open a terminal and navigate to the backend directory:

` cd backend `

**2**. Build the project and download dependencies:

` mvn clean install `

- This compiles the code and ensures the backend can connect to the database.

**3**. Run the backend server:

` mvn spring-boot:run `

- Spring Boot will start on http://localhost:8080
- Look for ` [INFO] Started Application ` in the console to confirm it is running.


## Frontend Setup

**1**. Open another terminal and navigate to the frontend folder:

` cd frontend `


**2**. Install dependencies:

` npm install `


**3**. Start the frontend server:

` npm start `

- The frontend will run on http://localhost:3000

- Ensure the backend is running first, so the frontend can make API requests.

## Running the Application

**1**. Start MySQL server.

**2**. Run backend:
` cd backend `
` mvn spring-boot:run `

**3**. Run frontend:

` cd frontend `
` npm start `

**4**. Open your browser and go to http://localhost:3000


### Notes

- Admin credentials can be initialized in application.properties:

` admin.default.email=admin@kudos.com `

` admin.default.password=admin123 `

- If using a new machine, ensure MySQL is installed and the database kudosdb exists.

- Use ` mvn clean install ` after any backend code changes to refresh dependencies.

- React frontend must be started from the ` frontend ` folder (` cd frontend && npm start `).

# Swagger API Documentation

The project includes a Swagger UI for testing and exploring the backend APIs.

## Access Swagger UI

Make sure your **backend is running** first:

` mvn spring-boot:run `

Then open:

http://localhost:8080/swagger-ui/index.html#/

## Authorization

Some endpoints require authentication (Admin/Employee). To test these:

**1. Login first:** Use the ` /api/auth/login ` endpoint.

**2. Request Body Format:**

` { `

`    "email": "string", `

`    "password": "string" `

` } `

**3.** Copy the tokey key from the response **(just the raw text, no quotes).**

**4.** Click **Authorize** at the top right of Swagger UI and paste the token.

Once authorized, you can test all endpoints accessible for your role (Admin or Employee).

## Test an API

**1.** Click on the API you want to test.

**2.** Click **Try it out.**

**3.** Provide the required request body (if applicable) or fill in the form fields.

**4.** Click **Execute** and observe the response.

### Note:

Ensure the backend is running and the database is connected before testing endpoints.

