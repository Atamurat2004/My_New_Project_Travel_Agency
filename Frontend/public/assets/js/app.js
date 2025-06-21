// --- LOCAL STORAGE AUTH ---
let token = localStorage.getItem('token');
let userEmail = localStorage.getItem('userEmail');
let userRole = localStorage.getItem('userRole');

// Базовый URL для API
const API_BASE_URL = 'http://localhost:8080';

const cityImageMap = {
    'москва': 'moscow',
    'владивосток': 'vladivostok',
    'горно-алтайск': 'altai',
    'сочи': 'sochi',
    'санкт-петербург': 'spb',
    'казань': 'kazan',
    'пятигорск': 'kavminvody',
    'петрозаводск': 'karelia',
    'калининград': 'kaliningrad',
    'великий новгород': 'novgorod'
};

// Новый фронтенд: загрузка и фильтрация туров, галерея, лучшие предложения
let tours = [];
let filteredTours = [];

// Категории туров (можно расширить)
const categories = [
    "Экскурсионные", "Пляжные", "Горные", "Экзотика", "Семейные"
];

// Логин
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.onsubmit = async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;
        try {
            const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
            });
            let data;
            try { data = await res.json(); } catch (e) { throw new Error('Ошибка входа'); }
            if (!res.ok) throw new Error(data && data.error ? data.error : 'Ошибка входа');
            token = data.token;
            userEmail = email;
            userRole = data.role;
            localStorage.setItem('token', token);
            localStorage.setItem('userEmail', userEmail);
            localStorage.setItem('userRole', userRole);
            // Закрываем модалку
            const loginModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('loginModal'));
            loginModal.hide();
            showAccountButtons();
            if (userRole === 'ROLE_ADMIN') {
                showAdminPanel();
            } else {
                showUserPanel();
            }
        } catch (err) {
            alert('Ошибка входа: ' + err.message);
        }
    };
}

// Регистрация
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.onsubmit = async (e) => {
        e.preventDefault();
        const name = document.getElementById('registerName').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        try {
            const res = await fetch(`${API_BASE_URL}/api/auth/register`, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
            });
            let data;
            try { data = await res.json(); } catch (e) { throw new Error('Ошибка регистрации'); }
            if (!res.ok) throw new Error(data && data.error ? data.error : 'Ошибка регистрации');
            token = data.token;
            userEmail = email;
            userRole = data.role;
            localStorage.setItem('token', token);
            localStorage.setItem('userEmail', userEmail);
            localStorage.setItem('userRole', userRole);
            // Закрываем модалку
            const registerModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('registerModal'));
            registerModal.hide();
            showAccountButtons();
            showUserPanel();
        } catch (err) {
            alert('Ошибка регистрации: ' + err.message);
        }
    };
}

function showAuthButtons() {
    document.getElementById('loginBtn').style.display = '';
    document.getElementById('registerBtn').style.display = '';
    document.getElementById('accountBtn').style.display = 'none';
    document.getElementById('logoutBtn').style.display = 'none';
    // Скрываем панель фильтров для неавторизованных пользователей
    const filterPanel = document.getElementById('filter-panel');
    if (filterPanel) filterPanel.style.display = 'none';
}

function showAccountButtons() {
    document.getElementById('loginBtn').style.display = 'none';
    document.getElementById('registerBtn').style.display = 'none';
    document.getElementById('accountBtn').style.display = '';
    document.getElementById('logoutBtn').style.display = '';
    // Показываем панель фильтров для авторизованных пользователей
    const filterPanel = document.getElementById('filter-panel');
    if (filterPanel) filterPanel.style.display = 'block';
}

function showMain() {
    showAccountButtons();
    window.location.reload();
}

function showAdminPanel() {
    const main = document.getElementById('main-section');
    main.innerHTML = `
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Админ-панель</h2>
            <div>
                <span class="badge bg-primary me-2">Админ: ${userEmail}</span>
                <button class="btn btn-outline-secondary btn-sm me-2" onclick="showMainInterface()">На главную</button>
                <button class="btn btn-outline-secondary btn-sm" onclick="logout()">Выйти</button>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h5>Управление турами</h5>
                    </div>
                    <div class="card-body">
                        <button class="btn btn-primary mb-2" onclick="showAddTourForm()">Добавить тур</button>
                        <button class="btn btn-info mb-2" onclick="loadToursForAdmin()">Просмотреть все туры</button>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h5>Управление пользователями</h5>
                    </div>
                    <div class="card-body">
                        <button class="btn btn-warning mb-2" onclick="showUserManagement()">Управление пользователями</button>
                        <button class="btn btn-success mb-2" onclick="showBookingManagement()">Управление бронированиями</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header">
                        <h5>Управление поддержкой</h5>
                    </div>
                    <div class="card-body">
                        <button class="btn btn-info mb-2" onclick="showSupportTicketManagement()">Управление обращениями</button>
                    </div>
                </div>
            </div>
        </div>
        <div id="admin-content"></div>
    `;
    
    // Скрываем панель фильтров для админа
    const filterPanel = document.getElementById('filter-panel');
    if (filterPanel) filterPanel.style.display = 'none';
    
    // Скрываем лучшие предложения и список туров для админа
    const bestOffers = document.getElementById('best-offers');
    if (bestOffers) bestOffers.style.display = 'none';
    
    const toursList = document.getElementById('tours-list');
    if (toursList) toursList.style.display = 'none';
}

