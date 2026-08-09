document.addEventListener("DOMContentLoaded", fetchAndRenderBooks);

async function fetchAndRenderBooks() {
    try {
        const response = await fetch('/api/books');
        if (!response.ok) {
            throw new Error('Không thể tải danh sách sách!');
        }

        const books = await response.json();

        const searchQuery = document.getElementById('searchInput').value.toLowerCase().trim();
        const selectedGenre = document.getElementById('genreFilter').value;
        const selectedStatus = document.getElementById('statusFilter').value;

        const filteredBooks = books.filter(book => {
            const matchesSearch = book.code.toLowerCase().includes(searchQuery) ||
                book.title.toLowerCase().includes(searchQuery);
            const matchesGenre = selectedGenre === "" || book.genre === selectedGenre;

            let matchesStatus = true;
            if (selectedStatus === "available") {
                matchesStatus = book.availableQuantity > 0;
            } else if (selectedStatus === "out_of_stock") {
                matchesStatus = book.availableQuantity === 0;
            }

            return matchesSearch && matchesGenre && matchesStatus;
        });

        renderBookTable(filteredBooks);

    } catch (error) {
        console.error("Lỗi:", error);
        alert(error.message);
    }
}

function renderBookTable(books) {
    const tableBody = document.getElementById('bookTableBody');
    tableBody.innerHTML = '';

    if (books.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;">Không tìm thấy sách nào!</td></tr>`;
        return;
    }

    books.forEach(book => {
        const row = document.createElement('tr');

        const isAvailable = book.availableQuantity > 0;
        const statusBadge = isAvailable
            ? `<span class="badge status-available">Còn sách</span>`
            : `<span class="badge status-empty">Hết sách</span>`;

        row.innerHTML = `
            <td><strong>${escapeHtml(book.code)}</strong></td>
            <td>${escapeHtml(book.title)}</td>
            <td>${escapeHtml(book.author)}</td>
            <td>${escapeHtml(book.genre)}</td>
            <td>${book.availableQuantity}</td>
            <td>${book.price ? book.price.toLocaleString('vi-VN') : 0} đ</td>
            <td>${statusBadge}</td>
        `;

        tableBody.appendChild(row);
    });
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}