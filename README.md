# Medifit

## Micro Services

### api-gateway

Routing

### auth

User and JWT management

[Auth service documentation](auth/README.md)

### scheduling

Doctor, Timeslots and appointments management.

[Scheduling service documentation](scheduling/README.md)

### ratings

Survey and Ratings management.

[Ratings service documentation](ratings/README.md)

## How to setup/start the app

For running the app you need to have `docker-compose` installed on your computer, also make sure that the port `8080` is
not in use.

Once you have `docer-compose` installed on your machine simply run the following command in this projects `root`
```
docker-compose up
```
Once you run the command the docker will pull and start `postgres`, `consul` and `rabbitmq` from the DockerHub. 

Docker will also compile and start all the services and expose `api-gateway` on port `8080`

## Technologies
### Code

- Kotlin
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring AMPQ
- Spring Cloud (Consul, Gateway)

### Testing

- Mockk
- TestContainers

### Database

- Postgresql
- H2 (for testing)
- Flyway (database migration tool)

### DevOps

- Docker
- RabbitMQ
- Consul
