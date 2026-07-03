// API Base URL
const API_URL = 'http://localhost:3000/api';

// State
let currentUser = null;

// DOM Elements
const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const loginForm = document.getElementById('login-form');
const signupForm = document.getElementById('signup-form');
const userDisplay = document.getElementById('user-display');
const logoutBtn = document.getElementById('logout-btn');
const tabBtns = document.querySelectorAll('.tab-btn');
const addAccommodationForm = document.getElementById('add-accommodation-form');
const searchForm = document.getElementById('search-form');
const refreshAllBtn = document.getElementById('refresh-all-btn');
const resultsContainer = document.getElementById('results-container');
const allAccommodationsContainer = document.getElementById('all-accommodations');

// Tab switching
tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        const tab = btn.dataset.tab;
        
        // Update active tab button
        tabBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        // Show corresponding form
        loginForm.classList.remove('active');
        signupForm.classList.remove('active');
        
        if (tab === 'login') {
            loginForm.classList.add('active');
        } else {
            signupForm.classList.add('active');
        }
    });
});

// Helper function to show messages
function showMessage(elementId, message, isSuccess) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.className = 'message ' + (isSuccess ? 'success' : 'error');
    
    // Clear message after 5 seconds
    setTimeout(() => {
        el.textContent = '';
        el.className = 'message';
    }, 5000);
}

// Helper function for API calls
async function apiCall(endpoint, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    if (data) {
        options.body = JSON.stringify(data);
    }
    
    const response = await fetch(`${API_URL}${endpoint}`, options);
    return await response.json();
}

// Signup
signupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('signup-username').value;
    const password = document.getElementById('signup-password').value;
    
    try {
        const result = await apiCall('/signup', 'POST', { username, password });
        
        if (result.success) {
            showMessage('signup-message', result.message, true);
            signupForm.reset();
            
            // Switch to login tab
            setTimeout(() => {
                document.querySelector('[data-tab="login"]').click();
            }, 1000);
        } else {
            showMessage('signup-message', result.message, false);
        }
    } catch (error) {
        showMessage('signup-message', 'Error connecting to server. Make sure the backend is running.', false);
        console.error('Signup error:', error);
    }
});

// Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const result = await apiCall('/login', 'POST', { username, password });
        
        if (result.success) {
            showMessage('login-message', result.message, true);
            currentUser = result.username;
            
            // Show app section, hide auth section
            setTimeout(() => {
                authSection.classList.add('hidden');
                appSection.classList.remove('hidden');
                userDisplay.textContent = currentUser;
                loadAllAccommodations();
            }, 1000);
        } else {
            showMessage('login-message', result.message, false);
        }
    } catch (error) {
        showMessage('login-message', 'Error connecting to server. Make sure the backend is running.', false);
        console.error('Login error:', error);
    }
});

// Logout
logoutBtn.addEventListener('click', () => {
    currentUser = null;
    authSection.classList.remove('hidden');
    appSection.classList.add('hidden');
    loginForm.reset();
    
    // Reset to login tab
    document.querySelector('[data-tab="login"]').click();
});

// Add Accommodation
addAccommodationForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const accommodation = {
        name: document.getElementById('acc-name').value,
        type: document.getElementById('acc-type').value,
        lat: parseFloat(document.getElementById('acc-lat').value),
        lon: parseFloat(document.getElementById('acc-lon').value),
        rent: parseFloat(document.getElementById('acc-rent').value),
        rating: parseFloat(document.getElementById('acc-rating').value),
    };
    
    try {
        const result = await apiCall('/accommodations', 'POST', accommodation);
        
        if (result.success) {
            showMessage('add-message', result.message, true);
            addAccommodationForm.reset();
            loadAllAccommodations();
        } else {
            showMessage('add-message', result.message, false);
        }
    } catch (error) {
        showMessage('add-message', 'Error adding accommodation', false);
        console.error('Add error:', error);
    }
});

// Search
searchForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const radius = parseFloat(document.getElementById('search-radius').value);
    const type = document.getElementById('search-type').value;
    
    try {
        const result = await apiCall('/search', 'POST', { radius, type });
        
        if (result.success) {
            displayResults(result.data);
            
            if (result.data.length === 0) {
                showMessage('search-message', 'No accommodations found matching your criteria.', false);
            } else {
                showMessage('search-message', `Found ${result.data.length} accommodation(s)!`, true);
            }
        }
    } catch (error) {
        showMessage('search-message', 'Error performing search', false);
        console.error('Search error:', error);
    }
});

// Refresh All Accommodations
refreshAllBtn.addEventListener('click', loadAllAccommodations);

// Display results
function displayResults(accommodations) {
    if (accommodations.length === 0) {
        resultsContainer.innerHTML = '<div class="no-results">No accommodations found</div>';
        return;
    }
    
    resultsContainer.innerHTML = accommodations.map(acc => createResultCard(acc)).join('');
}

// Load all accommodations
async function loadAllAccommodations() {
    try {
        const result = await apiCall('/accommodations');
        
        if (result.success) {
            displayAllAccommodations(result.data);
        }
    } catch (error) {
        console.error('Load error:', error);
    }
}

// Display all accommodations
function displayAllAccommodations(accommodations) {
    if (accommodations.length === 0) {
        allAccommodationsContainer.innerHTML = '<div class="no-results">No accommodations added yet</div>';
        return;
    }
    
    allAccommodationsContainer.innerHTML = accommodations.map(acc => createResultCard(acc)).join('');
}

// Create result card HTML
function createResultCard(acc) {
    return `
        <div class="result-card">
            <h4>${escapeHtml(acc.name)}</h4>
            <p><span class="highlight">Type:</span> ${escapeHtml(acc.type)}</p>
            <p><span class="highlight">Rent:</span> ₹${acc.rent.toLocaleString()}</p>
            <p><span class="rating">★ ${acc.rating}</span> / 5</p>
            <p><span class="highlight">Distance:</span> ${acc.distance.toFixed(2)} km from campus</p>
            <p><span class="highlight">Location:</span> (${acc.lat.toFixed(4)}, ${acc.lon.toFixed(4)})</p>
        </div>
    `;
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Initialize - check if we should show auth or app section
// For now, always start with auth section
authSection.classList.remove('hidden');
appSection.classList.add('hidden');
