document.addEventListener("DOMContentLoaded", async function () {
    const contentContainer = document.querySelector(".content-container");

    try {
        const response = await fetch("/assembler/on/work");
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

            orderBlock.innerHTML = `
                <div class="order-info">
                    <img src="/static/free-icon-gaming-pc-7177534-20.png" class="order-image" />
                    
                    <div class="order-details">
                        <div class="_00001">#${order.id}</div>
                        <div class="div3 order-status">${order.status}</div>
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
                        <div class="not_btn">Создать уведомление</div>
                    </div>
                </div>

                <div class="accordion-content" id="order-${order.id}" style="display: none;"></div>
            `;

            contentContainer.appendChild(orderBlock);

            // Добавляем обработчик клика для раскрытия товаров
            orderBlock.addEventListener("click", (event) => {
                const isControlClick = event.target.closest(".status_btn, .not_btn, .status-menu, .no-toggle");
                if (isControlClick) return;

                toggleOrderItems(order.id, order.items, orderBlock);
            });

            // Обработчик клика по кнопке "Изменить статус"
            const statusBtn = orderBlock.querySelector(".status_btn");
            const statusMenu = orderBlock.querySelector(".status-menu");

            statusBtn.addEventListener("click", (event) => {
                event.stopPropagation();
                toggleStatusMenu(statusMenu);
            });

            // Обработчик выбора статуса
            statusMenu.querySelectorAll(".status-option").forEach(option => {
                option.addEventListener("click", () => updateOrderStatus(order.id, option.dataset.status, orderBlock));
            });
        });
    }


    function toggleStatusMenu(menu) {
        const allMenus = document.querySelectorAll(".status-menu");
        allMenus.forEach(m => {
            if (m !== menu) {
                m.style.display = "none"; // Скрываем все другие меню
            }
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

            if (!response.ok) {
                throw new Error("Ошибка обновления статуса");
            }

            // Обновляем статус на фронте
            orderBlock.querySelector(".order-status").textContent = newStatus;

            // Скрываем меню после выбора
            orderBlock.querySelector(".status-menu").style.display = "none";
        } catch (error) {
            console.error("Ошибка обновления статуса:", error);
        }
    }
});