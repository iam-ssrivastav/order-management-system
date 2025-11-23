const API_URL = 'http://localhost:8080'; // API Gateway URL

// State
let token = localStorage.getItem('token');
let user = localStorage.getItem('user');

// DOM Elements
const loginScreen = document.getElementById('login-screen');
const dashboard = document.getElementById('dashboard');
const loginForm = document.getElementById('login-form');
const orderForm = document.getElementById('order-form');
const ordersList = document.getElementById('orders-list');

// Init
function init() {
    if (token) {
        showDashboard();
    } else {
        showLogin();
    }
}

function showLogin() {
    loginScreen.style.display = 'flex';
    dashboard.style.display = 'none';
}

function showDashboard() {
    loginScreen.style.display = 'none';
    dashboard.style.display = 'block';
    document.getElementById('user-display').textContent = user || 'User';
    fetchOrders();
}

// Auth
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;

    // For demo, we'll simulate a token since we haven't implemented a full Auth Service with DB yet.
    // In a real app, this would call the Auth Service.
    // We will use the Gateway's hardcoded token generation or just a dummy token if the Gateway allows it.
    // Wait, the Gateway checks for "Bearer " + token. 
    // The Gateway AuthenticationFilter validates the token. We need a valid token.
    // Since we didn't implement a /login endpoint that returns a signed JWT in the Gateway yet,
    // we will simulate a successful login and use a hardcoded valid token for testing if possible,
    // OR we should have implemented the token generation.

    // Let's try to hit the Gateway's /auth/token endpoint if we made one. We didn't.
    // CRITICAL: We need a way to get a valid token.
    // I will update the Gateway to have a simple /auth/login endpoint that returns a valid token.

    try {
        const response = await fetch(`${API_URL}/auth/login?username=${username}`, {
            method: 'POST'
        });

        if (response.ok) {
            const data = await response.text(); // Assuming it returns just the token string
            token = data;
            user = username;
            localStorage.setItem('token', token);
            localStorage.setItem('user', user);
            showDashboard();
        } else {
            alert('Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        // Fallback for demo if API is not ready
        alert('Login endpoint not reachable. Ensure API Gateway is running.');
    }
});

function logout() {
    token = null;
    user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    showLogin();
}

// Orders
orderForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const productId = document.getElementById('product-select').value;
    const quantity = parseInt(document.getElementById('quantity').value);

    const orderRequest = {
        productId,
        quantity,
        price: 999.99, // Hardcoded for demo
        customerId: user || 'demo-user'
    };

    try {
        const response = await fetch(`${API_URL}/api/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orderRequest)
        });

        if (response.ok) {
            alert('Order placed successfully!');
            fetchOrders();
        } else {
            alert('Failed to place order');
        }
    } catch (error) {
        console.error('Order error:', error);
        alert('Error placing order');
    }
});

async function fetchOrders() {
    try {
        const customerId = user || 'demo-user';
        const response = await fetch(`${API_URL}/api/orders/customer/${customerId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const orders = await response.json();
            renderOrders(orders);
        }
    } catch (error) {
        console.error('Fetch orders error:', error);
    }
}

function renderOrders(orders) {
    if (orders.length === 0) {
        ordersList.innerHTML = '<p style="color: var(--text-gray); text-align: center;">No orders yet.</p>';
        return;
    }

    ordersList.innerHTML = orders.map(order => `
        <div class="product-item">
            <div>
                <div style="font-weight: 600; color: white;">Order #${order.id}</div>
                <div style="font-size: 0.875rem; color: var(--text-gray);">${order.productId} (x${order.quantity})</div>
            </div>
            <div style="text-align: right;">
                <div style="font-weight: 600;">$${order.price}</div>
                <span class="status-badge ${order.status === 'CREATED' ? 'status-success' : 'status-pending'}">
                    ${order.status}
                </span>
            </div>
        </div>
    `).join('');
}

init();
