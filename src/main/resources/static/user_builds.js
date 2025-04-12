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
        const response = await fetch("/api/builds");
        if (!response.ok) {
            throw new Error("Ошибка загрузки сборок");
        }
        const builds = await response.json();
        renderBuilds(builds);
    } catch (error) {
        console.error("Ошибка загрузки данных:", error);
    }

    function renderBuilds(builds) {
        contentContainer.innerHTML = ""; // Очищаем контейнер перед рендерингом

        builds.forEach(build => {
            const buildBlock = document.createElement("div");
            buildBlock.classList.add("build-block"); // Используем новый класс для сборок

            buildBlock.innerHTML = `
            <div class="build-info">
                <!-- Картинка слева -->
                <img src="/static/free-icon-gaming-pc-7177534-20.png" class="build-image" />
                
                <!-- Название сборки -->
                <div class="build-details">
                    <div class="build-name">${build.name}</div>
                </div>

                <div class="build-column">
                <!-- Кнопки управления -->
                <div class="build-price">${build.totalPrice || "Не указана"}₽</div>
                <div class="build-action">
                    <img src="/static/delete.png" class="delete-btn" data-id="${build.id}" title="Удалить сборку">
                    <img src="/static/tocart.png" class="to-cart-btn" data-id="${build.id}" title="Добавить в корзину">
                </div>
                </div>
            </div>

            <!-- Аккордеон с товарами в сборке -->
            <div class="accordion-content" id="build-${build.id}" style="display: none;"></div>
        `;

            contentContainer.appendChild(buildBlock);

            // Добавляем обработчик клика для раскрытия товаров в сборке
            buildBlock.addEventListener("click", () => toggleBuildItems(build.id, build.items, buildBlock));

            // Обработчик удаления сборки
            const deleteButton = buildBlock.querySelector(".delete-btn");
            deleteButton.addEventListener("click", async (event) => {
                event.stopPropagation(); // Чтобы не срабатывал аккордеон
                await deleteBuild(build.id, buildBlock);
            });

            // Обработчик добавления товаров в корзину
            const toCartButton = buildBlock.querySelector(".to-cart-btn");
            toCartButton.addEventListener("click", async (event) => {
                event.stopPropagation();
                await addBuildToCart(build.id);
            });
        });
    }

    function toggleBuildItems(buildId, items, buildBlock) {
        const accordion = document.getElementById(`build-${buildId}`);

        if (accordion.style.display === "none" || accordion.innerHTML === "") {
            renderBuildItems(accordion, items);
            accordion.style.display = "flex"; // Показываем товары
        } else {
            accordion.style.display = "none"; // Скрываем, если уже открыто
        }
    }

    function renderBuildItems(container, items) {
        container.innerHTML = ""; // Очищаем перед вставкой новых элементов

        items.forEach(item => {
            const itemBlock = document.createElement("div");
            itemBlock.classList.add("build-item-block");

            itemBlock.innerHTML = `
            <!-- Картинка товара -->
            <img class="img_item" src="${item.imageUrl}" alt="${item.name}" />
            <!-- Название и категория -->
            <div class="item-details">
                <div class="item-name">${item.name}</div>
            </div>
            <!-- Цена -->
            <div class="item-action">
                <div class="item-price">${item.price || "Не указана"}₽</div>
                <div class="item-quantity">Количество: ${item.quantity}</div>
            </div>
        `;

            container.appendChild(itemBlock);
        });
    }

    async function deleteBuild(buildId, buildBlock) {
        if (!confirm("Вы действительно хотите удалить эту сборку?")) {
            return;
        }

        try {
            const response = await fetch(`/builds/${buildId}`, { method: "DELETE" });

            if (!response.ok) {
                throw new Error("Ошибка удаления сборки");
            }

            buildBlock.remove(); // Удаляем блок из DOM
        } catch (error) {
            console.error("Ошибка удаления сборки:", error);
        }
    }

    async function addBuildToCart(buildId) {
        try {
            const response = await fetch(`/builds/${buildId}/tocart`, { method: "POST" });

            if (!response.ok) {
                throw new Error("Ошибка добавления в корзину");
            }

            alert("Товары успешно добавлены в корзину!");
        } catch (error) {
            console.error("Ошибка добавления товаров в корзину:", error);
        }
    }
});

document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
});