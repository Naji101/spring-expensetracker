// charts.js - Visual Reporting Charts for Expense Tracker

let categoryChart, incomeExpenseChart, monthlyTrendChart, dailyChart;

/**
 * Initialize all charts with expense data
 */
function initializeCharts(expensesData) {
    if (!expensesData || expensesData.length === 0) {
        showEmptyChartMessages();
        return;
    }

    // Initialize each chart
    initCategoryChart(expensesData);
    initIncomeExpenseChart(expensesData);
    initMonthlyTrendChart(expensesData);
    initDailyChart(expensesData);
}

/**
 * Category Breakdown Pie Chart
 */
function initCategoryChart(expensesData) {
    const ctx = document.getElementById('categoryChart');
    if (!ctx) return;

    // Filter only expenses (not income)
    const expenses = expensesData.filter(exp => exp.type === 'EXPENSE');
    
    // Group by category
    const categoryData = {};
    expenses.forEach(exp => {
        const category = exp.category;
        if (!categoryData[category]) {
            categoryData[category] = 0;
        }
        categoryData[category] += parseFloat(exp.amount);
    });

    const labels = Object.keys(categoryData);
    const data = Object.values(categoryData);
    const colors = generateColors(labels.length);

    if (categoryChart) categoryChart.destroy();
    
    categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${label}: $${value.toFixed(2)} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

/**
 * Income vs Expense Bar Chart
 */
function initIncomeExpenseChart(expensesData) {
    const ctx = document.getElementById('incomeExpenseChart');
    if (!ctx) return;

    // Calculate totals
    const totalIncome = expensesData
        .filter(exp => exp.type === 'INCOME')
        .reduce((sum, exp) => sum + parseFloat(exp.amount), 0);
    
    const totalExpense = expensesData
        .filter(exp => exp.type === 'EXPENSE')
        .reduce((sum, exp) => sum + parseFloat(exp.amount), 0);

    if (incomeExpenseChart) incomeExpenseChart.destroy();

    incomeExpenseChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Income', 'Expenses', 'Balance'],
            datasets: [{
                label: 'Amount ($)',
                data: [totalIncome, totalExpense, totalIncome - totalExpense],
                backgroundColor: [
                    'rgba(40, 167, 69, 0.7)',
                    'rgba(220, 53, 69, 0.7)',
                    'rgba(102, 126, 234, 0.7)'
                ],
                borderColor: [
                    'rgb(40, 167, 69)',
                    'rgb(220, 53, 69)',
                    'rgb(102, 126, 234)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `$${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toFixed(0);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Monthly Trend Line Chart
 */
/**
 * Monthly Income vs Expense BAR Chart
 */
function initMonthlyTrendChart(expensesData) {
    const ctx = document.getElementById('monthlyTrendChart');
    if (!ctx) return;

    // Group by month
    const monthlyData = {};

	expensesData.forEach(exp => {
	    const date = new Date(
	        exp.date.year,
	        exp.date.monthValue - 1,
	        exp.date.dayOfMonth
	    );

	    const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

	    if (!monthlyData[monthKey]) {
	        monthlyData[monthKey] = { income: 0, expense: 0 };
	    }

	    if (exp.type === 'INCOME') {
	        monthlyData[monthKey].income += parseFloat(exp.amount);
	    } else {
	        monthlyData[monthKey].expense += parseFloat(exp.amount);
	    }
	});


    // Sort months chronologically
    const sortedMonths = Object.keys(monthlyData).sort();

    const labels = sortedMonths.map(month => {
        const [year, monthNum] = month.split('-');
        const date = new Date(year, monthNum - 1);
        return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
    });

    const incomeData = sortedMonths.map(month => monthlyData[month].income);
    const expenseData = sortedMonths.map(month => monthlyData[month].expense);

    if (monthlyTrendChart) monthlyTrendChart.destroy();

    monthlyTrendChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    backgroundColor: 'rgba(40, 167, 69, 0.7)',
                    borderColor: 'rgb(40, 167, 69)',
                    borderWidth: 1
                },
                {
                    label: 'Expenses',
                    data: expenseData,
                    backgroundColor: 'rgba(220, 53, 69, 0.7)',
                    borderColor: 'rgb(220, 53, 69)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: $${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    stacked: false
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function (value) {
                            return '$' + value.toFixed(0);
                        }
                    }
                }
            }
        }
    });
}


