let bookModal;
let allBooks = [];

document.addEventListener("DOMContentLoaded", () => {
  const modalElement = document.getElementById("bookModal");
  if (modalElement) {
    bookModal = new bootstrap.Modal(modalElement);
  }
  fetchAndRenderBooks();
});

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

async function fetchAndRenderBooks() {
  try {
    const response = await fetch("/api/books");
    if (!response.ok) {
      let errorMsg = "Không thể tải danh sách sách!";
      try {
        const error = await response.json();
        errorMsg = error.message || errorMsg;
      } catch (e) {}
      throw new Error(errorMsg);
    }

    allBooks = await response.json();

    updateGenreDropdowns(allBooks);

    const searchQuery = (document.getElementById("searchInput")?.value || "")
      .toLowerCase()
      .trim();
    const selectedGenre = document.getElementById("genreFilter")?.value || "";
    const selectedStatus = document.getElementById("statusFilter")?.value || "";

    const filteredBooks = allBooks.filter((book) => {
      const bookId = book.bookId || "";
      const title = book.title || "";
      const matchesSearch =
        bookId.toLowerCase().includes(searchQuery) ||
        title.toLowerCase().includes(searchQuery);

      const matchesGenre = selectedGenre === "" || book.genre === selectedGenre;

      let matchesStatus = true;
      if (selectedStatus === "available")
        matchesStatus = book.availableQuantity > 0;
      else if (selectedStatus === "out_of_stock")
        matchesStatus = book.availableQuantity === 0;

      return matchesSearch && matchesGenre && matchesStatus;
    });

    renderBookTable(filteredBooks);
  } catch (error) {
    console.error("Lỗi:", error);
    showTableError(error.message);
  }
}

function updateGenreDropdowns(books) {
  const genreFilter = document.getElementById("genreFilter");
  const genreDatalist = document.getElementById("genreOptions");
  if (!genreFilter || !genreDatalist) return;

  const currentSelectedGenre = genreFilter.value;

  const genres = [
    ...new Set(books.map((b) => b.genre).filter((g) => g && g.trim() !== "")),
  ].sort();

  genreFilter.innerHTML = '<option value="">Tất cả thể loại</option>';
  genres.forEach((genre) => {
    const option = document.createElement("option");
    option.value = genre;
    option.textContent = genre;
    if (genre === currentSelectedGenre) {
      option.selected = true;
    }
    genreFilter.appendChild(option);
  });

  genreDatalist.innerHTML = "";
  genres.forEach((genre) => {
    const option = document.createElement("option");
    option.value = genre;
    genreDatalist.appendChild(option);
  });
}

function showTableError(message) {
  const tableBody = document.getElementById("bookTableBody");
  if (!tableBody) return;
  tableBody.innerHTML = `
        <tr>
            <td colspan="8" class="text-center text-danger py-4">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <strong>Đã xảy ra lỗi:</strong> ${escapeHtml(message)}
            </td>
        </tr>`;
}

function renderBookTable(books) {
  const tableBody = document.getElementById("bookTableBody");
  if (!tableBody) return;

  tableBody.innerHTML = "";

  if (!books || books.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">Không tìm thấy sách nào!</td></tr>`;
    return;
  }

  books.forEach((book) => {
    const row = document.createElement("tr");
    const isAvailable = book.availableQuantity > 0;
    const statusBadge = isAvailable
      ? '<span class="status-badge status-returned">Còn sách</span>'
      : '<span class="status-badge status-overdue">Hết sách</span>';

    const safeBookId = escapeHtml(book.bookId);
    const safeTitle = escapeHtml(book.title);
    const safeAuthor = escapeHtml(book.author);
    const safeGenre = escapeHtml(book.genre);
    const quantity = Number.isInteger(book.availableQuantity)
      ? book.availableQuantity
      : 0;
    const priceFormatted = book.price ? book.price.toLocaleString("vi-VN") : 0;

    row.innerHTML = `
            <td><strong>${safeBookId}</strong></td>
            <td>${safeTitle}</td>
            <td>${safeAuthor}</td>
            <td>${safeGenre}</td>
            <td>${quantity}</td>
            <td>${priceFormatted} đ</td>
            <td>${statusBadge}</td>
            <td class="text-center">
                <button
                    class="btn btn-sm btn-outline-primary me-1"
                    onclick="openEditModal('${safeBookId}')"
                >
                    <i class="bi bi-pencil-square"></i>
                    Cập nhật
                </button>

                <button
                    class="btn btn-sm btn-outline-danger"
                    onclick="deleteBook('${safeBookId}')"
                >
                    <i class="bi bi-trash"></i>
                    Xóa
                </button>
            </td>
        `;
    tableBody.appendChild(row);
  });
}

function openAddModal() {
  document.getElementById("isEditMode").value = "false";
  document.getElementById("bookModalLabel").innerText = "Thêm sách mới";

  document.getElementById("bookForm").reset();
  document.getElementById("bookId").disabled = false;

  if (bookModal) bookModal.show();
}

function openEditModal(bookId) {
  const book = allBooks.find((b) => b.bookId === bookId);
  if (!book) return;

  document.getElementById("isEditMode").value = "true";
  document.getElementById("bookModalLabel").innerText =
    "Cập nhật thông tin sách";

  document.getElementById("bookId").value = book.bookId;
  document.getElementById("bookId").disabled = true;
  document.getElementById("bookTitle").value = book.title || "";
  document.getElementById("bookAuthor").value = book.author || "";
  document.getElementById("bookGenre").value = book.genre || "";
  document.getElementById("bookQuantity").value = book.availableQuantity ?? 0;
  document.getElementById("bookPrice").value = book.price ?? 0;

  if (bookModal) bookModal.show();
}

async function saveBook(event) {
  event.preventDefault();

  const isEdit = document.getElementById("isEditMode").value === "true";
  const bookId = document.getElementById("bookId").value.trim();

  const bookData = {
    bookId: bookId,
    title: document.getElementById("bookTitle").value.trim(),
    author: document.getElementById("bookAuthor").value.trim(),
    genre: document.getElementById("bookGenre").value.trim(),
    availableQuantity:
      parseInt(document.getElementById("bookQuantity").value) || 0,
    price: parseFloat(document.getElementById("bookPrice").value) || 0,
  };

  const url = isEdit
    ? `/api/books/${encodeURIComponent(bookId)}`
    : "/api/books";
  const method = isEdit ? "PUT" : "POST";

  try {
    const response = await fetch(url, {
      method: method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(bookData),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "Thao tác thất bại!");
    }

    if (bookModal) bookModal.hide();
    fetchAndRenderBooks();
  } catch (error) {
    alert(error.message);
  }
}

async function deleteBook(bookId) {
  if (!confirm(`Bạn có chắc chắn muốn xóa sách có mã '${bookId}' không?`)) {
    return;
  }

  try {
    const response = await fetch(`/api/books/${encodeURIComponent(bookId)}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "Xóa sách thất bại!");
    }

    alert("Xóa sách thành công!");
    fetchAndRenderBooks();
  } catch (error) {
    alert(error.message);
  }
}

async function importBooks(event) {
  const file = event.target.files[0];
  if (!file) return;

  if (!confirm(`Xác nhận nhập dữ liệu từ file '${file.name}'?`)) {
    event.target.value = "";
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await fetch("/api/books/import", {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "Nhập dữ liệu thất bại!");
    }

    const resultText = await response.text();
    alert(resultText);
    fetchAndRenderBooks();
  } catch (error) {
    alert(error.message);
  } finally {
    event.target.value = "";
  }
}
