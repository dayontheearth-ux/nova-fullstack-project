# NOVA — Team Productivity Platform

Full-stack project management application for the Full Stack Development Intern Assignment.

## Stack
- Frontend: React + Vite
- Backend: Java Spring Boot
- Database: MySQL
- API: REST
- Authentication: JWT
- ORM: Spring Data JPA / Hibernate

## Features
- Register and login
- JWT-protected API
- Dashboard statistics
- Create/update/delete projects
- Create/update/delete tasks
- Task status and priority
- Assign tasks to project members
- Project progress tracking
- Team/member management
- Responsive UI

## 1. Database

Create a MySQL database:

```sql
CREATE DATABASE nova_db;
```

Then edit:

`backend/src/main/resources/application.properties`

Set your MySQL username/password.

## 2. Run backend

Requirements: Java 17+ and Maven.

```bash
cd backend
mvn spring-boot:run
```

Backend:

`http://localhost:8080`

## 3. Run frontend

Requirements: Node.js 18+.

```bash
cd frontend
npm install
npm run dev
```

Frontend:

`http://localhost:5173`

## Demo flow

1. Register a new account.
2. Login.
3. Create a project.
4. Open the project.
5. Add team members.
6. Create tasks and assign them.
7. Change task status.
8. View progress on the dashboard.

## API base URL

`http://localhost:8080/api`
