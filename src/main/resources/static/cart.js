document.addEventListener("DOMContentLoaded", () => {
    const mainContent = document.querySelector(".frame-1"); // Центральный блок с прокруткой
    const cartTotal = document.querySelector(".frame-3"); // Блок для суммы корзины
    const orderButton = document.querySelector(".frame-4"); // Кнопка "Заказать"
    const saveBuildButton = document.querySelector(".frame-5"); // Кнопка "Сохранить сборку"
    const renderedProducts = new Map(); // Храним товары по их urlId

    // Добавляем обработчик на кнопку "Заказать"
    orderButton.addEventListener("click", () => {
        fetch("/making/order", {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include"
        })
            .then(() => {
                window.location.href = "/making/order"; // Перенаправляем пользователя
            })
            .catch(error => console.error("Ошибка при оформлении заказа:", error));
    });

    saveBuildButton.addEventListener("click", () => {
        // Запрашиваем у пользователя название сборки
        const buildName = prompt("Введите название сборки:");

        if (buildName) {
            // Создаем объект для отправки
            const requestPayload = {
                buildName: buildName
            };

            // Отправляем запрос на сервер
            fetch("/cart/save_build", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestPayload),
                credentials: "include" // Убедитесь, что сессия передается через cookies
            })
                .then(response => {
                    if (response.ok) {
                        alert("Сборка успешно сохранена");
                        // Перенаправление или обновление UI
                    } else {
                        alert("Ошибка при сохранении сборки");
                    }
                })
                .catch(error => {
                    console.error("Ошибка при отправке запроса:", error);
                    alert("Произошла ошибка");
                });
        } else {
            alert("Название сборки не может быть пустым");
        }
    });

    // Функция загрузки товаров из корзины
    function loadCartItems() {
        fetch("/cart/items", {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include"
        })
            .then(response => {
                if (!response.ok) throw new Error("Ошибка загрузки корзины");
                return response.json();
            })
            .then(products => {
                console.log("Загруженные товары:", products);
                mainContent.innerHTML = ""; // Очищаем контейнер перед загрузкой новых товаров
                renderedProducts.clear();

                products.forEach(product => {
                    addOrUpdateProductOnPage(product);
                });

                updateCartTotal(); // После загрузки товаров обновляем сумму корзины
            })
            .catch(error => console.error("Ошибка при загрузке корзины:", error));
    }

    // Функция добавления товара на страницу
    function addOrUpdateProductOnPage(product) {
        if (renderedProducts.has(product.urlId)) return;

        const contentBox = document.createElement("div");
        contentBox.className = "product-box";
        contentBox.setAttribute("data-id", product.urlId);
        const characteristicsHtml = Object.entries(product.characteristics || {})
            .map(([key, value]) => `<p><strong>${key}:</strong> ${value}</p>`)
            .join("");

        contentBox.innerHTML = `
  <img src="${product.img}" alt="${product.name}" class="content-image" />
  <div class="content-info-wrapper">
    <div class="content-text">
      <h3>${product.name || "Не указано"}</h3>
      <p class="product-price">Цена: ₽${product.price || "Не указана"}</p>
      <div class="product-characteristics">
        ${characteristicsHtml}
      </div>
    </div>
    <div class="content-actions">
      <div class="quantity-controls">
        <button class="decrease" data-id="${product.urlId}">-</button>
        <span class="quantity" data-id="${product.urlId}">${product.quantity}</span>
        <button class="increase" data-id="${product.urlId}">+</button>
      </div>
      <button class="remove-button" data-id="${product.urlId}">Удалить</button>
    </div>
  </div>
`;
        const decreaseButton = contentBox.querySelector(".decrease");
        const increaseButton = contentBox.querySelector(".increase");
        const quantitySpan = contentBox.querySelector(".quantity");

        decreaseButton.addEventListener("click", () => updateQuantity(product.urlId, quantitySpan, -1));
        increaseButton.addEventListener("click", () => updateQuantity(product.urlId, quantitySpan, 1));

        const removeButton = contentBox.querySelector(".remove-button");
        removeButton.addEventListener("click", () => removeFromCart(product.urlId));

        mainContent.appendChild(contentBox);
        renderedProducts.set(product.urlId, contentBox);
    }

    // Функция изменения количества товара
    function updateQuantity(urlId, quantitySpan, change) {
        let currentQuantity = parseInt(quantitySpan.textContent);
        let newQuantity = currentQuantity + change;

        if (newQuantity < 1) return;

        console.log(`Отправка запроса на обновление количества: ${newQuantity}`);

        fetch(`/cart/updateQuantity?sourceId=${urlId}&quantity=${newQuantity}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "include"
        })
            .then(response => {
                if (!response.ok) {
                    console.error("Ошибка при запросе обновления количества", response);
                    throw new Error("Ошибка обновления количества");
                }
                return response.text();
            })
            .then(() => {
                console.log("Количество успешно обновлено");
                quantitySpan.textContent = newQuantity; // Обновляем UI
                updateCartTotal(); // Обновляем сумму корзины
            })
            .catch(error => console.error("Ошибка при обновлении количества:", error));
    }

    // Функция удаления товара из корзины
    function removeFromCart(urlId) {
        fetch(`/cart/remove?sourceId=${urlId}`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            credentials: "include"
        })
            .then(response => {
                if (!response.ok) throw new Error("Ошибка удаления товара");
                return response.text();
            })
            .then(() => {
                console.log(`Товар ${urlId} удален`);
                renderedProducts.get(urlId)?.remove();
                renderedProducts.delete(urlId);
                updateCartTotal();
                localStorage.removeItem("selectedCpuSocket");
                localStorage.removeItem("selectedMemoryType");
                localStorage.removeItem("tdp_cpu");
                localStorage.removeItem("tdp_gpu");
            })
            .catch(error => console.error("Ошибка при удалении товара:", error));
    }

    // Функция обновления суммы корзины и отображения кнопки "Заказать"
    function updateCartTotal() {
        fetch("/cart/total", {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include"
        })
            .then(response => {
                if (!response.ok) throw new Error("Ошибка при получении суммы корзины");
                return response.json();
            })
            .then(data => {
                console.log("Сумма корзины:", data);
                if (data !== null && parseFloat(data) > 0) {
                    cartTotal.textContent = `₽${data}`;
                    cartTotal.style.display = "block";
                    orderButton.style.display = "flex"; // Показываем кнопку "Заказать"
                } else {
                    cartTotal.style.display = "none";
                    orderButton.style.display = "none"; // Скрываем кнопку "Заказать", если корзина пуста
                }
            })
            .catch(error => {
                console.error("Ошибка получения суммы корзины:", error);
                cartTotal.style.display = "none";
                orderButton.style.display = "none";
            });
    }

    // Загружаем товары корзины при загрузке страницы
    loadCartItems();
});