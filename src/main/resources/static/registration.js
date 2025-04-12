document.addEventListener('DOMContentLoaded', () => {
  // Получаем элементы формы
  const registerButton = document.querySelector('.div7');
  const phoneInput = document.getElementById('phone');
  const passwordInput = document.getElementById('password');
  const confirmPasswordInput = document.getElementById('confirmPassword');
  const roleRadios = document.getElementsByName('role'); // Получаем все радио-кнопки с name="role"

  registerButton.addEventListener('click', async (event) => {
    event.preventDefault(); // Предотвращаем стандартное поведение кнопки

    // Получаем значения из формы
    const phone = phoneInput.value.trim();
    const password = passwordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();

    // Проверяем, какая роль выбрана
    let role = null;
    for (let radio of roleRadios) {
      if (radio.checked) {
        role = radio.value; // Получаем значение выбранной радио-кнопки
        break;
      }
    }

    // Проверка, что поля не пустые
    if (!phone || !password || !confirmPassword) {
      alert('Пожалуйста, заполните все поля!');
      return;
    }



    // Проверка длины пароля
    if (password.length < 6) {
      alert('Пароль должен содержать не менее 6 символов!');
      return;
    }

    // Проверка совпадения паролей
    if (password !== confirmPassword) {
      alert('Пароли не совпадают!');
      return;
    }

    // Проверка, что выбрана хотя бы одна роль
    if (!role) {
      alert('Пожалуйста, выберите вашу роль (покупатель или сборщик).');
      return;
    }

    // Данные для отправки на сервер
    const data = {
      phone: phone,
      password: password,
      confirmPassword: confirmPassword,
      role: role  // Роль пользователя (buyer или assembler)
    };

    try {
      // Отправка данных на сервер через POST-запрос
      const response = await fetch("/api/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
      });

      const result = await response.json();

      if (response.status === 201) {
        alert(result.message);
        window.location.href = "/login";
      } else {
        alert(result.message || 'Ошибка при регистрации.'); // Ошибка
      }
    } catch (error) {
      alert('Произошла ошибка при регистрации. Попробуйте позже.');
      console.error(error);
    }
  });
});
