document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Add animation to stat cards on scroll
    const animateOnScroll = () => {
        const statCards = document.querySelectorAll('.stat-card');
        
        statCards.forEach(card => {
            const cardPosition = card.getBoundingClientRect().top;
            const screenPosition = window.innerHeight / 1.3;
            
            if (cardPosition < screenPosition) {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }
        });
    };

    // Set initial styles for animation
    document.querySelectorAll('.stat-card').forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.6s ease-out, transform 0.6s ease-out';
    });

    // Run once on page load
    animateOnScroll();
    
    // Run on scroll
    window.addEventListener('scroll', animateOnScroll);

    // Handle feedback submission
    const feedbackForms = document.querySelectorAll('.feedback-form');
    feedbackForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            const eventId = this.dataset.eventId;
            
            // Show loading state
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Submitting...';
            
            // In a real application, you would make an AJAX call here
            // For now, we'll simulate a successful submission
            setTimeout(() => {
                // Show success message
                const alert = document.createElement('div');
                alert.className = 'alert alert-success mt-3';
                alert.role = 'alert';
                alert.textContent = 'Thank you for your feedback!';
                this.appendChild(alert);
                
                // Reset form
                this.reset();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
                
                // Remove success message after 3 seconds
                setTimeout(() => {
                    alert.remove();
                }, 3000);
            }, 1000);
        });
    });

    // Handle event registration
    const registerButtons = document.querySelectorAll('.register-btn');
    registerButtons.forEach(button => {
        button.addEventListener('click', function() {
            const eventId = this.dataset.eventId;
            // In a real application, you would make an AJAX call to register for the event
            // For now, we'll simulate a successful registration
            this.disabled = true;
            this.textContent = 'Registered';
            this.classList.remove('btn-primary');
            this.classList.add('btn-success');
            
            // Show success message
            const alert = document.createElement('div');
            alert.className = 'alert alert-success mt-3';
            alert.role = 'alert';
            alert.textContent = 'Successfully registered for the event!';
            this.closest('.event-actions').appendChild(alert);
            
            // Remove success message after 3 seconds
            setTimeout(() => {
                alert.remove();
            }, 3000);
        });
    });
});
