# Lie Detector Arena

## Project Idea
Lie Detector Arena is a multiplayer web application inspired by **Two Truths and a Lie**.

Players join a room, take part in rounds, submit statements, discuss them, and try to identify the lie. The long-term goal is to build a complete multiplayer experience with room management, round progression, voting, scoring, and player statistics.

## Current Technical Direction
The project is now built as a **Spring Boot backend exposing a REST API**.

This is a change from the previous `Servlet + JSP` direction. The backend is responsible for:
- domain modeling
- persistence with JPA
- business logic through services
- HTTP endpoints through `RestController`

A JavaScript frontend may be added later and will consume the REST API.

## Technology Stack
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Web
- Maven
- Jakarta Persistence API
- HSQLDB runtime dependency

## Current Architecture
The project currently follows this structure:
- **Entity layer**: domain model of the game
- **Repository layer**: Spring Data JPA repositories
- **Service layer**: business logic and orchestration
- **Controller layer**: REST endpoints returning JSON

## Domain Model
The current model includes the following main entities:
- `User`: application account
- `Player`: gameplay participant derived from a user account
- `PlayerProfile`: gameplay statistics and behavioral indicators for a player
- `GameRoom`
- `Invitation`
- `Game`
- `Round`
- `Statement`
- `Vote`
- `ChatMessage`
- `ScoreEntry`

### Important Modeling Choice
`User` and `Player` are now separated.

- A `User` represents an application account.
- A `User` is **not necessarily** a player.
- A `Player` is created when a user actually participates in the gameplay layer.
- `PlayerProfile` stores the gameplay statistics of a `Player`.

This means a user can exist first, then become a player later.

## What Is Implemented

### 1. Entities
The backend already contains a richer domain model than in the first iteration:
- user accounts
- players and player profiles
- game rooms
- invitations
- games and rounds
- statements
- votes
- chat messages
- score entries

### 2. Repositories
The project already contains repositories for persistence queries, including:
- `UserRepository`
- `PlayerRepository`
- `GameRoomRepository`
- `RoundRepository`
- `StatementRepository`
- `VoteRepository`

### 3. Services
Current service layer:
- `UserService`
  - `createUser(User user)`
  - `getAllUsers()`
  - `getUserById(Long id)`
- `GameRoomService`
  - `createRoom(GameRoom room)`
  - `getAllRooms()`
  - `getRoomById(Long id)`
  - `addUserToRoom(Long roomId, Long userId)`

When a user is added to a room, the service can create the corresponding `Player` and `PlayerProfile` if they do not exist yet.

### 4. REST Controllers
The first REST controllers are already implemented:
- `UserController`
- `GameRoomController`

Current available endpoints:
- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/rooms`
- `GET /api/rooms`
- `GET /api/rooms/{id}`
- `POST /api/rooms/{roomId}/users/{userId}`

## What Is Not Done Yet
- full gameplay service layer for all entities
- dedicated controllers for rounds, statements, votes, chat, scores, and invitations
- DTOs and validation
- authentication / security
- frontend JavaScript integration
- real-time multiplayer features
- complete test coverage

## Running the Project
To compile the project:

```bash
./mvnw compile
```

To run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

## Next Steps
- extend the REST API to the remaining gameplay entities
- improve request/response design with DTOs
- add validation and error handling
- connect a JavaScript frontend
- implement tests
- refine multiplayer game flow

## Summary
Lie Detector Arena is currently a Spring Boot REST backend with a structured domain model and first working API endpoints. The project has moved away from the old `Servlet + JSP` strategy and now prepares for a cleaner backend/API-first architecture.
