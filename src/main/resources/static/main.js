document.addEventListener("DOMContentLoaded", () => {
  const cart = document.getElementById("cart");
  const authButton = document.getElementById("auth-button");
  const mainContent = document.querySelector(".main-content");
  const cartTotal = document.getElementById("cart-total");
  const sidebarImages = document.querySelectorAll(".sidebar img");
  const loadingSpinner = document.getElementById('loading-spinner');
  const productsContainer = document.getElementById('products-container');

  const renderedProducts = new Map(); // Храним товары по их urlId
  let currentCategory = null; // Текущая выбранная категория

  function showSpinner() {
    loadingSpinner.style.display = 'block';
  }

  function hideSpinner() {
    loadingSpinner.style.display = 'none';
  }

  function clearProducts() {
    productsContainer.innerHTML = '';
  }


  // Функция добавления/обновления товара на странице
  function addOrUpdateProductOnPage(product) {
    if (product.category !== currentCategory) return;

    if (renderedProducts.has(product.urlId)) {
      const existingProduct = renderedProducts.get(product.urlId);
      const priceElement = existingProduct.querySelector(".product-price");
      if (priceElement) {
        priceElement.innerHTML = `Цена: ₽${product.price || "Не указана"}`;
      }
      return;
    }

    const contentBox = document.createElement("div");
    const characteristicsHtml = Object.entries(product.characteristics || {})
        .map(([key, value]) => `<p><strong>${key}:</strong> ${value}</p>`)
        .join("");

    contentBox.className = "content-box fade-in"; // ← здесь!

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

    productsContainer.appendChild(contentBox);
    renderedProducts.set(product.urlId, contentBox);

    // Анимация появления
    setTimeout(() => {
      contentBox.classList.add("show");
    }, 100);
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
    }, function(error) {
      console.error("Ошибка подключения WebSocket:", error);
      setTimeout(setupWebSocket, 5000); // Переподключение
    });
  }


  function subscribeToCategory(category) {
    if (currentSubscription) {
      currentSubscription.unsubscribe(); // Отписка от старой категории
    }

    if (!category) return;

    currentSubscription = stompClient.subscribe('/topic/products/' + category, function(message) {
      const productList = JSON.parse(message.body);
      console.log("Получен список товаров по WebSocket:", productList);

      if (productList.length === 0) {
        mainContent.innerHTML = "<p>Товары не найдены.</p>";
      } else {
        productList.forEach(product => addOrUpdateProductOnPage(product));
      }

      hideSpinner(); // <-- Прячем спиннер ТОЛЬКО когда товары реально пришли
    });

  }

  // Обработчик кликов по категориям в sidebar
  sidebarImages.forEach(image => {
    image.addEventListener("click", () => {
      const category = image.dataset.category;
      if (category !== currentCategory) {
        currentCategory = category;

        clearProducts();   // Очищаем карточки
        showSpinner();     // Показываем спиннер

        renderedProducts.clear();
        subscribeToCategory(category);
        fetchAndRenderProducts(category);
      }
    });
  });

  function fetchAndRenderProducts(category) {
    renderedProducts.clear();
    showSpinner();

    fetch("/parse?category=" + category, { method: "POST" })
        .then(() => {
          console.log("Парсинг отправлен, ждём WebSocket сообщения");
          // Больше ничего не делаем!
        })
        .catch(err => {
          hideSpinner();
          console.error("Ошибка при парсинге:", err);
          mainContent.innerHTML = "<p>Ошибка при загрузке товаров.</p>";
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