function showUserPanel() {
    // Просто загружаем туры, не перезаписывая основной контент
    fetchTours();
}

function logout() {
    localStorage.clear();
    token = null;
    userEmail = null;
    userRole = null;
    showAuthButtons();
    window.location.reload();
}

function showAddTourForm() {
    const content = document.getElementById('admin-content');
    content.innerHTML = `
        <div class="card mt-3">
            <div class="card-header">
                <h5>Добавить новый тур</h5>
            </div>
            <div class="card-body">
                <form id="addTourForm" enctype="multipart/form-data">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label">Название тура</label>
                                <input type="text" class="form-control" name="name" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Описание</label>
                                <textarea class="form-control" name="description" rows="3" required></textarea>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Страна</label>
                                <input type="text" class="form-control" name="country" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Категория</label>
                                <select class="form-select" name="category" required>
                                    <option value="">Выберите категорию</option>
                                    <option value="Экскурсионные">Экскурсионные</option>
                                    <option value="Пляжные">Пляжные</option>
                                    <option value="Горные">Горные</option>
                                    <option value="Экологические">Экологические</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Изображения</label>
                                <input type="file" class="form-control" name="images" id="tourImages" multiple accept="image/*">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label">Город</label>
                                <input type="text" class="form-control" name="city" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Дата начала</label>
                                <input type="date" class="form-control" name="startDate" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Дата окончания</label>
                                <input type="date" class="form-control" name="endDate" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Цена (₽)</label>
                                <input type="number" class="form-control" name="price" step="0.01" required>
                            </div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary">Добавить тур</button>
                </form>
                <div id="addTourMsg" class="mt-3"></div>
            </div>
        </div>
    `;
    document.getElementById('addTourForm').onsubmit = async (e) => {
        e.preventDefault();
        const form = e.target;
        const formData = new FormData();
        const tour = {
            name: form.name.value,
            description: form.description.value,
            country: form.country.value,
            city: form.city.value,
            startDate: form.startDate.value,
            endDate: form.endDate.value,
            price: form.price.value,
            category: form.category.value
        };
        if (!/^\d{4}-\d{2}-\d{2}$/.test(form.startDate.value) || !/^\d{4}-\d{2}-\d{2}$/.test(form.endDate.value)) {
            document.getElementById('addTourMsg').innerHTML = '<div class="alert alert-danger">Дата должна быть в формате ГГГГ-ММ-ДД</div>';
            return;
        }
        formData.append('tour', new Blob([JSON.stringify(tour)], { type: 'application/json' }));
        const imagesInput = document.getElementById('tourImages');
        if (imagesInput && imagesInput.files.length > 0) {
            for (let i = 0; i < imagesInput.files.length; i++) {
                formData.append('images', imagesInput.files[i]);
            }
        }
        try {
            const res = await fetch('http://localhost:8080/api/admin/tours/upload', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                body: formData
            });
            if (!res.ok) throw new Error('Ошибка добавления тура');
            document.getElementById('addTourMsg').innerHTML = '<div class="alert alert-success">Тур успешно добавлен!</div>';
            form.reset();
        } catch (err) {
            document.getElementById('addTourMsg').innerHTML = '<div class="alert alert-danger">' + err.message + '</div>';
        }
    };
}

