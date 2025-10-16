// Privacy Policy Markdown Renderer with Material 3 Expressive behaviors
document.addEventListener('DOMContentLoaded', async function() {
    // Initialize theme system (must be first)
    initThemeSystem();

    // Initialize scroll-to-top button
    initScrollToTop();

    const contentElement = document.getElementById('privacy-content');
    const dateElement = document.querySelector('.date');

    try {
        // Fetch the privacy policy markdown file
        const response = await fetch('./privacy-policy.md');

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        let markdownText = await response.text();

        // Extract only the dates section (keep the title)
        const datesRegex = /\*\*Effective Date:\*\*[^\n]*\n\s*\*\*Last Updated:\*\*[^\n]*/m;
        const datesMatch = markdownText.match(datesRegex);

        if (datesMatch && dateElement) {
            // Extract dates info
            const effectiveDateMatch = markdownText.match(/\*\*Effective Date:\*\*\s*(.+)/);
            const lastUpdatedMatch = markdownText.match(/\*\*Last Updated:\*\*\s*(.+)/);

            const effectiveDate = effectiveDateMatch ? effectiveDateMatch[1].trim() : '';
            const lastUpdated = lastUpdatedMatch ? lastUpdatedMatch[1].trim() : '';

            // Create date content HTML
            dateElement.innerHTML = `
                <div class="date-content">
                    <div class="date-item">
                        <span class="date-label">Effective Date:</span>
                        <span class="date-value">${effectiveDate}</span>
                    </div>
                    <div class="date-item">
                        <span class="date-label">Last Updated:</span>
                        <span class="date-value">${lastUpdated}</span>
                    </div>
                </div>
            `;

            // Remove only the dates section from markdown (keep the title)
            markdownText = markdownText.replace(datesRegex, '').trim();
        }

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

        // Apply subtle reveal animation
        document.querySelector('.container').classList.add('reveal');

    } catch (error) {
        console.error('Error loading privacy policy:', error);
        contentElement.innerHTML = `
            <div class="error">
                <h2>Error Loading Privacy Policy</h2>
                <p>Sorry, we couldn't load the privacy policy. Please try again later or visit our <a href="https://github.com/Econ01/HydroTracker" target="_blank">GitHub repository</a>.</p>
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


// Theme System - Auto-detect and toggle between light/dark mode
function initThemeSystem() {
    const themeToggle = document.getElementById('themeToggle');
    const themeIcon = document.getElementById('themeIcon');
    const body = document.body;

    // Detect system theme preference
    const prefersDarkScheme = window.matchMedia('(prefers-color-scheme: dark)');

    // Apply theme based on system preference (no memory)
    function applySystemTheme() {
        if (prefersDarkScheme.matches) {
            // Dark mode is default (no class needed)
            body.classList.remove('light');
            themeIcon.textContent = 'dark_mode';
        } else {
            // Light mode
            body.classList.add('light');
            themeIcon.textContent = 'light_mode';
        }
    }

    // Apply initial theme
    applySystemTheme();

    // Toggle theme manually
    themeToggle.addEventListener('click', () => {
        body.classList.toggle('light');

        if (body.classList.contains('light')) {
            themeIcon.textContent = 'light_mode';
        } else {
            themeIcon.textContent = 'dark_mode';
        }
    });

    // Listen for system theme changes and auto-update
    prefersDarkScheme.addEventListener('change', (e) => {
        // Only auto-update if user hasn't manually overridden
        // Since we don't save state, always respect system changes
        applySystemTheme();
    });
}