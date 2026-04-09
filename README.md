# Accommodation Finder - Web Application

A web-based accommodation finder application that allows users to search for accommodations (PGs and Flats) near campus based on distance, type, rent, and rating.

## Project Structure

```
/workspace
├── server.js              # Node.js backend server
├── package.json           # Node.js dependencies
├── public/
│   ├── index.html         # Frontend HTML
│   ├── styles.css         # CSS styling
│   └── app.js             # Frontend JavaScript
└── MainApp.java          # Original Java implementation (reference)
```

## Features

- **User Authentication**: Signup and Login functionality
- **Add Accommodations**: Add PGs or Flats with details (name, location, type, rent, rating)
- **Search**: Filter accommodations by radius (distance from campus) and type
- **Distance Calculation**: Uses Haversine formula to calculate distances (same as Java version)
- **Responsive Design**: Works on desktop and mobile devices

## Campus Location (Default)
- Latitude: 30.3165
- Longitude: 78.0322

## How to Run

### Prerequisites
- Node.js installed (v14 or higher recommended)

### Installation & Running

1. Navigate to the workspace directory:
   ```bash
   cd /workspace
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the server:
   ```bash
   npm start
   ```

4. Open your browser and go to:
   ```
   http://localhost:3000
   ```

## Usage Flow

1. **Signup**: Create a new account with username and password
2. **Login**: Enter your credentials to access the application
3. **Add Accommodations**: Fill in the accommodation details form
4. **Search**: Select radius and type to find matching accommodations
5. **View Results**: See all matching accommodations with distance from campus
6. **Logout**: End your session

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/signup` | Register a new user |
| POST | `/api/login` | Authenticate user |
| POST | `/api/accommodations` | Add a single accommodation |
| POST | `/api/accommodations/bulk` | Add multiple accommodations |
| POST | `/api/search` | Search accommodations by radius and type |
| GET | `/api/accommodations` | Get all accommodations |
| POST | `/api/reset` | Clear all accommodation data |

## Technology Stack

- **Backend**: Node.js with Express.js
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **API**: RESTful API with JSON responses
- **Styling**: Custom CSS with responsive design

## Notes

- This is a demonstration application with in-memory storage (data resets on server restart)
- The distance calculation uses the same Haversine formula as the original Java implementation
- All coordinates use the campus location at (30.3165, 78.0322) as the reference point
