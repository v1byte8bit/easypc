ymaps.ready(init);

function init() {
    var myMap = new ymaps.Map("map", {
        center: [55.751574, 37.573856], // Москва по умолчанию
        zoom: 10
    });

    var myPlacemark;
    var addressInput = document.getElementById("address");
    var suggestionsList = document.getElementById("suggestions");
    var apiKey = "f1c36d0b-0581-4104-b504-d828f3368d59";
    let debounceTimer;

    // Автодополнение адреса через Yandex Geocoder API
    addressInput.addEventListener("input", function () {
        clearTimeout(debounceTimer);

        const query = addressInput.value.trim();
        if (query.length < 3) {
            suggestionsList.innerHTML = "";
            suggestionsList.style.display = "none"; // Скрываем список, если мало символов
            return;
        }

        debounceTimer = setTimeout(() => {
            fetch(`https://geocode-maps.yandex.ru/1.x/?apikey=${apiKey}&geocode=${query}&format=json`)
                .then(response => response.json())
                .then(data => {
                    suggestionsList.innerHTML = "";

                    const geoObjects = data.response.GeoObjectCollection.featureMember;
                    if (geoObjects.length === 0) {
                        suggestionsList.style.display = "none";
                        return;
                    }

                    geoObjects.forEach(item => {
                        const address = item.GeoObject.name + ", " + item.GeoObject.description;
                        const li = document.createElement("li");
                        li.textContent = address;
                        li.classList.add("suggestion-item");

                        li.addEventListener("click", () => {
                            addressInput.value = address;
                            suggestionsList.innerHTML = "";
                            suggestionsList.style.display = "none"; // Скрываем список после выбора
                            geocodeAddress(address);
                        });

                        suggestionsList.appendChild(li);
                    });

                    suggestionsList.style.display = "block"; // Показываем список
                })
                .catch(error => console.error("Ошибка получения подсказок:", error));
        }, 300);
    });

    // Скрытие списка при клике вне него
    document.addEventListener("click", function (event) {
        if (!suggestionsList.contains(event.target) && event.target.id !== "address") {
            suggestionsList.style.display = "none";
        }
    });

    // Обработчик изменения адреса вручную
    addressInput.addEventListener("change", function () {
        geocodeAddress(addressInput.value);
    });

    // Функция геокодирования адреса и установки метки
    function geocodeAddress(address) {
        ymaps.geocode(address).then(function (res) {
            var firstGeoObject = res.geoObjects.get(0);

            if (!firstGeoObject) {
                alert("Не удалось найти адрес!");
                return;
            }

            var coords = firstGeoObject.geometry.getCoordinates();
            setPlacemark(coords);
            myMap.setCenter(coords, 15);
        }).catch(function (err) {
            console.error("Ошибка геокодирования:", err);
        });
    }

    // Установка метки при клике по карте
    myMap.events.add("click", function (e) {
        var coords = e.get("coords");
        setPlacemark(coords);

        ymaps.geocode(coords).then(function (res) {
            var firstGeoObject = res.geoObjects.get(0);
            if (firstGeoObject) {
                addressInput.value = firstGeoObject.getAddressLine();
            }
        }).catch(function (err) {
            console.error("Ошибка обратного геокодирования:", err);
        });
    });

    // Функция для установки или обновления метки
    function setPlacemark(coords) {
        if (myPlacemark) {
            myPlacemark.geometry.setCoordinates(coords);
        } else {
            myPlacemark = new ymaps.Placemark(coords, {}, { draggable: true });
            myMap.geoObjects.add(myPlacemark);

            myPlacemark.events.add("dragend", function () {
                var newCoords = myPlacemark.geometry.getCoordinates();
                ymaps.geocode(newCoords).then(function (res) {
                    var firstGeoObject = res.geoObjects.get(0);
                    if (firstGeoObject) {
                        addressInput.value = firstGeoObject.getAddressLine();
                    }
                }).catch(function (err) {
                    console.error("Ошибка обратного геокодирования:", err);
                });
            });
        }
    }

    // Подтверждение заказа
    document.querySelector(".div18").addEventListener("click", function () {
        if (myPlacemark) {
            var address = addressInput.value;
            var phone = document.querySelector(".input1").value.trim();


            if (!phone) {
                alert("Введите номер телефона!");
                return;
            }

            fetch("/order/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    address: address,
                    phone: phone,
                })
            })
                .then(response => response.text())
                .then(data => alert("Заказ подтвержден: " + data))
                .catch(error => console.error("Ошибка:", error));
        } else {
            alert("Выберите адрес на карте.");
        }
    });
}