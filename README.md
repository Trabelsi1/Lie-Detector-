# Lie Detector - Two Truths and a Lie

## Project Idea
Lie Detector is a multiplayer web application based on the classic social game **Two Truths and a Lie**.

Each player joins a shared game room, submits three statements (two true, one false), and other players try to detect the lie. The game is designed to be social, fast to play, and suitable for short rounds with multiple participants.

### Core gameplay vision
- Players create or join a room using a room code.
- Every round, a player submits three statements.
- Other players vote on which statement is the lie.
- The system evaluates answers and tracks round outcomes.

The long-term goal is a complete multiplayer experience with clear room management, round progression, and interactive feedback for all players.

## Architecture
The project follows a classic **MVC architecture** with:
- **Model**: domain entities and persistence layer (JPA)
- **View**: JSP pages for user interfaces
- **Controller**: Servlets handling HTTP requests and application flow

### MVC responsibilities in this project
- **Model**
  - Represents core business objects and relationships.
  - Handles persistence through Spring Data JPA repositories.
  - Exposes business operations through service classes.

- **Controller (Servlet layer)**
  - Receives client requests (create room, join room, submit content, vote).
  - Calls services for business actions.
  - Forwards data to JSP views.

- **View (JSP layer)**
  - Displays pages for room actions and game interaction.
  - Renders state sent by servlet controllers.


## Technology Stack
- Java
- Spring framework
- Spring Data JPA
- Maven
- Jakarta Persistence API
- Servlet + JSP 

## What Is Implemented at This Stage
Current implementation focuses on the **backend domain foundation**.

### 1. JPA entities
- `User`
  - Fields: `id`, `username`, `email`
- `GameRoom`
  - Fields: `id`, `roomCode`, `status`

### 2. Relationship modeling
- Many-to-many association between users and rooms:
  - A user can join multiple game rooms.
  - A game room can contain multiple users.

### 3. Persistence layer
- `UserRepository` extending `JpaRepository<User, Long>`
- `GameRoomRepository` extending `JpaRepository<GameRoom, Long>`

### 4. Service layer
- `UserService`
  - `createUser(User user)`
  - `getAllUsers()`
  - `getUserById(Long id)`
- `GameRoomService`
  - `createRoom(GameRoom room)`
  - `getAllRooms()`
  - `getRoomById(Long id)`
  - `addUserToRoom(Long roomId, Long userId)`


## Planned Next Steps
- Implement servlet controllers for room and game actions.
- Create JSP pages for player interactions.
- Add gameplay domain parts (rounds, statements, votes, scoring).
- Connect end-to-end flow from HTTP request to persisted game state and rendered UI.
- Add real-time and multiplayer interaction improvements.

## Quick Summary
Lie Detector is being built as an MVC web app for the Two Truths and a Lie game. The current stage establishes the model and backend foundations (entities, relationships, repositories, and services), preparing the project for the next phase: servlet controllers, JSP views, and full gameplay logic.
