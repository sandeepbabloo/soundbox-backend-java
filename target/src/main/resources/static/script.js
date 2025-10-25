// Payment Alert Backend API JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Update timestamp
    updateTimestamp();
    
    // Add click handlers for endpoint items
    addEndpointClickHandlers();
    
    // Add smooth scrolling for better UX
    addSmoothScrolling();
    
    // Add loading animation
    addLoadingAnimation();
});

function updateTimestamp() {
    const timestampElement = document.getElementById('timestamp');
    if (timestampElement) {
        const now = new Date();
        const formattedTime = now.toLocaleString('en-US', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        timestampElement.textContent = formattedTime;
    }
}

function addEndpointClickHandlers() {
    const endpointItems = document.querySelectorAll('.endpoint-item');
    
    endpointItems.forEach(item => {
        item.addEventListener('click', function() {
            const code = this.querySelector('code');
            if (code) {
                // Copy to clipboard
                navigator.clipboard.writeText(code.textContent).then(() => {
                    showToast('Endpoint copied to clipboard!');
                }).catch(() => {
                    // Fallback for older browsers
                    selectText(code);
                    showToast('Endpoint selected - copy with Ctrl+C');
                });
            }
        });
        
        // Add hover effect
        item.style.cursor = 'pointer';
    });
}

function selectText(element) {
    if (window.getSelection) {
        const selection = window.getSelection();
        const range = document.createRange();
        range.selectNodeContents(element);
        selection.removeAllRanges();
        selection.addRange(range);
    }
}

function addSmoothScrolling() {
    // Add smooth scrolling to all internal links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

function addLoadingAnimation() {
    // Add a subtle loading animation to the status dot
    const statusDot = document.querySelector('.status-dot');
    if (statusDot) {
        statusDot.style.animation = 'pulse 2s infinite';
    }
}

function showToast(message) {
    // Create toast notification
    const toast = document.createElement('div');
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #4caf50;
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 1000;
        font-size: 14px;
        font-weight: 500;
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    }, 100);
    
    // Animate out and remove
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 2000);
}

// Add keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + K to focus on search (if we add search functionality)
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        // Future: focus on search input
    }
    
    // Escape to clear any selections
    if (e.key === 'Escape') {
        if (window.getSelection) {
            window.getSelection().removeAllRanges();
        }
    }
});

// Add API status check
function checkApiStatus() {
    fetch('/health')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'UP') {
                updateStatusIndicator(true);
            } else {
                updateStatusIndicator(false);
            }
        })
        .catch(error => {
            console.error('API status check failed:', error);
            updateStatusIndicator(false);
        });
}

function updateStatusIndicator(isUp) {
    const statusDot = document.querySelector('.status-dot');
    const statusText = document.querySelector('.status-text');
    
    if (isUp) {
        statusDot.style.background = '#4caf50';
        statusText.textContent = 'API is running';
        statusText.style.color = '#4caf50';
    } else {
        statusDot.style.background = '#f44336';
        statusText.textContent = 'API is down';
        statusText.style.color = '#f44336';
    }
}

// Check API status on page load and every 30 seconds
checkApiStatus();
setInterval(checkApiStatus, 30000);

// Add performance monitoring
window.addEventListener('load', function() {
    const loadTime = performance.now();
    console.log(`Page loaded in ${loadTime.toFixed(2)}ms`);
    
    // Log any performance issues
    if (loadTime > 3000) {
        console.warn('Page load time is slow:', loadTime.toFixed(2) + 'ms');
    }
});