/**
 * Daily Activity Chart (Last 30 Days)
 */
function initDailyChart(expensesData) {
    const ctx = document.getElementById('dailyChart');
    if (!ctx) return;

    // Today at midnight (avoid time drift)
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const thirtyDaysAgo = new Date(today);
    thirtyDaysAgo.setDate(today.getDate() - 30);

    // Group by day
    const dailyData = {};

    // Initialize last 30 days with 0
    for (let d = new Date(thirtyDaysAgo); d <= today; d.setDate(d.getDate() + 1)) {
        const dayKey = d.toISOString().split('T')[0];
        dailyData[dayKey] = { income: 0, expense: 0 };
    }

    // Fill with actual data
    expensesData.forEach(exp => {

        const date = new Date(
            exp.date.year,
            exp.date.monthValue - 1,
            exp.date.dayOfMonth
        );

        // Normalize time
        date.setHours(0, 0, 0, 0);

        if (date >= thirtyDaysAgo && date <= today) {
            const dayKey = date.toISOString().split('T')[0];

            if (exp.type === 'INCOME') {
                dailyData[dayKey].income += parseFloat(exp.amount);
            } else {
                dailyData[dayKey].expense += parseFloat(exp.amount);
            }
        }
    });

    // Sort by date
    const sortedDays = Object.keys(dailyData).sort();

    const labels = sortedDays.map(day => {
        const date = new Date(day + 'T00:00:00');
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    });

    const incomeData = sortedDays.map(day => dailyData[day].income);
    const expenseData = sortedDays.map(day => dailyData[day].expense);

    if (dailyChart) dailyChart.destroy();

    dailyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    backgroundColor: 'rgba(40, 167, 69, 0.7)',
                    borderColor: 'rgb(40, 167, 69)',
                    borderWidth: 1
                },
                {
                    label: 'Expenses',
                    data: expenseData,
                    backgroundColor: 'rgba(220, 53, 69, 0.7)',
                    borderColor: 'rgb(220, 53, 69)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: $${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: value => '$' + value.toFixed(0)
                    }
                }
            }
        }
    });
}

/**
 * Generate colors for charts
 */
function generateColors(count) {
    const colors = [
        '#667eea', '#764ba2', '#f093fb', '#4facfe',
        '#43e97b', '#fa709a', '#fee140', '#30cfd0',
        '#a8edea', '#fed6e3', '#ff6b6b', '#4ecdc4'
    ];
    
    const result = [];
    for (let i = 0; i < count; i++) {
        result.push(colors[i % colors.length]);
    }
    return result;
}

/**
 * Show empty state messages when no data
 */
function showEmptyChartMessages() {
    const chartIds = ['categoryChart', 'incomeExpenseChart', 'monthlyTrendChart', 'dailyChart'];
    
    chartIds.forEach(id => {
        const canvas = document.getElementById(id);
        if (canvas) {
            const parent = canvas.parentElement;
            canvas.style.display = 'none';
            
            const emptyMsg = document.createElement('div');
            emptyMsg.className = 'chart-empty-state';
            emptyMsg.innerHTML = '<p>No data available. Add transactions to see charts.</p>';
            parent.appendChild(emptyMsg);
        }
    });
}

/**
 * Update charts when filters change
 */
function updateChartsWithFilteredData(filteredData) {
    if (filteredData && filteredData.length > 0) {
        initializeCharts(filteredData);
    } else {
        showEmptyChartMessages();
    }
}