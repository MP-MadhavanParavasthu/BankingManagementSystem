# Banking Management System
A secure Banking Management System REST API developed using **Java, Spring Boot, Spring Security, Spring Data JPA, and MySQL**.

## Project Overview
This project demonstrates the development of a backend banking application supporting customer registration, account management, banking transactions, and user authentication. It follows a layered architecture using controllers, services, repositories, and entities.

## Technologies Used
- **Java** — Backend programming language
- **Spring Boot** — REST API development
- **Spring Security** — Authentication and access control
- **Spring Data JPA / Hibernate** — Database operations
- **MySQL** — Relational database
- **Maven** — Dependency management
- **JUnit and Mockito** — Unit testing
- **Git and GitHub** — Version control

## Features
- Customer registration
- User authentication using Spring Security
- Secure password hashing with BCrypt
- Bank account management
- Deposit and withdrawal operations
- Money transfers between accounts
- Transaction history
- Account ownership authorization
- Exception handling
- Unit testing

## Project Architecture
The application follows a layered architecture:
**Client → REST Controller → Service Layer → Repository Layer → MySQL Database**
Spring Security handles authentication and access control.

## Project Structure
`src/main/java/com/bms/banking/`
- `config/` — Security configuration
- `controller/` — REST API endpoints
- `entity/` — Database entities
- `exception/` — Global exception handling
- `repository/` — Database access
- `service/` — Business logic
- `BankingManagementSystemApplication.java` — Application entry point

## Security
- Spring Security authentication
- BCrypt password hashing
- Account ownership checks
- Protected banking operations

## Running the Project
### Prerequisites
- Java JDK compatible with the project's Spring Boot version
- MySQL Server
- Maven or Maven Wrapper
- IntelliJ IDEA or another Java IDE

### Setup
1. Clone the repository.
2. Create a MySQL database named `banking_db`.
3. Configure your database connection in `src/main/resources/application.properties`.
4. Run the application using `.\mvnw.cmd spring-boot:run`.
5. Access the backend at `http://localhost:8080`.

**Note:** The local database configuration is excluded from Git to protect credentials.

## Testing
Run the automated tests using `.\mvnw.cmd test`.

## Future Improvements
- Add Swagger/OpenAPI documentation
- Implement JWT authentication
- Containerize the application using Docker
- Deploy the backend to AWS
- Add automated CI/CD using GitHub Actions
- Develop a frontend dashboard

## Author
[Madhavan Paravasthu](https://github.com/MP-MadhavanParavasthu)
