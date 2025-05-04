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

                    if (notification.replacementProductUrlIds && notification.replacementProductUrlIds.length > 0) {
                        const replacementsContainer = document.createElement('div');
                        replacementsContainer.innerHTML = `<h4>Предложенные замены:</h4>`;

                        notification.replacementProductUrlIds.forEach(productUrlId => {
                            fetch('/scrape?urlId=' + encodeURIComponent(productUrlId))
                                .then(resp => resp.ok ? resp.json() : Promise.reject())
                                .then(product => {
                                    const card = document.createElement('div');
                                    card.className = 'replacement-card';

                                    // Сформировать HTML для характеристик, если они есть
                                    let characteristicsHtml = '';
                                    if (product.characteristics && Array.isArray(product.characteristics)) {
                                        characteristicsHtml = product.characteristics.map(c =>
                                            `<p><strong>${c.name}</strong>: ${c.value}</p>`
                                        ).join('');
                                    }

                                    card.innerHTML = `
                <div class="replacement-content">
                    <img src="${product.img}" alt="${product.name}" class="content-image" />
                    <div class="content-text">
                        <h3>${product.name || "Не указано"}</h3>
                        <p class="product-price">Цена: ₽${product.price || "Не указана"}</p>
                        <div class="product-characteristics">
                            ${characteristicsHtml}
                        </div>
                        <button class="add-button" data-id="${product.urlId}">Выбрать</button>
                    </div>
                </div>
            `;
                                    replacementsContainer.appendChild(card);
                                })
                                .catch(() => {
                                    const err = document.createElement('p');
                                    err.textContent = `Не удалось загрузить товар: ${productUrlId}`;
                                    replacementsContainer.appendChild(err);
                                });
                        });

                        notificationDetail.appendChild(replacementsContainer);
                    }
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