function loadToursForAdmin() {
    const content = document.getElementById('admin-content');
    content.innerHTML = '<h5>Загрузка туров...</h5>';
    
    fetch('http://localhost:8080/api/tours', {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
    .then(res => res.json())
    .then(tours => {
        let html = `
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Все туры (${tours.length})</h5>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Название</th>
                                    <th>Страна/Город</th>
                                    <th>Даты</th>
                                    <th>Цена</th>
                                    <th>Действия</th>
                                </tr>
                            </thead>
                            <tbody>
        `;
        
        for (const tour of tours) {
            html += `
                <tr>
                    <td>${tour.id}</td>
                    <td><strong>${tour.name}</strong><br><small>${tour.description}</small></td>
                    <td>${tour.country}, ${tour.city}</td>
                    <td>${tour.startDate} — ${tour.endDate}</td>
                    <td>${tour.price} ₽</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="editTour(${tour.id})">Редактировать</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteTour(${tour.id})">Удалить</button>
                    </td>
                </tr>
            `;
        }
        
        html += `
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
        
        content.innerHTML = html;
    })
    .catch(err => {
        content.innerHTML = '<div class="alert alert-danger">Ошибка загрузки туров: ' + err.message + '</div>';
    });
}

function showUserManagement() {
    const content = document.getElementById('admin-content');
    content.innerHTML = '<h5>Загрузка пользователей...</h5>';
    
    fetch('http://localhost:8080/api/admin/users', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(res => res.json())
    .then(users => {
        let html = `
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Все пользователи (${users.length})</h5>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Имя</th>
                                    <th>Email</th>
                                    <th>Роль</th>
                                    <th>Действия</th>
                                </tr>
                            </thead>
                            <tbody>
        `;
        
        for (const user of users) {
            const roleBadge = user.role === 'ROLE_ADMIN' 
                ? '<span class="badge bg-primary">Админ</span>'
                : '<span class="badge bg-success">Пользователь</span>';
                
            html += `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.name}</td>
                    <td>${user.email}</td>
                    <td>${roleBadge}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="changeUserRole(${user.id}, '${user.role}')">Изменить роль</button>
                        ${user.role !== 'ROLE_ADMIN' ? `<button class="btn btn-sm btn-danger me-1" onclick="deleteUser(${user.id})">Удалить</button>` : ''}
                        ${user.role !== 'ROLE_ADMIN' ? `<button class="btn btn-sm btn-dark" onclick="forceDeleteUser(${user.id})">Удалить принудительно</button>` : ''}
                    </td>
                </tr>
            `;
        }
        
        html += `
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
        
        content.innerHTML = html;
    })
    .catch(err => {
        content.innerHTML = '<div class="alert alert-danger">Ошибка загрузки пользователей: ' + err.message + '</div>';
    });
}

function changeUserRole(userId, currentRole) {
    const newRole = currentRole === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
    const roleName = newRole === 'ROLE_ADMIN' ? 'Админ' : 'Пользователь';
    
    if (confirm(`Изменить роль пользователя на "${roleName}"?`)) {
        fetch(`http://localhost:8080/api/admin/users/${userId}/role`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ role: newRole })
        })
        .then(res => res.json())
        .then(data => {
            alert('Роль пользователя изменена успешно!');
            showUserManagement(); // Обновляем список
        })
        .catch(err => {
            alert('Ошибка: ' + err.message);
        });
    }
}

