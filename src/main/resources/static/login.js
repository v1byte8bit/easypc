document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    const phoneInput = document.getElementById("phone");
    const passwordInput = document.getElementById("password");
    const errorDiv = document.getElementById("error");

    form.addEventListener("submit", (event) => {
        event.preventDefault(); // Останавливаем стандартную отправку формы

        const phone = phoneInput.value.trim();
        const password = passwordInput.value.trim();

        console.log("Отправляем данные: ", { phone, password });

        if (!phone || !password) {
            alert("Пожалуйста, заполните все поля!");
            return;
        }

        // Используем FormData для отправки данных формы
        const formData = new FormData(form);

        fetch(form.action, {
            method: "POST",
            body: formData,
            credentials: "include",
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorMessage => {
                        throw new Error(errorMessage.message || "Ошибка при входе");
                    });
                }
                return response.json(); // Ожидаем JSON-ответ
            })
            .then(data => {
                console.log("Ответ сервера:", data);

                // Проверка роли и перенаправление
                if (data.role === "buyer") {
                    window.location.href = "/main";
                } else if (data.role === "assembler") {
                    window.location.href = "/assembler/profile";
                } else {
                    throw new Error("Доступ запрещен. Роль не подходит.");
                }
            })
            .catch(error => {
                console.error("Ошибка:", error);
                errorDiv.textContent = error.message || "Произошла ошибка при попытке войти.";
            });
    });
});
