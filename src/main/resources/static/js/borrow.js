let items = [];

function addItem() {
  const bookIdInput = document.getElementById("bookId");
  const quantityInput = document.getElementById("quantity");
  const messageDiv = document.getElementById("message");

  const bookId = bookIdInput.value.trim();
  const quantity = parseInt(quantityInput.value);

  if (!bookId) {
    alert("Vui lòng nhập mã sách!");
    return;
  }

  if (isNaN(quantity) || quantity <= 0) {
    alert("Số lượng phải lớn hơn 0!");
    return;
  }

  items.push({
    bookId: bookId,
    quantity: quantity,
  });

  renderItemsTable();

  bookIdInput.value = "";
  quantityInput.value = "1";
  messageDiv.innerText = "";
  messageDiv.className = "";
}

function removeItem(index) {
  items.splice(index, 1);
  renderItemsTable();
}

function renderItemsTable() {
  const tbody = document.getElementById("itemsTableBody");

  tbody.innerHTML = "";

  items.forEach((item, index) => {
    const row = `
<tr>
<td>${item.bookId}</td>
<td>${item.quantity}</td>
<td>
    <button
        type="button"
        class="btn btn-danger"
        onclick="removeItem(${index})"
    >
        <i class="bi bi-trash"></i>
        Xóa
    </button>
</td>
</tr>
`;

    tbody.innerHTML += row;
  });
}

async function createTicket() {
  const readerId = document.getElementById("readerId").value.trim();

  const borrowDate = document.getElementById("borrowDate").value;

  const dueDate = document.getElementById("dueDate").value;

  const messageDiv = document.getElementById("message");

  // Kiểm tra danh sách sách
  if (items.length === 0) {
    alert("Vui lòng thêm ít nhất 1 cuốn sách!");
    return;
  }

  // Kiểm tra mã bạn đọc
  if (!readerId) {
    alert("Vui lòng nhập mã bạn đọc!");
    return;
  }

  // Kiểm tra ngày mượn
  if (!borrowDate) {
    alert("Vui lòng chọn ngày mượn!");
    return;
  }

  // Kiểm tra hạn trả
  if (!dueDate) {
    alert("Vui lòng chọn hạn trả!");
    return;
  }

  const payload = {
    readerId: readerId,
    borrowDate: borrowDate,
    dueDate: dueDate,
    items: items,
  };

  try {
    const response = await fetch("/api/borrow-tickets", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify(payload),
    });

    const data = await response.json();

    // TẠO PHIẾU THÀNH CÔNG
    if (response.ok) {
      messageDiv.className = "success";

      messageDiv.innerText =
        "Tạo phiếu mượn thành công! Mã phiếu: " + data.ticketId;

      // Xóa danh sách sách
      items = [];
      renderItemsTable();

      // Xóa dữ liệu form
      document.getElementById("readerId").value = "";
      document.getElementById("bookId").value = "";
      document.getElementById("quantity").value = "1";
      document.getElementById("borrowDate").value = "";
      document.getElementById("dueDate").value = "";
    }

    // BACKEND TRẢ VỀ LỖI
    else {
      messageDiv.className = "error";

      messageDiv.innerText =
        "Lỗi: " + (data.message || "Không thể tạo phiếu mượn!");
    }
  } catch (error) {
    // LỖI KẾT NỐI
    messageDiv.className = "error";

    messageDiv.innerText = "Lỗi kết nối máy chủ!";
  }
}
