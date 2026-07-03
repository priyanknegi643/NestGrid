const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, 'public')));

// In-memory storage (simulating the Java repository)
let users = [];
let accommodations = [];

// Campus coordinates (from Java code)
const CAMPUS_LAT = 30.3165;
const CAMPUS_LON = 78.0322;

// Helper function to calculate distance (Haversine formula)
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Earth radius in km
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = 
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

function toRad(degrees) {
    return degrees * Math.PI / 180;
}

// API Routes

// Signup
app.post('/api/signup', (req, res) => {
    const { username, password } = req.body;
    
    const existingUser = users.find(u => u.username === username);
    if (existingUser) {
        return res.json({ success: false, message: 'User already exists!' });
    }
    
    users.push({ username, password });
    res.json({ success: true, message: 'Signup successful!' });
});

// Login
app.post('/api/login', (req, res) => {
    const { username, password } = req.body;
    
    const user = users.find(u => u.username === username && u.password === password);
    if (user) {
        res.json({ success: true, message: 'Login successful!', username });
    } else {
        res.json({ success: false, message: 'Invalid credentials!' });
    }
});

// Add Accommodation
app.post('/api/accommodations', (req, res) => {
    const accommodation = req.body;
    accommodation.distance = calculateDistance(CAMPUS_LAT, CAMPUS_LON, accommodation.lat, accommodation.lon);
    accommodations.push(accommodation);
    res.json({ success: true, message: 'Accommodation added!', data: accommodation });
});

// Add Multiple Accommodations
app.post('/api/accommodations/bulk', (req, res) => {
    const accommodationList = req.body.accommodations || [];
    const results = accommodationList.map(acc => {
        acc.distance = calculateDistance(CAMPUS_LAT, CAMPUS_LON, acc.lat, acc.lon);
        accommodations.push(acc);
        return acc;
    });
    res.json({ success: true, message: `${results.length} accommodations added!`, data: results });
});

// Search Accommodations
app.post('/api/search', (req, res) => {
    const { radius, type } = req.body;
    
    const results = accommodations.filter(acc => {
        const distance = calculateDistance(CAMPUS_LAT, CAMPUS_LON, acc.lat, acc.lon);
        return distance <= radius && acc.type.toLowerCase() === type.toLowerCase();
    }).map(acc => ({
        ...acc,
        distance: calculateDistance(CAMPUS_LAT, CAMPUS_LON, acc.lat, acc.lon)
    }));
    
    res.json({ success: true, data: results });
});

// Get all accommodations
app.get('/api/accommodations', (req, res) => {
    const results = accommodations.map(acc => ({
        ...acc,
        distance: calculateDistance(CAMPUS_LAT, CAMPUS_LON, acc.lat, acc.lon)
    }));
    res.json({ success: true, data: results });
});

// Clear all data (for testing)
app.post('/api/reset', (req, res) => {
    accommodations = [];
    res.json({ success: true, message: 'Data reset successfully!' });
});

// Serve the main HTML file
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
