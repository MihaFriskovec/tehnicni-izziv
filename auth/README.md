# Auth Service

This service is responsible for managing users (signup) and issuing JWT tokens (login)

## Sing up

Endpoint: `/api/auth/signup` (POST)

Body:

```json
{
  "username": "string",
  "password": "string",
  "email": "string (optional)",
  "role": "string (optional"
}
```

Endpoint returns `201 CREATED` if the signup is successful.
If there is and error the endpoint returns `4xx` status code for bad input or `5xx`
for server errors.

## Login

Endpoint: `/api/auth/login` (POST)

Body:

```json
{
  "username": "string",
  "password": "string"
}
```

If the login is successful the endpoint returns a signed JWT token that can be used to authenticate the user or
return respected error for unsuccessful logins.

# UserInfo
Endpoint: `/api/users/me`

This endpoint return currently authenticated `User`

Do not forget to include the `Authorization` headers.

```
'Authorization`: 'Bearer ${JWT}'
```

## Model

### User

| Field         | Type                          |
|---------------|-------------------------------|
| id (PK)       | Long                          |
| email (UK)    | String                        |
| username (UK) | String                        |
| password      | String                        |
| active        | Boolean                       |
| role (enum)   | String (Patient/Doctor/Admin) |
| createDate    | LocalDateTime                 |
| modifiedDate  | LocalDateTime                 |

