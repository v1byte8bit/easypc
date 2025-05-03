document.addEventListener("DOMContentLoaded", async () => {
  const cart = document.getElementById("cart");
  const authButton = document.getElementById("auth-button");
  const mainContent = document.querySelector(".main-content");
  const cartTotal = document.getElementById("cart-total");
  const sidebarImages = document.querySelectorAll(".sidebar img");
  const loadingSpinner = document.getElementById('loading-spinner');
  const productsContainer = document.getElementById('products-container');
  const isAuthenticated = authButton?.dataset.authenticated === "true";
  const renderedProducts = new Map();
  let currentCategory = null;

  const categoryLocalStorageMap = {
    cpu: {
      "Сокет": "selectedCpuSocket",
      "Тепловыделение (TDP)": "tdp_cpu"
    },
    gpu: {
      "Тепловыделение (TDP)": "tdp_gpu"
    },
    motherboard: {
      "Тип памяти": "selectedMemoryType"
    }
  };

  async function restoreCpuSocketFromCart() {
    if (!isAuthenticated) return;

    try {
      const response = await fetch("/cart/items", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        credentials: "include"
      });

      if (!response.ok) throw new Error("Ошибка получения корзины");

      const items = await response.json();

      // Восстанавливаем сокет CPU
      const cpuItem = items.find(item => item.category === "cpu" && item.characteristics?.["Сокет"]);
      if (cpuItem) {
        localStorage.setItem("selectedCpuSocket", cpuItem.characteristics["Сокет"]);
      } else {
        localStorage.removeItem("selectedCpuSocket");
      }

      // Восстанавливаем тип памяти
      const motherboardOrRamItem = items.find(item =>
          (item.category === "motherboard" || item.category === "ram") &&
          item.characteristics?.["Тип памяти"]
      );
      if (motherboardOrRamItem) {
        localStorage.setItem("selectedMemoryType", motherboardOrRamItem.characteristics["Тип памяти"]);
      } else {
        localStorage.removeItem("selectedMemoryType");
      }

      const cpuTdp = items.find(item => item.category === "cpu")?.characteristics?.["Тепловыделение (TDP)"];
      const gpuTdp = items.find(item => item.category === "gpu")?.characteristics?.["Тепловыделение (TDP)"];

      if (cpuTdp) {
        const match = cpuTdp.match(/\d+/);
        if (match) localStorage.setItem("tdp_cpu", match[0]);
      }

      if (gpuTdp) {
        const match = gpuTdp.match(/\d+/);
        if (match) localStorage.setItem("tdp_gpu", match[0]);
      }
    } catch (err) {
      console.error("Ошибка при восстановлении данных из корзины:", err);
    }
  }

  if (isAuthenticated) {
    updateCartTotal();
    restoreCpuSocketFromCart();
    updateSidebarMarksFromCart();
  } else {
    localStorage.removeItem("selectedCpuSocket");
    localStorage.removeItem("selectedMemoryType");
    localStorage.removeItem("tdp_cpu");
    localStorage.removeItem("tdp_gpu");
  }

  function showAuthNotice() {
    const notice = document.getElementById("auth-notice");
    if (notice) {
      notice.style.display = "block";
      setTimeout(() => {
        notice.style.display = "none";
      }, 3000);
    }
  }

  // Функция добавления/обновления товара на странице
  function addOrUpdateProductOnPage(product) {
    if (product.category !== currentCategory) return;

    if (product.category === "motherboard") {
      const requiredSocket = localStorage.getItem("selectedCpuSocket");
      const motherboardSocket = product.characteristics?.["Сокет"];
      if (requiredSocket && motherboardSocket && requiredSocket !== motherboardSocket) {
        return;
      }
    }

    if (product.category === "ram") {
      const requiredMemoryType = localStorage.getItem("selectedMemoryType");
      const ramType = product.characteristics?.["Тип памяти"];
      if (requiredMemoryType && ramType && requiredMemoryType !== ramType) {
        return;
      }
    }

    if (product.category === "psu") {
      const cpuTdp = parseInt(localStorage.getItem("tdp_cpu")) || 0;
      const gpuTdp = parseInt(localStorage.getItem("tdp_gpu")) || 0;
      const totalTdp = cpuTdp + gpuTdp;

      if (totalTdp > 0) {
        const recommendedWattage = Math.ceil(totalTdp * 1.3);
        const wattRaw = product.characteristics?.["Мощность"];
        const watt = wattRaw ? parseInt(wattRaw.match(/\d+/)?.[0]) : 0;

        // Пропускаем, если БП слишком слабый или чрезмерно мощный
        if (watt < recommendedWattage || watt > recommendedWattage + 200) {
          return;
        }
      }
    }

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

    contentBox.className = "content-box fade-in";

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
    if (!isAuthenticated) {
      showAuthNotice();
      return;
    }

    if (!product.urlId) {
      console.error("Ошибка: у продукта нет urlId");
      return;
    }

    if (product.category === "cpu" && product.characteristics?.["Сокет"]) {
      localStorage.setItem("selectedCpuSocket", product.characteristics["Сокет"]);
    }

    if (product.category === "motherboard" && product.characteristics?.["Тип памяти"]) {
      localStorage.setItem("selectedMemoryType", product.characteristics["Тип памяти"]);
    }

    if (product.category === "cpu" || product.category === "gpu") {
      const tdpRaw = product.characteristics?.["Тепловыделение (TDP)"];
      if (tdpRaw) {
        const match = tdpRaw.match(/\d+/);
        if (match) {
          localStorage.setItem(`tdp_${product.category}`, match[0]);
        }
      }
    }

    const mappings = categoryLocalStorageMap[product.category];
    if (mappings && product.characteristics) {
      Object.entries(mappings).forEach(([characteristicKey, storageKey]) => {
        const value = product.characteristics[characteristicKey];
        if (value) {
          localStorage.setItem(storageKey, value);
        }
      });
    }

    fetch("/add?sourceId=" + product.urlId, {
      method: "POST",
      headers: {"Content-Type": "application/json"}
    })
        .then(response => {
          if (!response.ok) {
            throw new Error("Ошибка при добавлении в корзину");
          }
          return response.text();
        })
        .then(() => {
          updateCartTotal();
          updateSidebarMarksFromCart();
        })
        .catch(error => {
          console.error("Ошибка:", error);
        });
  }

  // Обновление суммы корзины
  function updateCartTotal() {
    if (!isAuthenticated) {
      if (cartTotal) {
        cartTotal.textContent = "Корзина";
        cartTotal.style.display = "block";
      }
      return;
    }

    fetch("/cart/total", {
      method: "GET",
      headers: {"Content-Type": "application/json"},
      credentials: "include"
    })
        .then(response => response.json())
        .then(data => {
          if (!cartTotal) return;

          const total = typeof data === "number" ? data : parseFloat(data.total);
          if (!isNaN(total)) {
            cartTotal.textContent = `${total}₽`;
            cartTotal.style.display = "block";
          } else {
            cartTotal.textContent = "Корзина";
            cartTotal.style.display = "block";
          }
        })
        .catch(() => {
          if (cartTotal) {
            cartTotal.textContent = "Корзина";
            cartTotal.style.display = "block";
          }
        });
  }

  // Настройка WebSocket для получения продуктов
  let stompClient = null;
  let currentSubscription = null;

  function setupWebSocket() {
    const socket = new SockJS('/websocket-endpoint');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
      console.log('Connected to WebSocket: ' + frame);
    }, function (error) {
      console.error("Ошибка подключения WebSocket:", error);
      setTimeout(setupWebSocket, 5000); // Переподключение
    });
  }

  // Настроить WebSocket при загрузке страницы
  setupWebSocket();

  function subscribeToCategory(category) {
    if (currentSubscription) {
      currentSubscription.unsubscribe(); // Отписка от старой категории
    }

    if (!category) return;

    currentSubscription = stompClient.subscribe('/topic/products/' + category, function (message) {
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

    fetch("/parse?category=" + category, {method: "POST"})
        .then(() => {
        })
        .catch(err => {
          hideSpinner();
          mainContent.innerHTML = "<p>Ошибка при загрузке товаров.</p>";
        });
  }

  // Переход в корзину
  if (cart) {
    cart.addEventListener("click", () => {
      if (!isAuthenticated) {
        showAuthNotice();
      } else {
        window.location.href = "/cart";
      }
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

  function showSpinner() {
    loadingSpinner.style.display = 'block';
    const hint = document.getElementById("category-hint");
    if (hint) hint.style.display = "none";
  }

  function hideSpinner() {
    loadingSpinner.style.display = 'none';
  }

  function clearProducts() {
    const productsContainer = document.getElementById("products-container");
    const hint = document.getElementById("category-hint");

    // Очищаем товары
    if (productsContainer) productsContainer.innerHTML = "";

    // Показываем подсказку
    if (hint) hint.style.display = "block";
  }

  async function updateSidebarMarksFromCart() {
    if (!isAuthenticated) return;

    try {
      const response = await fetch("/cart/items", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        credentials: "include"
      });

      if (!response.ok) throw new Error("Ошибка получения корзины");

      const items = await response.json();
      const categoriesInCart = new Set(items.map(item => item.category));

      document.querySelectorAll(".sidebar-item").forEach(item => {
        const category = item.dataset.category;
        if (categoriesInCart.has(category)) {
          item.classList.add("checked");
        } else {
          item.classList.remove("checked");
        }
      });
    } catch (err) {
    }
  }

  updateCartTotal();
});