document.addEventListener("DOMContentLoaded", () => {
  const cart = document.getElementById("cart");
  const authButton = document.getElementById("auth-button");
  const mainContent = document.querySelector(".main-content");
  const cartTotal = document.getElementById("cart-total");
  const sidebarImages = document.querySelectorAll(".sidebar img"); // Все картинки в sidebar

  const renderedProducts = new Map(); // Храним товары по их urlId
  let currentCategory = null; // Текущая выбранная категория

  // Функция добавления/обновления товара на странице
  function addOrUpdateProductOnPage(product) {
    if (product.category !== currentCategory) return; // Если категория не совпадает, не отображаем товар

    if (renderedProducts.has(product.urlId)) {
      // Если товар уже есть, обновляем только цену
      const existingProduct = renderedProducts.get(product.urlId);
      const priceElement = existingProduct.querySelector(".product-price");
      if (priceElement) {
        priceElement.innerHTML = `Цена: ₽${product.price || "Не указана"}`;
      }
      return;
    }

    // Создаем новый элемент товара
    const contentBox = document.createElement("div");
    const characteristicsHtml = Object.entries(product.characteristics || {})
        .map(([key, value]) => `<p><strong>${key}:</strong> ${value}</p>`)
        .join(""); // Если нет характеристик, будет пустая строка

    contentBox.className = "content-box";
    contentBox.setAttribute("data-id", product.urlId);

    contentBox.innerHTML = `
      <img src="${product.img}" alt="${product.name}" class="content-image" />
      <div class="content-text">
        <h3>${product.name || "Не указано"}</h3>
        <p class="product-price">Цена: ₽${product.price || "Не указана"}</p>
        <div class="product-characteristics">
      ${characteristicsHtml}
    </div>
      </div>
      <button class="add-button" data-id="${product.urlId}">В корзину</button>
    `;

    const addButton = contentBox.querySelector(".add-button");
    addButton.addEventListener("click", () => {
      addToCart(product);
    });

    mainContent.appendChild(contentBox);
    renderedProducts.set(product.urlId, contentBox); // Запоминаем товар
  }

  // Функция добавления товара в корзину
  function addToCart(product) {
    if (!product.urlId) {
      console.error("Ошибка: у продукта нет urlId");
      return;
    }

    fetch("/add?sourceId=" + product.urlId, {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    })
        .then(response => {
          if (!response.ok) {
            throw new Error("Ошибка при добавлении в корзину");
          }
          return response.text();
        })
        .then(() => {
          alert("Товар добавлен в корзину");
          updateCartTotal(); // Обновляем сумму корзины после добавления
        })
        .catch(error => {
          console.error("Ошибка:", error);
          alert("Не удалось добавить товар в корзину");
        });
  }

  // Обновление суммы корзины
  function updateCartTotal() {
    fetch("/cart/total", {
      method: "GET",
      headers: { "Content-Type": "application/json" },
      credentials: "include"
    })
        .then(response => response.json())
        .then(data => {
          if (!cartTotal) {
            console.error("cartTotal не найден!");
            return;
          }

          const total = typeof data === "number" ? data : parseFloat(data.total);
          if (!isNaN(total)) {
            cartTotal.textContent = `${total}₽`;
            cartTotal.style.display = "block";
          } else {
            cartTotal.style.display = "none";
          }
        })
        .catch(error => {
          console.error("Ошибка получения суммы корзины:", error);
          cartTotal.style.display = "none";
        });
  }

  // Настройка WebSocket для получения продуктов
  let stompClient = null;
  let currentSubscription = null;

  function setupWebSocket() {
    const socket = new SockJS('/websocket-endpoint');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
      console.log('Connected to WebSocket: ' + frame);
      subscribeToCategory(currentCategory); // подписываемся сразу на текущую категорию
    }, function(error) {
      console.error("Ошибка подключения WebSocket:", error);
      setTimeout(setupWebSocket, 5000); // Переподключение
    });
  }

  function subscribeToCategory(category) {
    if (currentSubscription) {
      currentSubscription.unsubscribe(); // Отписка от старой категории
    }

    if (!category) return; // Нет выбранной категории — нечего подписывать

    currentSubscription = stompClient.subscribe('/topic/products/' + category, function(message) {
      const productList = JSON.parse(message.body);
      console.log("Получен список товаров по WebSocket:", productList);

      productList.forEach(product => {
        if (!renderedProducts.has(product.urlId)) {
          addOrUpdateProductOnPage(product);
          saveProductToLocalStorage(product);
        } else {
          addOrUpdateProductOnPage(product);
        }
      });
    });
  }

  // Обработчик кликов по категориям в sidebar
  sidebarImages.forEach(image => {
    image.addEventListener("click", () => {
      const category = image.dataset.category;
      if (category !== currentCategory) {
        currentCategory = category;
        renderedProducts.clear();
        mainContent.innerHTML = "";

        subscribeToCategory(category); // <--- ПЕРЕПОДПИСКА ПО НОВОЙ КАТЕГОРИИ

        fetchAndRenderProducts(category); // Загрузка по новой категории
      }
    });
  });


  function fetchAndRenderProducts(category) {
    fetch("/parse?category=" + category, { method: "POST" })
        .then(res => res.json()) // Получаем список продуктов
        .then(product => {
          mainContent.innerHTML = ""; // Очищаем старые товары
          renderedProducts.clear();  // Очищаем кешированные товары
          localStorage.setItem("products", JSON.stringify(product)); // Сохраняем в localStorage

          // Добавляем товары на страницу
          product.forEach(product => addOrUpdateProductOnPage(product));

          // Запускаем WebSocket для получения дальнейших обновлений
          setupWebSocket();
        })
        .catch(err => {
          console.error("Ошибка при парсинге:", err);
        });
  }

  // Настроить WebSocket при загрузке страницы
  setupWebSocket();

  // Обновляем сумму корзины при загрузке страницы
  updateCartTotal();

  // Переход в корзину
  if (cart) {
    cart.addEventListener("click", () => {
      window.location.href = "/cart";
    });
  }

  // Управление авторизацией
  if (authButton) {
    const isAuthenticated = authButton.dataset.authenticated === "true";
    if (isAuthenticated) {
      authButton.textContent = "Личный кабинет";
      authButton.addEventListener("click", () => {
        window.location.href = "/user/profile";
      });
    } else {
      authButton.textContent = "Авторизация";
      authButton.addEventListener("click", () => {
        window.location.href = "/login";
      });
    }
  }
});
