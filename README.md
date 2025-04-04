# Kyoka Food - Online Food Ordering System

## 🎯 Project Overview
Kyoka Food is a comprehensive full-stack web application for online food ordering featuring secure user authentication, role-based authorization, and real-time order management.

The project consists of two main repositories:
- Backend Repository: [`Backend`][backend]
- Frontend Repository: [`Frontend`][frontend]

Try application here:
[`Food Odering System`][demo]

[backend]: https://github.com/Kyoka-run/Food-Ordering-Backend
[frontend]: https://github.com/Kyoka-run/Food-Ordering-Frontend
[demo]: http://kyoka-food-ordering-system.s3-website-eu-west-1.amazonaws.com

## ⚙️ Technology Stack

### Back-end
- **Framework:** Spring Boot 3.4.1
- **Security:** Spring Security with JWT
- **Database:** MySQL 8.0 with JPA/Hibernate, RDS for AWS deployment
- **Payment Processing:** Stripe API
- **Testing:** JUnit, Mockito, JaCoCo
- **Build Tool:** Maven

### Front-end
- **Framework:** React 18.3.1 with Vite 6.0.5
- **State Management:** Redux Toolkit
- **UI Components:** Material UI 6.4.2, Tailwind CSS 3.4.0
- **Form Handling:** Formik 2.4.6 / React Hook Form 7.54.2
- **Validation:** Yup 1.6.1
- **HTTP Client:** Axios 1.7.9
- **Routing:** React Router DOM 7.1.3
- **Testing:** Vitest 3.0.7, React Testing Library 16.2.0
- **UI Notifications:** React Hot Toast 2.5.2

### DevOps & Deployment
- **Containerization:** Docker
- **Version Control:** Git
- **CI/CD Pipeline:** Jenkins
- **Cloud Infrastructure:** AWS (EC2, S3)
- **Deployment:** Docker containers orchestrated with Jenkins

## ✨ Key Features

### User Authentication & Authorization
- **JWT-based authentication**
- **Role-based access control (ADMIN, RESTAURANT_OWNER, CUSTOMER)**

### Restaurant Management
- **Restaurant CRUD operations**
- **Menu management with categories and ingredients**
- **Event creation and management**

### Customer Experience
- **Food browsing and searching**
- **Real-time cart management**
- **Customizable food orders with ingredients selection**
- **Order tracking and history**
- **Address management for delivery**
- **Restaurant favorite system**

### Order Processing
- **Integrated Stripe payment processing**
- **Restaurant order management interface**


## 📊 Testing and Code Quality

- Backend test with Junit and Mockito
- Frontend component testing with Vitest and React Testing Library
- Integration tests for API endpoints

## 🛠️ Installation & Setup

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Docker and Docker Compose (optional)

### Backend Setup
1. Clone the repository:
```bash
git clone https://github.com/Kyoka-run/food-ordering.git
cd food-ordering
```

2. Set up the MySQL database:
```sql
CREATE DATABASE kyoka_food_order;
```

3. Update the application.properties file with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

4. Configure Stripe API key:
```properties
stripe.api.key=YOUR_STRIPE_API_KEY
```

5. Build and run the application:
```bash
mvn clean install
java -jar target/food-ordering-0.0.1-SNAPSHOT.jar
```

### Frontend Setup
1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Configure API endpoint:
```javascript
// Update API_URL in api.js if needed
export const API_URL = "http://localhost:8080/api";
```

4. Start development server:
```bash
npm run dev
```

## 🖼️ Application Screenshots

### Customer Interface
- Restaurant Browsing
- Food Ordering Process
- Cart Management
- Order History

### Restaurant Owner Dashboard
- Menu Management
- Order Processing
- Event Creation
- Restaurant Settings

### Admin Interface
- User Management
- Restaurant Oversight
- System Configuration