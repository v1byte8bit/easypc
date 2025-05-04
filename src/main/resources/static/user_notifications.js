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

                                    let characteristicsHtml = '';
                                    if (product.characteristics && Array.isArray(product.characteristics)) {
                                        characteristicsHtml = product.characteristics.map(c =>
                                            `<p><strong>${c.name}</strong>: ${c.value}</p>`
                                        ).join('');
                                    }

                                    // Вставляем визуальную галочку и кнопку для выбора товара
                                    card.innerHTML = `
                                    <div class="replacement-content">
                                        <img src="${product.img}" alt="${product.name}" class="content-image" />
                                        <div class="content-text">
                                            <h3>${product.name || "Не указано"}</h3>
                                            <p class="product-price">Цена: ₽${product.price || "Не указана"}</p>
                                            <div class="product-characteristics">
                                                ${characteristicsHtml}
                                            </div>
                                            <div class="select-container">
                                                <span class="select-checkbox">&#10003;</span>
                                                <button class="add-button" data-id="${product.urlId}" data-order-id="${notification.orderId}">
                                                    Выбрать
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                `;

                                    const checkbox = card.querySelector('.select-checkbox');
                                    const selectButton = card.querySelector('.add-button');
                                    const allSelectButtons = document.querySelectorAll('.add-button');

                                    // Убираем галочку, пока она не выбрана
                                    checkbox.style.display = 'none';

                                    // Обработчик для изменения состояния (выбор товара)
                                    selectButton.addEventListener('click', (e) => {
                                        // Показать галочку и отключить другие кнопки
                                        checkbox.style.display = 'inline-block'; // Показываем галочку
                                        allSelectButtons.forEach(btn => btn.disabled = true); // Отключаем все кнопки
                                        selectButton.disabled = false; // Оставляем кнопку активной для выбранного товара

                                        // Выполняем замену товара
                                        const replacementUrlId = e.target.dataset.id;
                                        const orderId = e.target.dataset.orderId;
                                        fetch('/user/order/replace-product/' + orderId, {
                                            method: 'POST',
                                            headers: {
                                                'Content-Type': 'application/json'
                                            },
                                            body: JSON.stringify({
                                                replacementUrlId: parseInt(replacementUrlId),
                                            })
                                        })
                                            .then(response => {
                                                if (response.ok) {
                                                    console.log("Товар успешно заменен.");
                                                } else {
                                                    console.error("Произошла ошибка.");
                                                }
                                            })
                                            .catch(error => console.error('Ошибка:', error));
                                    });

                                    replacementsContainer.appendChild(card); // Добавляем карточку с товаром
                                })
                                .catch(err => console.error('Ошибка при получении данных о товаре', err));
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