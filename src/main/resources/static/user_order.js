const navLinks = {
    'Персональные данные': '/user/profile',
    'Заказы': '/orders',
    'Сборки': '/builds',
    'Уведомления': '/notifications',
    'Выход': '/logout',
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

document.addEventListener("DOMContentLoaded", async function () {
    const contentContainer = document.querySelector(".content-container");

    try {
        const response = await fetch("/api/orders");
        if (!response.ok) {
            throw new Error("Ошибка загрузки заказов");
        }
        const orders = await response.json();
        renderOrders(orders);
    } catch (error) {
        console.error("Ошибка загрузки данных:", error);
    }

    function renderOrders(orders) {
        const contentContainer = document.querySelector(".content-container");
        contentContainer.innerHTML = ""; // Очищаем контейнер перед рендерингом

        orders.forEach(order => {
            const orderBlock = document.createElement("div");
            orderBlock.classList.add("order-block");

            // Определяем CSS-класс для статуса заказа
            const statusClass = order.status === "Отменен" ? "status-canceled" : "";

            orderBlock.innerHTML = `
        <div class="order-info">
            <img src="/static/free-icon-gaming-pc-7177534-20.png" class="order-image" />
            
            <div class="order-details">
                <div class="_00001">#${order.id}</div>
                <div class="div3 ${statusClass}">${order.status}</div>
            </div>

            <div class="order-action">
                <div class="_134500">₽${order.totalPrice || "Не указана"}</div>
                <div class="cancel-btn" data-order-id="${order.id}">Отменить</div>
            </div>
        </div>

        <div class="accordion-content" id="order-${order.id}" style="display: none;"></div>
    `;

            contentContainer.appendChild(orderBlock);

            // Добавляем обработчик для кнопки отмены заказа
            const cancelButton = orderBlock.querySelector(".cancel-btn");

            if (order.status === "Отменен") {
                cancelButton.remove(); // Убираем кнопку, если заказ отменен
            } else {
                cancelButton.addEventListener("click", async (event) => {
                    event.stopPropagation(); // Чтобы не срабатывал аккордеон
                    await cancelOrder(order.id, orderBlock);
                });
            }

            orderBlock.addEventListener("click", () => toggleOrderItems(order.id, order.items, orderBlock));
        });
    }

    async function cancelOrder(orderId, orderBlock) {
        try {
            const response = await fetch(`/${orderId}/cancel`, { method: "PUT" });

            if (!response.ok) {
                throw new Error("Ошибка при отмене заказа");
            }

            // Меняем текст статуса и добавляем красный цвет
            const statusElement = orderBlock.querySelector(".div3");
            statusElement.textContent = "Отменен";
            statusElement.classList.add("status-canceled");

            // Удаляем кнопку отмены, так как заказ уже отменен
            const cancelButton = orderBlock.querySelector(".cancel-btn");
            if (cancelButton) {
                cancelButton.remove();
            }

        } catch (error) {
            console.error("Ошибка при отмене заказа:", error);
        }
    }

    function toggleOrderItems(orderId, items, orderBlock) {
        const accordion = document.getElementById(`order-${orderId}`);

        if (accordion.style.display === "none" || accordion.innerHTML === "") {
            renderOrderItems(accordion, items);
            accordion.style.display = "flex"; // Показываем товары
        } else {
            accordion.style.display = "none"; // Скрываем, если уже открыто
        }
    }

    function renderOrderItems(container, items) {
        container.innerHTML = ""; // Очищаем перед вставкой новых элементов

        items.forEach(item => {
            const itemBlock = document.createElement("div");
            itemBlock.classList.add("order-item-block");

            itemBlock.innerHTML = `
            <!-- Картинка товара -->
            <img class="img_item" src="${item.imageUrl}" alt="${item.name}" />
            <!-- Название и статус -->
            <div class="item-details">
                <div class="_00001">${item.name}</div>
            </div>
            <!-- Цена и кнопка -->
            <div class="item-action">
                <div class="_134500">${item.price || "Не указана"}₽</div>
                <div class="quantity">Количество: ${item.quantity}</div>
            </div>
        `;

            container.appendChild(itemBlock);
        });
    }
});

document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
});