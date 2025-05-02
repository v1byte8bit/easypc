const navLinks = {
    'Персональные данные': '/assembler/profile',
    'Заявки': '/assembler/orders',
    'В работе': '/on_work',
    'Выход': 'logout',
};

// Функция для обработки кликов по навигационным элементам
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach((item) => {
        const text = item.textContent.trim();

        if (navLinks[text]) {
            item.addEventListener('click', () => {
                window.location.href = navLinks[text];
            });
        }
    });
}

async function loadUserData() {
    try {
        const response = await fetch('/assembler/api/current', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (response.ok) {
            const { email, phone } = await response.json();

            // Устанавливаем значения в поля
            document.getElementById('email').value = email || '';
            document.getElementById('phone').value = phone || '';
        } else {
            console.error('Ошибка загрузки данных:', response.status);
        }
    } catch (error) {
        console.error('Ошибка при загрузке данных:', error);
    }
}

// Функция для сохранения данных
function setupSaveButton() {
    const saveButton = document.querySelector('.save-btn');
    const emailInput = document.getElementById('email');
    const phoneInput = document.getElementById('phone');

    saveButton.addEventListener('click', async () => {
        const email = emailInput.value.trim();
        const phone = phoneInput.value.trim();

        // Отправка данных на сервер
        try {
            const response = await fetch('/assembler/api/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, phone }),
            });

            if (response.ok) {
                alert('Данные успешно сохранены.');
            } else {
                const errorData = await response.json();
                alert(`Ошибка сохранения данных: ${errorData.message}`);
            }
        } catch (error) {
            console.error('Ошибка при отправке данных:', error);
            alert('Произошла ошибка. Попробуйте позже.');
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    loadUserData();
    setupNavigation();
    setupSaveButton();
});