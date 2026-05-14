# Carthage GG

## Project Overview

Carthage GG is a Symfony-based web application with a Python microservice for face recognition functionality.

## Project Structure

```
Carthage_GG/
├── .composer/                 # Composer cache directory
├── assets/                    # Frontend assets
├── config/                    # Symfony configuration files
├── docker/                    # Docker configuration
│   ├── nginx/                 # Nginx configuration
│   └── php/                   # PHP Dockerfile
├── migrations/                # Database migrations
├── public/                    # Public web directory
├── src/                       # Symfony source code
│   ├── ApiResource/           # API resources
│   ├── Controller/            # Controllers
│   ├── Entity/                # Doctrine entities
│   └── Repository/            # Repositories
├── templates/                 # Twig templates
├── translations/              # Translation files
├── var/                       # Cache and logs (not tracked)
├── vendor/                    # Composer dependencies (not tracked)
├── .gitignore                 # Git ignore rules
├── docker-compose.yml         # Docker Compose configuration
├── Dockerfile                 # Python microservice Dockerfile
├── face_api.py                # Python face recognition microservice
├── requirements.txt           # Python dependencies
└── README.md                  # This file
```

## Services

This project uses Docker Compose to run the following services:

- **PHP**: Symfony application backend
- **Nginx**: Web server
- **MySQL**: Database
- **phpMyAdmin**: Database management interface
- **MailHog**: Email testing tool
- **Python API**: Flask-based face recognition microservice

## Getting Started

### Prerequisites

- Docker and Docker Compose installed

### Installation

1. Clone or download the project
2. Navigate to the project directory

### Starting All Services

```bash
docker-compose up --build
```

This will start all services, including:
- Symfony app at http://localhost:8088
- phpMyAdmin at http://localhost:8081
- MailHog at http://localhost:8025
- Python API at http://localhost:5000

### Starting Only the Python Microservice

#### With Docker Compose

```bash
docker-compose up --build python-api
```

#### Without Docker

1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

2. Run the service:
   ```bash
   python face_api.py
   ```

## Python Microservice Endpoints

### POST /extract
Extracts a face descriptor from an image.

**Request Body:**
```json
{
  "image": "base64_encoded_image_data"
}
```

**Response:**
```json
{
  "descriptor": [0.1, 0.2, ...]  // 128-dimensional vector
}
```

## Database Credentials

- **Database Name**: carthagegg
- **Username**: carthage
- **Password**: carthage
- **Root Password**: root
- **Port**: 3307 (host) → 3306 (container)
