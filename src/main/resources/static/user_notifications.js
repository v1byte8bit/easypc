document.addEventListener('DOMContentLoaded', () => {
    const notificationsList = document.getElementById('notificationsList');
    const notificationDetail = document.getElementById('notificationDetail');
    const navLinks = {
        'Персональные данные': '/user/profile',
        'Заказы': '/orders',
        'Сборки': '/builds',
        'Уведомления': '/user/notification',
        'Выход': '/logout',
    };

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

    // Загрузка уведомлений с сервера
    fetch('/user/notification/get')
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при загрузке уведомлений');
            }
            return response.json();
        })
        .then(notifications => {
            if (notifications.length === 0) {
                notificationsList.innerHTML = '<p>Нет уведомлений</p>';
                notificationDetail.innerHTML = '<p>Выберите уведомление слева</p>';
                return;
            }
            notificationsList.innerHTML = '';
            notifications.forEach(notification => {
                const item = document.createElement('div');
                item.className = 'notification-item';
                item.textContent = `Уведомление #${notification.id}`;
                item.dataset.message = notification.message;

                item.addEventListener('click', () => {
                    notificationDetail.innerHTML = `<h3>Текст уведомления</h3><p>${notification.message}</p>`;
                });

                notificationsList.appendChild(item);
            });
        })
        .catch(error => {
            console.error('Ошибка:', error);
            notificationsList.innerHTML = '<p>Не удалось загрузить уведомления</p>';
            notificationDetail.innerHTML = '';
        });

    setupNavigation()
});