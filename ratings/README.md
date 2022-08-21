# Rating service

This service is responsible for generating and submitting surveys and processing them to calculate rating for Doctors

Surveys are processed once a day with scheduled job. Everything the job is run all survers get `processed` filed set
to `true` rating is then saved to `Rating` table and sent to `scheduling-service` when `Doctor` rating is updated.

## Survey

### Submit

User can only submit a Survey from Appointment he/she was a patient. If you are tyring to submit Survey you are not
patient the endpoint will return an error.

## Model

### Survey

| Field            | Type          |
|------------------|---------------|
| id (PK)          | Long          |
| appointment (UK) | Long          |
| patient          | Long          |
| startTime        | LocalDateTime |
| endTime          | LocalDateTime |
| processed        | Boolean       |
| rating           | Int (1-5)     |

### Rating

| Field                | Type          |
|----------------------|---------------|
| id (PK)              | Long          |
| doctor (UK)          | Long          |
| rating               | BigDecimal    |
| totalNumberOfRatings | Int           |
