const navLinks = {
    'Персональные данные': '/assembler/profile',
    'Заявки': '/assembler/orders',
    'В работе': '/on_work',
    'Выход': '/assembler/logout',
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
        const response = await fetch("/created/orders");
        if (!response.ok) {
            throw new Error("Ошибка загрузки заказов");
        }
        const orders = await response.json();
        renderOrders(orders);
    } catch (error) {
        console.error("Ошибка загрузки данных:", error);
    }

    function renderOrders(orders, builderId) {
        const contentContainer = document.querySelector(".content-container");
        contentContainer.innerHTML = ""; // Очищаем контейнер перед рендерингом

        orders.forEach(order => {
            const orderBlock = document.createElement("div");
            orderBlock.classList.add("order-block");

            orderBlock.innerHTML = `
            <div class="order-info">
                <img src="/static/free-icon-gaming-pc-7177534-20.png" class="order-image" />
                
                <div class="order-details">
                    <div class="_00001">#${order.id}</div>
                    <div class="div3">${order.status || "На сборке"}</div>
                </div>

                <div class="order-action">
                    <div class="_134500">₽${order.totalPrice || "Не указана"}</div>
                    <div class="work-btn" data-order-id="${order.id}">В работу</div>
                </div>
            </div>

            <div class="accordion-content" id="order-${order.id}" style="display: none;"></div>
        `;

            contentContainer.appendChild(orderBlock);

            // Добавляем обработчик клика на кнопку "В работу"
            const workBtn = orderBlock.querySelector(".work-btn");
            workBtn.addEventListener("click", async (event) => {
                event.stopPropagation(); // Чтобы клик не раскрывал аккордеон
                await takeOrderInWork(order.id, builderId, orderBlock);
            });

            // Добавляем обработчик клика для раскрытия товаров
            orderBlock.addEventListener("click", () => toggleOrderItems(order.id, order.items, orderBlock));
        });
    }

// Функция для обновления статуса заказа
    async function takeOrderInWork(orderId, builderId, orderBlock) {
        try {
            const response = await fetch(`/${orderId}/take`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ builderId: builderId })
            });

            if (response.ok) {
                // Обновляем статус заказа на клиенте
                const statusElement = orderBlock.querySelector(".div3");
                statusElement.textContent = "На сборке";

                // Делаем кнопку неактивной после успешного обновления
                const workBtn = orderBlock.querySelector(".work-btn");
                workBtn.textContent = "Взято";
                workBtn.classList.add("disabled");
                workBtn.removeEventListener("click", takeOrderInWork);
            } else {
                console.error("Ошибка при взятии заказа в работу");
            }
        } catch (error) {
            console.error("Ошибка сети:", error);
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
            <div class="url">Ссылка: <a href="${item.url}" target="_blank" class="no-toggle">Перейти</a></div>
        </div>
        <!-- Цена и кнопка -->
        <div class="item-action">
            <div class="_134500 no-toggle">${item.price || "Не указана"}₽</div>
            <div class="quantity no-toggle">Количество: ${item.quantity}</div>
        </div>
    `;

            container.appendChild(itemBlock);
        });

        // Добавляем обработчик для предотвращения закрытия аккордеона
        container.querySelectorAll(".no-toggle").forEach(element => {
            element.addEventListener("click", event => {
                event.stopPropagation(); // Останавливаем всплытие клика
            });
        });
    }
});

document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
});