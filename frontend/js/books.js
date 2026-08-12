let bookModal;
let allBooks = [];

document.addEventListener("DOMContentLoaded", () => {
    bookModal = new bootstrap.Modal(document.getElementById('bookModal'));
    fetchAndRenderBooks();
});

async function fetchAndRenderBooks() {
    try {
        const response = await fetch('/api/books');
        if (!response.ok) throw new Error('Không thể tải danh sách sách!');

        allBooks = await response.json();

        const searchQuery = document.getElementById('searchInput').value.toLowerCase().trim();
        const selectedGenre = document.getElementById('genreFilter').value;
        const selectedStatus = document.getElementById('statusFilter').value;

        const filteredBooks = allBooks.filter(book => {
            const matchesSearch = book.code.toLowerCase().includes(searchQuery) ||
                book.title.toLowerCase().includes(searchQuery);
            const matchesGenre = selectedGenre === "" || book.genre === selectedGenre;

            let matchesStatus = true;
            if (selectedStatus === "available") matchesStatus = book.availableQuantity > 0;
            else if (selectedStatus === "out_of_stock") matchesStatus = book.availableQuantity === 0;

            return matchesSearch && matchesGenre && matchesStatus;
        });

        renderBookTable(filteredBooks);
    } catch (error) {
        console.error("Lỗi:", error);
    }
}

function renderBookTable(books) {
    const tableBody = document.getElementById('bookTableBody');
    tableBody.innerHTML = '';

    if (books.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">Không tìm thấy sách nào!</td></tr>`;
        return;
    }

    books.forEach(book => {
        const row = document.createElement('tr');
        const isAvailable = book.availableQuantity > 0;
        const statusBadge = isAvailable
            ? `<span class="badge bg-success">Còn sách</span>`
            : `<span class="badge bg-danger">Hết sách</span>`;

        row.innerHTML = `
            <td><strong>${book.code}</strong></td>
            <td>${book.title}</td>
            <td>${book.author}</td>
            <td>${book.genre}</td>
            <td>${book.availableQuantity}</td>
            <td>${book.price ? book.price.toLocaleString('vi-VN') : 0} đ</td>
            <td>${statusBadge}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-warning me-1" onclick="openEditModal('${book.code}')">
                    Cập nhật
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteBook('${book.code}')">
                    Xóa
                </button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function openAddModal() {
    document.getElementById('isEditMode').value = "false";
    document.getElementById('bookModalLabel').innerText = "Thêm sách mới";

    document.getElementById('bookForm').reset();
    document.getElementById('bookCode').disabled = false;

    bookModal.show();
}

function openEditModal(code) {
    const book = allBooks.find(b => b.code === code);
    if (!book) return;

    document.getElementById('isEditMode').value = "true";
    document.getElementById('bookModalLabel').innerText = "Cập nhật thông tin sách";

    document.getElementById('bookCode').value = book.code;
    document.getElementById('bookCode').disabled = true;
    document.getElementById('bookTitle').value = book.title;
    document.getElementById('bookAuthor').value = book.author;
    document.getElementById('bookGenre').value = book.genre;
    document.getElementById('bookQuantity').value = book.availableQuantity;
    document.getElementById('bookPrice').value = book.price;

    bookModal.show();
}

async function saveBook(event) {
    event.preventDefault();

    const isEdit = document.getElementById('isEditMode').value === "true";
    const bookData = {
        code: document.getElementById('bookCode').value,
        title: document.getElementById('bookTitle').value,
        author: document.getElementById('bookAuthor').value,
        genre: document.getElementById('bookGenre').value,
        availableQuantity: parseInt(document.getElementById('bookQuantity').value),
        price: parseFloat(document.getElementById('bookPrice').value)
    };

    const url = '/api/books';
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookData)
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Thao tác thất bại!');
        }

        bookModal.hide();
        fetchAndRenderBooks();
    } catch (error) {
        alert(error.message);
    }
}

async function deleteBook(code) {
    if (!confirm(`Bạn có chắc chắn muốn xóa sách có mã '${code}' không?`)) {
        return;
    }

    try {
        const response = await fetch(`/api/books?code=${encodeURIComponent(code)}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Xóa sách thất bại!');
        }

        alert('Xóa sách thành công!');
        fetchAndRenderBooks();
    } catch (error) {
        alert(error.message);
    }
}

async function importBooks(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (!confirm(`Xác nhận nhập dữ liệu từ file '${file.name}'?`)) {
        event.target.value = '';
        return;
    }

    try {
        const response = await fetch('/api/books/import', {
            method: 'POST',
            headers: { 'Content-Type': 'text/csv; charset=utf-8' },
            body: file
        });

        const resultText = await response.text();

        if (!response.ok) {
            throw new Error(resultText || 'Nhập dữ liệu thất bại!');
        }

        alert(resultText);
        fetchAndRenderBooks();
    } catch (error) {
        alert(error.message);
    } finally {
        event.target.value = '';
    }
}