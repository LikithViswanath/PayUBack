# RESTful-Microservices

• Developed a Social Media API enabling friends to connect, lend money to each other, and schedule reminders for repayments.

• Utilized microservice architecture and Reactive Spring Boot for lighting speed performance by leveraging Spring Boot WebFlux and Mono for reactive programming.

• Implemented Eureka server for service discovery and utilized MongoDB as the primary database in the reactive environment.

## Tech Stack

- `Backend Framework:` `Spring Boot`
- `Databases:` `MongoDb`
- `Service Discovery:` `Eureka Server, Eureka Client`


## System Design

![image](https://github.com/LikithViswanath/PayUBack/blob/main/PayUBack.png)


- `GateWay Service:` ` Managed API gateway functionality for routing requests`
- `Discovery Service:` `Eureka server for service discovery and registration.`
- `Auth Service:` `authentication and authorization mechanisms for securing access to services.`
- `User Service:` ` user management functionalities`
- `Schedule Service:` `cron scheduling EMAIL reminder if and when needed`
- `Transaction Service:` `Facilitating Lending between users ans scheduling EMAIl reminders`

## Installation

Once all the service Dockerized as mentioned in docker-compose.yaml file, run the following command

```bash
  docker compose up
```

### API Endpoints


#### Register as A User
```javascript
  POST http://localhost:9002/auth/register/newuser@example.com
```
#### Payload

```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "firstName": "New",
  "lastName": "User",
  "roles": ["USER","LENDER"],
  "phoneNumber": "1234567890"
}
```

#### Example Response
```json 
{
  "email": "newuser@example.com",
  "pendingRequests": [],
  "connections": [],
  "connectionRequests": []
}
```

#### User Login
```javascript
  POST http://localhost:9002/auth/login/user@example.com
```
#### Payload

```json
{
  "email": "user@example.com",
  "password": "password123",
  "roles": ["USER"]
}
```

#### Example Response
```json 
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
}

```

#### Get User Details
```javascript
  GET http://localhost:9002/user/user1@example.com
```
#### Example Response

```json
{
  "email": "user1@example.com",
  "pendingRequests": [],
  "connections": [],
  "connectionRequests": []
}
```

#### Send Connect Request Between Users from Email to Email
```javascript
  POST http://localhost:9002/user/user2@example.com/user1@example.com
```
#### Example Response

```json
{
  "email": "user1@example.com",
  "pendingRequests": ["user2@example.com"],
  "connections": [],
  "connectionRequests": ["user3@example.com"]
}
```

#### Accept Connection Request Between Users from Email to Email
```javascript
  POST http://localhost:9002/user/user1@example.com/user2@example.com
```
#### Example Response

```json
{
  "email": "user1@example.com",
  "pendingRequests": [],
  "connections": ["user2@example.com"],
  "connectionRequests": ["user3@example.com"]
}
```

#### Remove Connection Request or Connection Between Users from Email to Email
```javascript
  DELETE http://localhost:9002/user/user2@example.com/user1@example.com
```
#### Example Response

```json
{
  "email": "user1@example.com",
  "pendingRequests": [],
  "connections": [],
  "connectionRequests": ["user3@example.com"]
}
```


#### To initiate a transaction create a register and lend money
```javascript
  POST http://localhost:9002/transaction/init-transaction-user
```

### Payload
```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Example Response
```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "borrowRequests": [],
  "pendingBorrowRequests": [],
  "acceptedBorrowRequests": []
}

```

#### Send borrow request (Before asking money we need to send a borrow request From Email to Email)
```javascript
  POST http://localhost:9002/transaction/send-borrow-request/user2@example.com/user1@example.com
```

### Payload
```json
{
  "amount": 100.0,
  "days": 30,
  "interest": 5.0
}
```

#### Example Response
```json
{
  "uniqueId": "1234567890",
  "fromName": "John Doe",
  "toName": "Jane Smith",
  "transactionId": {
    "toEmail": "user2@example.com",
    "fromEmail": "user1@example.com"
  },
  "amount": 100.0,
  "amountPayed": 0.0,
  "amountPayedBack": 0.0,
  "days": 30,
  "interest": 5.0
}
```

#### Accept borrow request, a corn is sent to run after the set amount of days in the request ( Before sending money we need to accept a borrow request From Email to Email )
```javascript
  POST http://localhost:9002/transaction/send-borrow-request/user1@example.com/user2@example.com
```

#### Example Response
```json
{
  "email": "user2@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "borrowRequests": [],
  "pendingBorrowRequests": [],
  "acceptedBorrowRequests": [
    {
      "uniqueId": "1234567890",
      "name": "Jane Smith",
      "email": "user1@example.com",
      "amount": 100.0,
      "days": 30,
      "interest": 5.0
    }
  ]
}

```

#### Remove pending borrow request or accepted borrow request
```javascript
  POST http://localhost:9002/transaction/remove-borrow-request/user1@example.com/user2@example.com
```

#### Example Response
```json
{
  "email": "user2@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "borrowRequests": [],
  "pendingBorrowRequests": [],
  "acceptedBorrowRequests": []
}
```

#### Pay the lender ( Once the total amount is payed the reminders are deleted )
```javascript
  POST http://localhost:9002/transaction//pay-amount/user1@example.com/user2@example.com/1234567890
```

#### Example Response
```json
{
  "email": "user2@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "borrowRequests": [],
  "pendingBorrowRequests": [],
  "acceptedBorrowRequests": []
}
```



