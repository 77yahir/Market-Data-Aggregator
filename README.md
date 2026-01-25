# Market Data Aggregator

A Spring Boot backend service that aggregates market price data from multiple sources, filters invalid data (stale or outliers), and exposes REST APIs to retrieve the best available price per symbol.

This project emphasizes backend reliability, API design, and test-driven development.

---

## Features

- Aggregates price data from multiple simulated sources  
- Filters stale, outlier, and invalid price ticks  
- Exposes REST APIs with proper HTTP semantics (`200`, `400`, `404`)  
- Returns consistent JSON error messages  
- Deterministic time handling via injected `Clock`  
- Comprehensive controller tests using `MockMvc` and Mockito  

---

## Architecture

Controller → Service → Aggregator → Price Sources

- **Controller**: Handles HTTP requests, validation, and response formatting  
- **Service**: Coordinates polling and querying logic  
- **Aggregator**: Maintains in-memory best prices per symbol and applies business rules  
- **Price Sources**: Simulated sources producing normal, stale, or outlier prices  

---

## API Endpoints

### Get best price for a symbol

GET /prices/{symbol}

- `200 OK` – returns best price  
- `400 Bad Request` – invalid symbol  
- `404 Not Found` – symbol not found  

---

### Get all best prices

GET /prices

- `200 OK` – returns list of prices (empty list if none exist)

---

### Poll a symbol for updated prices

POST /poll/{symbol}

- `200 OK` – polling succeeded and best price returned  
- `400 Bad Request` – invalid symbol  
- `404 Not Found` – no price available after polling  

---

## Testing

- **JUnit 5**
- **Mockito**
- **Spring MockMvc**

Tests validate:
- HTTP status codes and JSON responses  
- Error handling behavior  
- Controller-to-service interaction  
- Deterministic timestamps (no real-time dependency)  

---

## Running the Project

**Requirements**
- Java 17+
- Maven

Run tests:
```bash
mvn test
```

Run application:
```bash
mvn spring-boot:run
```

Service runs at:
```code
http://localhost:8080
```
