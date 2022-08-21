# Scheduling service

Scheduling service manage Doctors, Timeslot and Appointments.
Doctor can have multiple Timeslots, when a user book given Timeslot an Appointment is created and Timeslot is no longer
available to book.

Everytime an Appointment is created or deleted an event is sent to `Ratings Service` where the survey is
generated/deleted. This is done via `RabbitMQ` broker.

## Doctors

### Create

Endpoint: `/api/doctors` (POST)

Role: `DOCTOR`

Body:

```json
{
  "name": "string",
  "specialties": [
    1,
    2
  ]
}
```

This endpoint creates a new Doctor, linked to authenticated `User`. If authenticated user does not have `DOCOR` role
the endpoint will return `403 Forbidden` error.

### List

Endpoint: `/api/doctors` (GET)

Role: `ADMIN`

This endpoint returns all Doctors in the system.

## Timeslots

Timeslots have the following structure:

```json
{
  "doctor": "doctor_id",
  "startTime": "LocalDateTime",
  "endTime": "LocalDateTime",
  "free": "Boolean"
}
```

Each Timeslot has its own start and end time, meaning the slot is available during this time ranges. Timeslot also have
a `free` field which is set to false everytime an appointment with given timeslot is created.

### Create

Endpoint: `/api/timeslots` (POST)

Role: `DOCTOR`

Body:

```json
[
  {
    "startTime": "LocalDateTime (yyyy-MM-dd hh:mm:ss)",
    "endTime": "LocalDateTime (yyyy-MM-dd hh:mm:ss)"
  }
]
```

You can create new Timeslot with the following payload. Note that `startTime` and `endTime` date needs to be in the
future and Timeslots can not overlap one each other.

### List

Endpoint: `/api/timeslots` (GET)

Filters:

```
/api/timeslots?doctor=EXTERNAL_1&startDate=2022-08-21&endDate=2022-08-22
```

This endpoint returns all available Timeslots for given filters.

## Appointments

### Create

Endpoint: `/api/appointments` (POST)

Body:

```json
{
  "timeslot": "string (EXTERNAL_ID)"
}
```

This endpoint creates a new Appointment for the provided Timeslot in the body for authenticated `User`. If timeslot is already
taken the endpoint will return an error.

### Cancel

Endpoint: `/api/appointments/{id}/cancel` (POST)

This endpoint cancels Appointment by id. It also updates Timeslot to be `free` again and available to book
again. Note that you can not cancel Appointment starting within one day.

### List
Endpoint: `/api/appointments` (GET)

Role: `ADMIN`

This endpoint lists all the Appointments in the system.

## Model

### Specialty

| Field        | Type          |
|--------------|---------------|
| id (PK)      | Long          |
| name         | String        |
| description  | String        |
| doctors      | List<Doctor>  |
| createDate   | LocalDateTime |
| modifiedDate | LocalDateTime |

### Doctor

| Field        | Type            |
|--------------|-----------------|
| id (PK)      | Long            |
| user         | Long            |
| rating       | BigDecimal      |
| timeslots    | List<Timeslot>  |
| specialties  | List<Specialty> |
| createDate   | LocalDateTime   |
| modifiedDate | LocalDateTime   |

### Timeslot

| Field        | Type          |
|--------------|---------------|
| id (PK)      | Long          |
| doctor       | Doctor        |
| startTime    | LocalDateTime |
| free         | Boolean       |
| appointment  | Appointment   |

### Appointment

| Field        | Type          |
|--------------|---------------|
| id (PK)      | Long          |
| patient      | Long          |
| timeslot     | Timeslot      |
| createDate   | LocalDateTime |
| modifiedDate | LocalDateTime |
