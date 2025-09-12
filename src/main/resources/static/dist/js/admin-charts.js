// Admin Dashboard Charts
document.addEventListener('DOMContentLoaded', function() {

    if (window.dashboardData && window.dashboardData.userRegistrationChart) {
        initUserRegistrationChart();
    }

    if (window.dashboardData && window.dashboardData.eventCreationChart) {
        initEventCreationChart();
    }

    if (window.dashboardData && window.dashboardData.usersByDepartment) {
        initUsersByDepartmentChart();
    }

    if (window.dashboardData && window.dashboardData.eventsByDepartment) {
        initEventsByDepartmentChart();
    }

    setInterval(refreshDashboard, 300000);
});

function initUserRegistrationChart() {
    const ctx = document.getElementById('userRegistrationChart').getContext('2d');
    const data = window.dashboardData.userRegistrationChart;

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.map(item => item.label),
            datasets: [{
                label: 'Đăng ký người dùng',
                data: data.map(item => item.value),
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Thống kê đăng ký người dùng (30 ngày gần nhất)'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function initEventCreationChart() {
    const ctx = document.getElementById('eventCreationChart').getContext('2d');
    const data = window.dashboardData.eventCreationChart;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(item => item.label),
            datasets: [{
                label: 'Sự kiện được tạo',
                data: data.map(item => item.value),
                backgroundColor: 'rgba(54, 162, 235, 0.8)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Thống kê tạo sự kiện (30 ngày gần nhất)'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function initUsersByDepartmentChart() {
    const ctx = document.getElementById('usersByDepartmentChart').getContext('2d');
    const data = window.dashboardData.usersByDepartment;

    const labels = Object.keys(data);
    const values = Object.values(data);

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    '#FF6384',
                    '#36A2EB',
                    '#FFCE56',
                    '#4BC0C0',
                    '#9966FF',
                    '#FF9F40'
                ]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Phân bố người dùng theo phòng ban'
                },
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

function initEventsByDepartmentChart() {
    const ctx = document.getElementById('eventsByDepartmentChart').getContext('2d');
    const data = window.dashboardData.eventsByDepartment;

    const labels = Object.keys(data);
    const values = Object.values(data);

    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    '#FF6384',
                    '#36A2EB',
                    '#FFCE56',
                    '#4BC0C0',
                    '#9966FF',
                    '#FF9F40'
                ]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Phân bố sự kiện theo phòng ban'
                },
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

function refreshDashboard() {
    fetch('/admin/api/dashboard-refresh', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            // Cập nhật dữ liệu mới
            updateDashboardData(data);
        })
        .catch(error => {
            console.error('Lỗi khi refresh dashboard:', error);
        });
}

function updateDashboardData(newData) {
    document.querySelector('.total-users h3').textContent = newData.totalUsers || 0;
    document.querySelector('.active-users h3').textContent = newData.activeUsers || 0;
    document.querySelector('.pending-events h3').textContent = newData.pendingEvents || 0;
    document.querySelector('.critical-alerts h3').textContent = newData.criticalAlerts || 0;

    const now = new Date();
    document.querySelector('.last-updated span').textContent =
        now.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
}

// Xử lý cảnh báo hệ thống
function dismissAlert(alertId) {
    fetch(`/admin/api/alerts/${alertId}/dismiss`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.ok) {
                // Ẩn cảnh báo
                document.querySelector(`[data-alert-id="${alertId}"]`).remove();
            }
        })
        .catch(error => {
            console.error('Lỗi khi đóng cảnh báo:', error);
        });

}