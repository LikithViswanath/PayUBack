# RESTful-Microservices

• A RESTful Microservices, built using Spring Boot which has Product-Service, Order-Service and Inventory-Service.

• Used Eureka server for service discovery and MongoDB and MySql as the database.

• Used KeyCloak for identity and access management, and Spring Cloud Sleuth and Zipkin for distributed tracing.


## Tech Stack

- `Backend Framework:` `Spring Boot`
- `Databases:` `MongoDb`
- `Service Discovery:` `Eureka Server, Eureka Client`


## System Design

![image](https://github.com/LikithViswanath/PayUBack/blob/main/PayUBack.png)

## Installation

Keycloak Docker Image

```bash
  docker run -p 8180:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:20.0.1 start-dev
```

### API Endpoints


#### Get Available Products
```javascript
  GET http://localhost:8080/api/product
```
#### Example Response
```json 
[
  {
    "id": "6364ea69906ea11f14e5b2f3",
    "name": "Iphone 13",
    "description": "Iphone 13",
    "price": 120000
  },
  {
    "id": "6364ea94906ea11f14e5b2f4",
    "name": "Legion 5",
    "description": "Legion 5",
    "price": 80000
  },
  {
    "id": "6368c04c3e806727453333eb",
    "name": "Hp Pavilion",
    "description": "Hp Pavilion",
    "price": 50000
  },
  {
    "id": "636a489d65f8da7270084425",
    "name": "Dell Vostro",
    "description": "Dell Vostro",
    "price": 60000
  }
]
```

#### Add Product
```javascript
  Post http://localhost:8080/api/product
```
```json 
{
    "name": "Dell Vostro",
    "description": "Dell Vostro Laptop",
    "price": 60000
}
```
#### Example Response
```
201 Created
```

#### Place a order


```javascript
  Post http://localhost:8080/api/order
```

```json 
{
    "orderItemsDtoList":[
        {
            "skuCode":"iphone_13",
            "price":120000,
            "quantity":1
        }
    ]
}
```
#### Example Response

```
Order Placed Successfully
```
#### Update Inventory
```javascript
  Post http://localhost:8080/api/inventory
```

```json 
{
    "skuCode":"dell_vostro",
    "quantity":10
}
```
#### Example Response
```
Inventory Successfully Updated
```