function deleteUser(userId) {
    if (confirm('Вы уверены, что хотите удалить этого пользователя?')) {
        fetch(`http://localhost:8080/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
        .then(res => {
            if (res.ok) {
                return res.json();
            }
            return res.json().then(errData => {
                throw new Error(errData.error || 'Не удалось удалить пользователя');
            });
        })
        .then(data => {
            alert(data.message || 'Пользователь удален успешно!');
            showUserManagement(); // Обновляем список
        })
        .catch(err => {
            alert('Ошибка: ' + err.message);
        });
    }
}

function forceDeleteUser(userId) {
    const confirmation = prompt('Это действие необратимо и удалит пользователя, а все его данные (брони, отзывы) будут анонимизированы. Для подтверждения введите "УДАЛИТЬ".');
    if (confirmation === 'УДАЛИТЬ') {
        fetch(`http://localhost:8080/api/admin/users/${userId}/force`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        })
        .then(res => {
            if (res.ok) {
                return res.json();
            }
            return res.json().then(errData => {
                throw new Error(errData.error || 'Не удалось удалить пользователя');
            });
        })
        .then(data => {
            alert(data.message || 'Пользователь удален!');
            showUserManagement();
        })
        .catch(err => {
            alert('Ошибка: ' + err.message);
        });
    }
}

function editTour(tourId) {
    // Сначала получаем данные тура
    fetch(`http://localhost:8080/api/tours/${tourId}`, {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
    .then(res => res.json())
    .then(tour => {
        const content = document.getElementById('admin-content');
        content.innerHTML = `
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Редактировать тур: ${tour.name}</h5>
                </div>
                <div class="card-body">
                    <form id="editTourForm" enctype="multipart/form-data">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">Название тура</label>
                                    <input type="text" class="form-control" name="name" value="${tour.name}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Описание</label>
                                    <textarea class="form-control" name="description" rows="3" required>${tour.description}</textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Страна</label>
                                    <input type="text" class="form-control" name="country" value="${tour.country}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Категория</label>
                                    <select class="form-select" name="category" required>
                                        <option value="Экскурсионные" ${tour.category === 'Экскурсионные' ? 'selected' : ''}>Экскурсионные</option>
                                        <option value="Пляжные" ${tour.category === 'Пляжные' ? 'selected' : ''}>Пляжные</option>
                                        <option value="Горные" ${tour.category === 'Горные' ? 'selected' : ''}>Горные</option>
                                        <option value="Экологические" ${tour.category === 'Экологические' ? 'selected' : ''}>Экологические</option>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Текущие изображения</label>
                                    <div id="currentImages" class="mb-2"></div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Добавить новые изображения</label>
                                    <input type="file" class="form-control" name="images" id="editTourImages" multiple accept="image/*">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">Город</label>
                                    <input type="text" class="form-control" name="city" value="${tour.city}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Дата начала</label>
                                    <input type="date" class="form-control" name="startDate" value="${tour.startDate}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Дата окончания</label>
                                    <input type="date" class="form-control" name="endDate" value="${tour.endDate}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Цена (₽)</label>
                                    <input type="number" class="form-control" name="price" step="0.01" value="${tour.price}" required>
                                </div>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary">Сохранить изменения</button>
                        <button type="button" class="btn btn-secondary" onclick="loadToursForAdmin()">Отмена</button>
                    </form>
                    <div id="editTourMsg" class="mt-3"></div>
                </div>
            </div>
        `;
        // Показываем текущие изображения с кнопкой удалить
        const currentImagesDiv = document.getElementById('currentImages');
        let imagesArr = Array.isArray(tour.images) ? [...tour.images] : [];

        function renderCurrentImages() {
            currentImagesDiv.innerHTML = ''; // Очищаем контейнер

            if (imagesArr.length === 0) {
                currentImagesDiv.innerHTML = '<span class="text-muted">Нет изображений</span>';
                return;
            }

            imagesArr.forEach((img, idx) => {
                const imgDiv = document.createElement('div');
                imgDiv.className = 'd-flex align-items-center mb-1';

                const imgTag = document.createElement('img');
                imgTag.src = img;
                imgTag.alt = 'img';
                imgTag.style.height = '40px';
                imgTag.style.width = 'auto';
                imgTag.style.marginRight = '8px';

                const spanTag = document.createElement('span');
                spanTag.className = 'me-2';
                spanTag.textContent = img.split('/').pop();

                const deleteBtn = document.createElement('button');
                deleteBtn.type = 'button';
                deleteBtn.className = 'btn btn-sm btn-danger';
                deleteBtn.textContent = 'Удалить';
                deleteBtn.onclick = () => {
                    imagesArr.splice(idx, 1); // Удаляем элемент из массива
                    renderCurrentImages();    // Перерисовываем список
                };

                imgDiv.appendChild(imgTag);
                imgDiv.appendChild(spanTag);
                imgDiv.appendChild(deleteBtn);
                currentImagesDiv.appendChild(imgDiv);
            });
        }
        renderCurrentImages();
        // Обработка отправки формы
        document.getElementById('editTourForm').onsubmit = async (e) => {
            e.preventDefault();
            const form = e.target;
            const formData = new FormData();
            const tourData = {
                name: form.name.value,
                description: form.description.value,
                country: form.country.value,
                city: form.city.value,
                startDate: form.startDate.value,
                endDate: form.endDate.value,
                price: form.price.value,
                category: form.category.value
            };
            formData.append('tour', new Blob([JSON.stringify(tourData)], { type: 'application/json' }));
            formData.append('existingImages', JSON.stringify(imagesArr));
            const imagesInput = document.getElementById('editTourImages');
            if (imagesInput && imagesInput.files.length > 0) {
                for (let i = 0; i < imagesInput.files.length; i++) {
                    formData.append('images', imagesInput.files[i]);
                }
            }
            try {
                const res = await fetch(`http://localhost:8080/api/admin/tours/${tourId}`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    },
                    body: formData
                });
                if (!res.ok) throw new Error('Ошибка обновления тура');
                document.getElementById('editTourMsg').innerHTML = '<div class="alert alert-success">Тур успешно обновлен!</div>';
                setTimeout(loadToursForAdmin, 1000);
            } catch (err) {
                document.getElementById('editTourMsg').innerHTML = '<div class="alert alert-danger">' + err.message + '</div>';
            }
        };
    })
    .catch(err => {
        alert('Ошибка загрузки данных тура: ' + err.message);
    });
}

function deleteTour(tourId) {
    if (confirm('Вы уверены, что хотите удалить этот тур?')) {
        fetch(`http://localhost:8080/api/admin/tours/${tourId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
        .then(res => res.json())
        .then(data => {
            alert('Тур успешно удален!');
            loadToursForAdmin(); // Обновляем список
        })
        .catch(err => {
            alert('Ошибка: ' + err.message);
        });
    }
}

function showBookingManagement() {
    const content = document.getElementById('admin-content');
    content.innerHTML = '<h5>Загрузка бронирований...</h5>';
    fetch('http://localhost:8080/api/admin/bookings', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(res => res.json())
    .then(bookings => {
        let html = `
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Все бронирования (${bookings.length})</h5>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Пользователь</th>
                                    <th>Email</th>
                                    <th>Тур</th>
                                    <th>Даты</th>
                                    <th>Статус</th>
                                    <th>Действия</th>
                                </tr>
                            </thead>
                            <tbody>`;
        for (const b of bookings) {
            html += `<tr>
                <td>${b.id}</td>
                <td>${b.user ? b.user.name : '—'}</td>
                <td>${b.user ? b.user.email : '—'}</td>
                <td>${b.tour ? b.tour.name : '—'}</td>
                <td>${b.tour ? b.tour.startDate : ''} — ${b.tour ? b.tour.endDate : ''}</td>
                <td>${b.status || '—'}</td>
                <td>
                    ${b.status !== 'CONFIRMED' ? `<button class="btn btn-sm btn-success me-1" onclick="adminUpdateBookingStatus(${b.id}, 'CONFIRMED')">Подтвердить</button>` : ''}
                    ${b.status !== 'PAID' ? `<button class="btn btn-sm btn-primary me-1" onclick="adminUpdateBookingStatus(${b.id}, 'PAID')">Оплатить</button>` : ''}
                    ${b.status !== 'COMPLETED' ? `<button class="btn btn-sm btn-info me-1" onclick="adminUpdateBookingStatus(${b.id}, 'COMPLETED')">Завершить</button>` : ''}
                    ${b.status !== 'CANCELLED' ? `<button class="btn btn-sm btn-danger" onclick="adminCancelBooking(${b.id})">Отменить</button>` : ''}
                </td>
            </tr>`;
        }
        html += `</tbody></table></div></div></div>`;
        content.innerHTML = html;
    })
    .catch(err => {
        content.innerHTML = '<div class="alert alert-danger">Ошибка загрузки бронирований: ' + err.message + '</div>';
    });
}

function adminUpdateBookingStatus(bookingId, status) {
    fetch(`http://localhost:8080/api/admin/bookings/${bookingId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify({ status })
    })
    .then(res => {
        if (!res.ok) throw new Error('Ошибка смены статуса');
        return res.json();
    })
    .then(() => {
        showBookingManagement();
    })
    .catch(err => alert(err.message));
}

function adminCancelBooking(bookingId) {
    if (!confirm('Вы уверены, что хотите отменить это бронирование?')) return;
    fetch(`http://localhost:8080/api/admin/bookings/${bookingId}/cancel`, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(res => {
        if (!res.ok) throw new Error('Ошибка отмены бронирования');
        return res.json();
    })
    .then(() => {
        showBookingManagement();
    })
    .catch(err => alert(err.message));
}

function showSupportTicketManagement() {
    const content = document.getElementById('admin-content');
    content.innerHTML = '<h5>Загрузка обращений...</h5>';
    fetch('http://localhost:8080/api/admin/support-tickets', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(res => res.json())
    .then(tickets => {
        let html = `
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Все обращения (${tickets.length})</h5>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Пользователь</th>
                                    <th>Тема</th>
                                    <th>Статус</th>
                                    <th>Дата</th>
                                    <th>Действия</th>
                                </tr>
                            </thead>
                            <tbody>`;
        for (const ticket of tickets) {
            const date = new Date(ticket.createdAt).toLocaleString('ru-RU');
            html += `<tr>
                <td>${ticket.id}</td>
                <td>${ticket.user ? ticket.user.name : '—'} (${ticket.user ? ticket.user.email : '—'})</td>
                <td>${ticket.subject}</td>
                <td><span class="badge bg-warning">${ticket.status || '—'}</span></td>
                <td>${date}</td>
                <td>
                    <button class="btn btn-sm btn-info me-1" onclick="showAdminSupportTicketModal(${ticket.id})">Просмотр</button>
                    <button class="btn btn-sm btn-success" ${ticket.status === 'RESOLVED' ? 'disabled' : ''} onclick="adminUpdateTicketStatus(${ticket.id}, 'RESOLVED')">Решено</button>
                </td>
            </tr>`;
        }
        html += `</tbody></table></div></div></div>`;
        content.innerHTML = html;
    })
    .catch(err => {
        content.innerHTML = '<div class="alert alert-danger">Ошибка загрузки обращений: ' + err.message + '</div>';
    });
}

async function showAdminSupportTicketModal(ticketId) {
    const modalElement = document.getElementById('adminSupportTicketModal');
    const modal = new bootstrap.Modal(modalElement);

    document.getElementById('adminSupportTicketModalLabel').textContent = `Обращение #${ticketId}`;
    document.getElementById('adminReplyTicketId').value = ticketId;
    const detailsContainer = document.getElementById('adminSupportTicketDetails');
    const messagesContainer = document.getElementById('adminSupportMessages');

    detailsContainer.innerHTML = 'Загрузка...';
    messagesContainer.innerHTML = 'Загрузка...';

    try {
        // Загружаем детали тикета
        const ticketRes = await fetch(`${API_BASE_URL}/api/support/${ticketId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!ticketRes.ok) throw new Error('Ошибка загрузки деталей тикета');
        const ticket = await ticketRes.json();

        detailsContainer.innerHTML = `
            <p><strong>Пользователь:</strong> ${ticket.user.name} (${ticket.user.email})</p>
            <p><strong>Тема:</strong> ${ticket.subject}</p>
            <p><strong>Описание:</strong> ${ticket.description}</p>
            <p><strong>Статус:</strong> <span class="badge bg-warning">${ticket.status}</span></p>
            <p><strong>Категория:</strong> ${ticket.ticketType}</p>
        `;

        // Загружаем сообщения
        const messagesRes = await fetch(`${API_BASE_URL}/api/support/${ticketId}/messages`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!messagesRes.ok) throw new Error('Ошибка загрузки сообщений');
        const messages = await messagesRes.json();

        if (messages.length === 0) {
            messagesContainer.innerHTML = '<p class="text-muted">Сообщений нет.</p>';
        } else {
            messagesContainer.innerHTML = messages.map(msg => {
                const date = new Date(msg.sentAt).toLocaleString('ru-RU');
                const sender = msg.sender ? msg.sender.name : 'Система';
                const roleClass = msg.messageType === 'ADMIN' ? 'text-success' : 'text-primary';
                return `<div class="border-bottom pb-2 mb-2">
                          <p class="mb-1">${msg.message}</p>
                          <small class="text-muted">От: <strong class="${roleClass}">${sender}</strong> | ${date}</small>
                        </div>`;
            }).join('');
        }

    } catch (err) {
        detailsContainer.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
    
    modal.show();
}

function adminUpdateTicketStatus(ticketId, status) {
    fetch(`http://localhost:8080/api/admin/support-tickets/${ticketId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify({ status })
    })
    .then(res => {
        if (!res.ok) throw new Error('Ошибка смены статуса обращения');
        return res.json();
    })
    .then(() => {
        showSupportTicketManagement();
    })
    .catch(err => alert(err.message));
}

function adminCancelTicket(ticketId) {
    if (!confirm('Вы уверены, что хотите отменить это обращение?')) return;
    fetch(`http://localhost:8080/api/admin/support-tickets/${ticketId}/cancel`, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(res => {
        if (!res.ok) throw new Error('Ошибка отмены обращения');
        return res.json();
    })
    .then(() => {
        showSupportTicketManagement();
    })
    .catch(err => alert(err.message));
}

async function loadTours() {
    const main = document.getElementById('main-section');
    main.innerHTML = '<h2>Список туров</h2><div id="tours"></div>';
    const res = await fetch('http://localhost:8080/api/tours', {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    const tours = await res.json();
    let html = '<ul class="list-group">';
    for (const t of tours) {
        html += `<li class="list-group-item">
            <b>${t.name}</b> (${t.country}, ${t.city})<br>
            ${t.description}<br>
            <b>Даты:</b> ${t.startDate} — ${t.endDate}<br>
            <b>Цена:</b> ${t.price} €
        </li>`;
    }
    html += '</ul>';
    document.getElementById('tours').innerHTML = html;
}

// Загрузка туров с бэкенда
async function fetchTours() {
    try {
        console.log('Fetching tours from:', `${API_BASE_URL}/api/tours`);
        const res = await fetch(`${API_BASE_URL}/api/tours`, {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        if (!res.ok) throw new Error(`Ошибка загрузки туров: ${res.status} ${res.statusText}`);
        tours = await res.json();
        
        tours.forEach(tour => {
            if (!tour.rating) {
                tour.rating = Math.round(3 + Math.random()*2); // 3-5
            }
            if (!tour.bookingsCount) {
                tour.bookingsCount = Math.floor(Math.random()*20);
            }
        });
        filteredTours = [...tours];
        renderBestOffers();
        renderTours(tours);
    } catch (err) {
        console.error('Ошибка загрузки туров:', err);
        document.getElementById('tours-list').innerHTML = 
            '<div class="col-12"><div class="alert alert-danger">Ошибка загрузки туров: ' + err.message + '</div></div>';
    }
}

// Функция для проверки существования изображения
function checkImage(url) {
    return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => resolve(true);
        img.onerror = () => resolve(false);
        img.src = url;
    });
}

// Генерация HTML карточки тура
async function tourCardHtml(tour, compact = false) {
    let gallery = '';

    // Определяем, какие изображения использовать
    let imagesToShow = [];
    if (tour.images && tour.images.length > 0) {
        // Используем изображения с бэкенда. Пути уже правильные.
        imagesToShow = tour.images;
    } else {
        // Используем старую логику для туров без картинок
        const cityKey = tour.city.toLowerCase();
        const imageCity = cityImageMap[cityKey] || cityKey;
        imagesToShow = [
            `assets/images/${imageCity}1.jpg`,
            `assets/images/${imageCity}2.jpg`
        ];
    }
    
    gallery = `
        <div id="gallery-${tour.id}" class="carousel slide" data-bs-ride="carousel">
            <div class="carousel-inner">
                ${imagesToShow.map((img, idx) => `
                    <div class="carousel-item${idx === 0 ? ' active' : ''}">
                        <img src="${img}" class="d-block w-100 tour-card-img" alt="${tour.name}">
                    </div>
                `).join('')}
            </div>
            ${imagesToShow.length > 1 ? `
                <button class="carousel-control-prev" type="button" data-bs-target="#gallery-${tour.id}" data-bs-slide="prev">
                    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Previous</span>
                </button>
                <button class="carousel-control-next" type="button" data-bs-target="#gallery-${tour.id}" data-bs-slide="next">
                    <span class="carousel-control-next-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Next</span>
                </button>
            ` : ''}
        </div>`;

    const cats = tour.category ? `<span class="badge bg-info">${tour.category}</span>` : '';

    const stars = '★'.repeat(Math.round(tour.rating || 0)) + 
                  '☆'.repeat(5 - Math.round(tour.rating || 0));

    let bookButton;
    if (token) {
        bookButton = `<button class="btn btn-primary w-100" onclick="bookTour(${tour.id})">Забронировать</button>`;
    } else {
        bookButton = `<button class="btn btn-primary w-100" onclick="openLoginModal()">Забронировать</button>`;
    }
    
    return `
    <div class="card tour-card mb-3${compact ? '' : ' shadow'}">
        ${gallery}
        <div class="card-body">
            <h5 class="card-title">${tour.name}</h5>
            <div class="mb-2">${cats}</div>
            <div class="mb-2 text-muted">${tour.country}, ${tour.city}</div>
            <div class="mb-2"><b>Даты:</b> ${tour.startDate} — ${tour.endDate}</div>
            <div class="mb-2"><b>Цена:</b> ${tour.price} ₽</div>
            <div class="mb-2">${stars} <span class="text-secondary">(${tour.rating || 0}/5)</span></div>
            ${bookButton}
        </div>
    </div>`;
}

// Рендер списка туров
function renderTours(toursToRender = tours) {
    console.log('Rendering tours:', toursToRender);
    const container = document.getElementById('tours-list');
    if (!container) return;

    container.innerHTML = '';
    if (!toursToRender.length) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-info">Туры не найдены</div></div>';
        return;
    }

    toursToRender.forEach(async tour => {
        const div = document.createElement('div');
        div.className = 'col-md-6 col-lg-4';
        div.innerHTML = await tourCardHtml(tour);
        container.appendChild(div);
    });
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ru-RU');
}

async function bookTour(tourId) {
    if (!token) {
        alert('Пожалуйста, войдите в систему для бронирования тура');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/api/bookings?tourId=${tourId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!res.ok) {
            const errorData = await res.text();
            throw new Error(`Ошибка при бронировании тура: ${res.status} - ${errorData}`);
        }

        const booking = await res.json();
        alert('Тур успешно забронирован!');
    } catch (err) {
        console.error('Ошибка бронирования:', err);
        alert('Ошибка при бронировании тура: ' + err.message);
    }
}

// Рендер лучших предложений
async function renderBestOffers() {
    if (!tours.length) return;
    const bestRated = [...tours].sort((a,b) => (b.rating || 0) - (a.rating || 0))[0];
    const mostBooked = [...tours].sort((a,b) => (b.bookingsCount || 0) - (a.bookingsCount || 0))[0];
    
    if (!bestRated || !mostBooked) return;

    const bestRatedHtml = await tourCardHtml(bestRated, false);
    const mostBookedHtml = await tourCardHtml(mostBooked, false);
    
    let html = '<div class="row">';
    html += `<div class="col-md-6"><h5 class="mb-2">🏅 Лучший по рейтингу</h5><div class="best-offer">${bestRatedHtml}</div></div>`;
    html += `<div class="col-md-6"><h5 class="mb-2">🔥 Самый популярный</h5><div class="best-offer">${mostBookedHtml}</div></div>`;
    html += '</div>';
    
    document.getElementById('best-offers').innerHTML = html;
    
    // Инициализация карусели для лучших предложений
    [bestRated, mostBooked].forEach(tour => {
        if (tour && tour.images && tour.images.length > 1) {
            const carousel = document.getElementById(`gallery-${tour.id}`);
            if (carousel) new bootstrap.Carousel(carousel);
        }
    });
}

// Фильтрация туров
function filterTours() {
    const name = document.getElementById('filterNameMain')?.value.toLowerCase() || '';
    const category = document.getElementById('filterCategory')?.value || '';
    const dateStart = document.getElementById('filterDateStart')?.value || '';
    const dateEnd = document.getElementById('filterDateEnd')?.value || '';

    filteredTours = tours.filter(tour => {
        // Фильтр по названию (регистронезависимый)
        if (name && !(tour.name || '').toLowerCase().includes(name)) {
            return false;
        }

        // Фильтр по категории
        if (category && tour.category !== category) {
            return false;
        }

        // Фильтр по диапазону дат
        if (dateStart && (!tour.endDate || tour.endDate < dateStart)) {
            return false; // Тур заканчивается до начала диапазона или у него нет даты
        }
        if (dateEnd && (!tour.startDate || tour.startDate > dateEnd)) {
            return false; // Тур начинается после конца диапазона или у него нет даты
        }

        return true;
    });
    
    renderTours(filteredTours);
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('filterNameMain')?.addEventListener('input', filterTours);
    document.getElementById('filterCategory')?.addEventListener('change', filterTours);
    document.getElementById('filterDateStart')?.addEventListener('change', filterTours);
    document.getElementById('filterDateEnd')?.addEventListener('change', filterTours);
    document.getElementById('resetFiltersBtn')?.addEventListener('click', () => {
        const nameInput = document.getElementById('filterNameMain');
        if (nameInput) nameInput.value = '';
        const categoryInput = document.getElementById('filterCategory');
        if (categoryInput) categoryInput.value = '';
        
        // Сброс flatpickr
        const fpStart = document.getElementById('filterDateStart')?._flatpickr;
        if (fpStart) fpStart.clear();
        const fpEnd = document.getElementById('filterDateEnd')?._flatpickr;
        if (fpEnd) fpEnd.clear();

        filteredTours = tours;
        renderTours(filteredTours);
    });
});

// --- Функция для открытия модального окна входа ---
function openLoginModal() {
    var loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
    loginModal.show();
}

function showMainInterface() {
    const main = document.getElementById('main-section');
    main.innerHTML = '';
    
    // Показываем панель фильтров
    const filterPanel = document.getElementById('filter-panel');
    if (filterPanel) filterPanel.style.display = 'block';
    
    // Показываем лучшие предложения и список туров
    const bestOffers = document.getElementById('best-offers');
    if (bestOffers) bestOffers.style.display = 'block';
    
    const toursList = document.getElementById('tours-list');
    if (toursList) toursList.style.display = 'block';
    
    // Загружаем туры
    fetchTours();
}

function renderOrdersSection() {
    const content = document.getElementById('accountContent');
    content.innerHTML = `<h5>📋 Мои заказы / Мои туры</h5><div id="ordersLoading">Загрузка...</div><div id="ordersTable"></div>`;
    fetch('http://localhost:8080/api/bookings/my-dto', {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
    .then(res => res.json())
    .then(bookings => {
        if (!Array.isArray(bookings) || bookings.length === 0) {
            document.getElementById('ordersTable').innerHTML = '<div class="alert alert-info">У вас пока нет заказов.</div>';
            document.getElementById('ordersLoading').style.display = 'none';
            return;
        }
        let html = `<div class="table-responsive"><table class="table table-striped"><thead><tr><th>Тур</th><th>Даты</th><th>Статус</th><th>Действия</th></tr></thead><tbody>`;
        for (const booking of bookings) {
            console.log('Booking:', booking);
            const canCancel = ['NEW','CONFIRMED','Подтвержден'].includes((booking.status||'').toUpperCase());
            html += `<tr>
                <td>${booking.tour ? booking.tour.name : '—'}<br><small>${booking.tour ? booking.tour.city + ', ' + booking.tour.country : ''}</small></td>
                <td>${booking.tour ? booking.tour.startDate : ''} — ${booking.tour ? booking.tour.endDate : ''}</td>
                <td>${booking.status || '—'}</td>
                <td>
                    <button class="btn btn-sm btn-success me-1" disabled>Оплатить</button>
                    <button class="btn btn-sm btn-outline-primary me-1" disabled>Скачать ваучер</button>
                    <button class="btn btn-sm btn-outline-secondary me-1" disabled>Скачать договор</button>
                    <button class="btn btn-sm btn-danger" ${canCancel ? '' : 'disabled'} onclick="cancelBooking(${booking.id})">Отменить</button>
                </td>
            </tr>`;
        }
        html += '</tbody></table></div>';
        document.getElementById('ordersTable').innerHTML = html;
        document.getElementById('ordersLoading').style.display = 'none';
    })
    .catch(() => {
        document.getElementById('ordersTable').innerHTML = '<div class="alert alert-danger">Ошибка загрузки заказов</div>';
        document.getElementById('ordersLoading').style.display = 'none';
    });
} 

document.addEventListener('DOMContentLoaded', () => {
    const adminReplyForm = document.getElementById('adminReplyForm');
    if(adminReplyForm) {
        adminReplyForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const ticketId = document.getElementById('adminReplyTicketId').value;
            const message = document.getElementById('adminReplyMessage').value;

            if(!message.trim()) {
                alert('Сообщение не может быть пустым.');
                return;
            }

            try {
                const res = await fetch(`${API_BASE_URL}/api/admin/support-tickets/${ticketId}/message`, {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message })
                });

                if(!res.ok) {
                    const errData = await res.json();
                    throw new Error(errData.message || 'Ошибка отправки ответа');
                }

                document.getElementById('adminReplyMessage').value = '';
                await showAdminSupportTicketModal(ticketId); // Обновляем чат
            } catch (err) {
                alert(err.message);
            }
        });
    }
}); 