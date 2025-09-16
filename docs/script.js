// Privacy Policy Markdown Renderer with Material 3 Expressive behaviors
document.addEventListener('DOMContentLoaded', async function() {
    // Initialize scroll-to-top button
    initScrollToTop();

    // Initialize expressive animations
    initExpressiveAnimations();
    const contentElement = document.getElementById('privacy-content');

    try {
        // Fetch the privacy policy markdown file
        const response = await fetch('./privacy-policy.md');

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const markdownText = await response.text();

        // Configure marked options
        marked.setOptions({
            breaks: true,
            gfm: true,
            headerIds: true,
            sanitize: false
        });

        // Convert markdown to HTML
        const htmlContent = marked.parse(markdownText);

        // Update the content
        contentElement.innerHTML = htmlContent;

        // Add smooth scroll behavior to anchor links
        addSmoothScrolling();

        // Add copy link functionality to headings
        addCopyLinkToHeadings();

    } catch (error) {
        console.error('Error loading privacy policy:', error);
        contentElement.innerHTML = `
            <div class="error">
                <h2>Error Loading Privacy Policy</h2>
                <p>Sorry, we couldn't load the privacy policy. Please try again later or visit our <a href="https://github.com/cemcakmak/HydroTracker" target="_blank">GitHub repository</a>.</p>
                <p>Error details: ${error.message}</p>
            </div>
        `;
    }
});

function addSmoothScrolling() {
    // Add smooth scrolling to all anchor links
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

function addCopyLinkToHeadings() {
    // Add copy link functionality to headings
    document.querySelectorAll('h1[id], h2[id], h3[id], h4[id], h5[id], h6[id]').forEach(heading => {
        heading.style.position = 'relative';
        heading.style.cursor = 'pointer';

        heading.addEventListener('click', function() {
            const url = window.location.origin + window.location.pathname + '#' + this.id;

            // Modern clipboard API
            if (navigator.clipboard) {
                navigator.clipboard.writeText(url).then(() => {
                    showCopyNotification(this);
                }).catch(err => {
                    console.error('Failed to copy URL:', err);
                    fallbackCopyTextToClipboard(url);
                });
            } else {
                fallbackCopyTextToClipboard(url);
            }
        });
    });
}

function showCopyNotification(element) {
    // Create temporary notification
    const notification = document.createElement('span');
    notification.textContent = ' (Link copied!)';
    notification.style.color = 'var(--secondary-color)';
    notification.style.fontSize = '0.8em';
    notification.style.fontWeight = 'normal';

    element.appendChild(notification);

    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 2000);
}

function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;

    // Avoid scrolling to bottom
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.position = 'fixed';

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        document.execCommand('copy');
    } catch (err) {
        console.error('Fallback: Could not copy text:', err);
    }

    document.body.removeChild(textArea);
}

// Add CSS for error styling
const errorStyles = `
.error {
    text-align: center;
    padding: 2rem;
    color: var(--text-secondary);
}

.error h2 {
    color: #f44336;
    margin-bottom: 1rem;
}

.error p {
    margin-bottom: 1rem;
}

.error a {
    color: var(--primary-color);
    text-decoration: none;
    font-weight: 500;
}

.error a:hover {
    text-decoration: underline;
}
`;

// Inject error styles
const style = document.createElement('style');
style.textContent = errorStyles;
document.head.appendChild(style);

// Material 3 Expressive Functions

function initScrollToTop() {
    const fab = document.getElementById('scrollToTop');

    // Show/hide FAB based on scroll position
    window.addEventListener('scroll', () => {
        if (window.scrollY > 300) {
            fab.classList.add('visible');
        } else {
            fab.classList.remove('visible');
        }
    });

    // Smooth scroll to top
    fab.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

function initExpressiveAnimations() {
    // Intersection Observer for fade-in animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Add staggered fade-in animations
    setTimeout(() => {
        const animatedElements = document.querySelectorAll('h1, h2, h3, p, ul, ol');
        animatedElements.forEach((el, index) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = `opacity 0.6s cubic-bezier(0.2, 0.0, 0, 1.0) ${index * 0.1}s, transform 0.6s cubic-bezier(0.2, 0.0, 0, 1.0) ${index * 0.1}s`;
            observer.observe(el);
        });
    }, 500);

    // Add hover effects to links
    document.addEventListener('mouseover', (e) => {
        if (e.target.tagName === 'A') {
            e.target.style.transform = 'scale(1.02)';
        }
    });

    document.addEventListener('mouseout', (e) => {
        if (e.target.tagName === 'A') {
            e.target.style.transform = 'scale(1)';
        }
    });

    // Add ripple effect to clickable elements
    document.addEventListener('click', createRipple);
}

function createRipple(event) {
    const element = event.target;

    if (!element.matches('a, button, .fab')) return;

    const circle = document.createElement('span');
    const diameter = Math.max(element.clientWidth, element.clientHeight);
    const radius = diameter / 2;

    circle.style.width = circle.style.height = `${diameter}px`;
    circle.style.left = `${event.clientX - element.offsetLeft - radius}px`;
    circle.style.top = `${event.clientY - element.offsetTop - radius}px`;
    circle.classList.add('ripple');

    const ripple = element.getElementsByClassName('ripple')[0];
    if (ripple) {
        ripple.remove();
    }

    element.appendChild(circle);

    // Remove ripple after animation
    setTimeout(() => {
        circle.remove();
    }, 600);
}

// Add ripple styles dynamically
const rippleStyles = `
.ripple {
    position: absolute;
    border-radius: 50%;
    transform: scale(0);
    animation: ripple 600ms linear;
    background-color: rgba(255, 255, 255, 0.6);
    pointer-events: none;
}

@keyframes ripple {
    to {
        transform: scale(4);
        opacity: 0;
    }
}

/* Ensure elements can contain ripples */
a, button, .fab {
    position: relative;
    overflow: hidden;
}
`;

const rippleStyleElement = document.createElement('style');
rippleStyleElement.textContent = rippleStyles;
document.head.appendChild(rippleStyleElement);