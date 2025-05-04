document.addEventListener("DOMContentLoaded", async function () {
    const contentContainer = document.querySelector(".content-container");
    const loadingSpinner = document.getElementById('loading-spinner');
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

    // Добавляем модалку
    const modal = document.getElementById('notification-modal');
    const closeModalButton = document.getElementById('close-modal');

// Функция для закрытия модалки
    function closeModal() {
        modal.style.display = 'none';
    }

// Закрыть модалку при клике на фон
    modal.addEventListener('click', function(event) {
        if (event.target === modal) { // Проверяем, что клик был именно по фону
            closeModal();
        }
    });

// Закрыть модалку при клике на кнопку "закрыть"
    closeModalButton.addEventListener('click', closeModal);
    modal.style.display = "none";
    modal.innerHTML = `
    <div class="modal-content">
        <span class="close-button" id="close-modal">&times;</span>
        <h2>Создать уведомление</h2>
        <textarea id="notification-text" placeholder="Введите сообщение..."></textarea>
        <select class="replacement-url-select" data-index="0">
            <option value="">Выберите товар-замену #1</option>
        </select>
        <select class="replacement-url-select" data-index="1">
             <option value="">Выберите товар-замену #2</option>
        </select>
        <button id="send-notification-button">Отправить</button>
    </div>
`;

    document.body.appendChild(modal);

    let currentOrderId = null;
    let currentUserId = null;
    let currentAssemblerId = null;

    // Обработчик закрытия модалки
    const closeBtn = document.getElementById("close-modal");
    if (closeBtn) {
        closeBtn.addEventListener("click", () => {
            modal.style.display = "none";
        });
    }

    // Обработчик отправки уведомления
    const sendBtn = document.getElementById("send-notification-button");
    if (sendBtn) {
        sendBtn.addEventListener("click", async () => {
            try {
                const message = document.getElementById("notification-text").value.trim();
                if (!message) {
                    alert("Введите текст уведомления");
                    return;
                }

                const replacementSelects = document.querySelectorAll(".replacement-url-select");
                const replacementUrls = Array.from(replacementSelects)
                    .map(select => select.value.trim())
                    .filter(id => id);

                if (replacementUrls.length > 2) {
                    alert("Можно указать не более двух товаров-замен");
                    return;
                }

                const response = await fetch(`/notifications/create/${currentOrderId}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        message: message,
                        orderId: currentOrderId,
                        userId: currentUserId,
                        assemblerId: currentAssemblerId,
                        replacementProductUrlIds: replacementUrls
                    })
                });

                if (response.ok) {
                    alert(`Уведомление отправлено для заказа #${currentOrderId}`);
                    modal.style.display = "none";
                    document.getElementById("notification-text").value = "";
                    replacementSelects.forEach(select => select.value = "");
                } else {
                    alert("Ошибка при отправке уведомления");
                }
            } catch (error) {
                console.error("Ошибка в обработчике кнопки:", error);
                alert("Ошибка при отправке запроса");
            }
        });
    }
    showSpinner();
    try {
        const response = await fetch("/assembler/on/work");
        if (!response.ok) throw new Error("Ошибка загрузки заказов");
        const orders = await response.json();
        renderOrders(orders);
    } catch (error) {
        console.error("Ошибка загрузки данных:", error);
    } finally {
    hideSpinner();
    }

    function renderOrders(orders) {
        contentContainer.innerHTML = "";
        if (orders.length === 0) {
            const emptyMessage = document.createElement("div");
            emptyMessage.textContent = "Нет выполняемых заказов";
            emptyMessage.style.fontSize = "20px";
            emptyMessage.style.color = "#aaa";
            emptyMessage.style.textAlign = "center";
            emptyMessage.style.marginTop = "50px";
            contentContainer.appendChild(emptyMessage);
            return;
        }
        orders.forEach(order => {
            const orderBlock = document.createElement("div");
            orderBlock.classList.add("order-block");

            orderBlock.innerHTML = `
                <div class="order-info">
                    <img src="/static/free-icon-gaming-pc-7177534-20.png" class="order-image" />
                    <div class="order-details">
                        <div class="_00001">Номер заказа:  #${order.id}</div>
                        <div class="address">Адрес: ${order.address}</div>
                        <div class="phone_number">Телефон: ${order.phone}</div>
                        <div class="div3 order-status">
                        <span style="color: #ffffff;">Статус:</span> <span>${order.status}</span>
                        </div>
                    </div>
                    <div class="order-action">
                        <div class="_134500">₽${order.totalPrice || "Не указана"}</div>
                        <div class="status_btn">Изменить статус</div>
                        <div class="status-menu" style="display: none;">
                            <div class="status-option" data-status="На сборке">На сборке</div>
                            <div class="status-option" data-status="Ожидает">Ожидает</div>
                            <div class="status-option" data-status="Готов">Готов</div>
                            <div class="status-option" data-status="Отправлен">Отправлен</div>
                            <div class="status-option" data-status="Завершен">Завершен</div>
                        </div>
                        <div class="not_btn" data-order-id="${order.id}">Создать уведомление</div>
                    </div>
                </div>

                <div class="accordion-content" id="order-${order.id}" style="display: none;"></div>
            `;

            const notBtn = orderBlock.querySelector(".not_btn");
            if (notBtn) {
                notBtn.addEventListener("click", async () => {
                    currentOrderId = order.id;
                    currentUserId = order.userId;
                    currentAssemblerId = order.assemblerId;

                    document.getElementById("notification-text").value = "";

                    document.querySelectorAll(".replacement-url-select").forEach(select => {
                        select.innerHTML = '<option value="">Выберите товар-замену</option>';
                    });
                    try {
                        const res = await fetch("/notifications/source/all");
                        if (!res.ok) throw new Error("Ошибка загрузки товаров");
                        const products = await res.json();

                        document.querySelectorAll(".replacement-url-select").forEach(select => {
                            products.forEach(p => {
                                const option = document.createElement("option");
                                option.value = p.id;
                                option.textContent = `${p.category}: ${p.source}`;
                                select.appendChild(option);
                            });
                        });
                    } catch (e) {
                        console.error("Ошибка загрузки товаров:", e);
                        alert("Не удалось загрузить товары для выбора");
                    }
                    modal.style.display = "flex";
                });
            }

            contentContainer.appendChild(orderBlock);

            // Раскрытие товаров
            orderBlock.addEventListener("click", (event) => {
                const isControlClick = event.target.closest(".status_btn, .not_btn, .status-menu, .no-toggle, .modal");
                if (isControlClick) return;
                toggleOrderItems(order.id, order.items, orderBlock);
            });

            // Меню статуса
            const statusBtn = orderBlock.querySelector(".status_btn");
            const statusMenu = orderBlock.querySelector(".status-menu");

            if (statusBtn && statusMenu) {
                statusBtn.addEventListener("click", (event) => {
                    event.stopPropagation();
                    toggleStatusMenu(statusMenu);
                });

                statusMenu.querySelectorAll(".status-option").forEach(option => {
                    option.addEventListener("click", () => {
                        updateOrderStatus(order.id, option.dataset.status, orderBlock);
                    });
                });
            }
        });
    }

    function toggleOrderItems(orderId, items, orderBlock) {
        const content = orderBlock.querySelector(`#order-${orderId}`);
        if (!content) return;

        if (content.style.display === "none") {
            if (!content.hasChildNodes()) {
                if (items && items.length > 0) {
                    content.innerHTML = items.map(item => `
                        <div class="order-item-block">
                            <img class="img_item" src="${item.imageUrl}" alt="${item.name}" />
                            <div class="item-details">
                                <div class="_00001">${item.name}</div>
                                <div class="url">Ссылка: <a href="${item.url}" target="_blank" class="no-toggle">Перейти</a></div>
                            </div>
                            <div class="item-action">
                                <div class="_134500 no-toggle">${item.price || "Не указана"}₽</div>
                                <div class="quantity no-toggle">Количество: ${item.quantity}</div>
                            </div>
                        </div>
                    `).join("");
                } else {
                    content.innerHTML = "<div class='order-item'>Нет товаров</div>";
                }
            }
            content.style.display = "block";
        } else {
            content.style.display = "none";
        }
    }

    function toggleStatusMenu(menu) {
        document.querySelectorAll(".status-menu").forEach(m => {
            if (m !== menu) m.style.display = "none";
        });
        menu.style.display = menu.style.display === "none" ? "block" : "none";
    }

    async function updateOrderStatus(orderId, newStatus, orderBlock) {
        try {
            const response = await fetch(`/assembler/work/${orderId}/status`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ status: newStatus }),
            });

            if (!response.ok) throw new Error("Ошибка обновления статуса");

            orderBlock.querySelector(".order-status").textContent = newStatus;
            orderBlock.querySelector(".status-menu").style.display = "none";
        } catch (error) {
            console.error("Ошибка обновления статуса:", error);
        }
    }

    function showSpinner() {
        loadingSpinner.style.display = 'block';
    }

    function hideSpinner() {
        loadingSpinner.style.display = 'none';
    }

    setupNavigation();
});