let items = [];

function addItem() {
    const bookIdInput = document.getElementById('bookId');
    const quantityInput = document.getElementById('quantity');
    const messageDiv = document.getElementById('message');

    const bookId = bookIdInput.value.trim();
    const quantity = parseInt(quantityInput.value);

    if (!bookId) {
        alert('Vui lòng nhập mã sách!');
        return;
    }
    if (isNaN(quantity) || quantity <= 0) {
        alert('Số lượng phải lớn hơn 0!');
        return;
    }

    items.push({ bookId: bookId, quantity: quantity });
    renderItemsTable();

    bookIdInput.value = '';
    quantityInput.value = '1';
    messageDiv.innerText = '';
}

function removeItem(index) {
    items.splice(index, 1);
    renderItemsTable();
}

function renderItemsTable() {
    const tbody = document.getElementById('itemsTableBody');
    tbody.innerHTML = '';

    items.forEach((item, index) => {
        const row = `
            <tr>
                <td>${item.bookId}</td>
                <td>${item.quantity}</td>
                <td><button type="button" style="background-color: #dc3545;" onclick="removeItem(${index})">Xóa</button></td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

async function createTicket() {
    const readerId = document.getElementById('readerId').value.trim();
    const borrowDate = document.getElementById('borrowDate').value;
    const dueDate = document.getElementById('dueDate').value;
    const messageDiv = document.getElementById('message');

    if (items.length === 0) {
        alert('Vui lòng thêm ít nhất 1 cuốn sách!');
        return;
    }

    const payload = {
        readerId: readerId,
        borrowDate: borrowDate,
        dueDate: dueDate,
        items: items
    };

    try {
        const response = await fetch('/api/borrow-tickets', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok) {
            messageDiv.style.color = 'green';
            messageDiv.innerText = 'Tạo phiếu mượn thành công! Mã phiếu: ' + data.ticketId;
            items = [];
            renderItemsTable();
        } else {
            messageDiv.style.color = 'red';
            messageDiv.innerText = 'Lỗi: ' + (data.message || 'Không thể tạo phiếu mượn!');
        }
    } catch (error) {
        messageDiv.style.color = 'red';
        messageDiv.innerText = 'Lỗi kết nối máy chủ!';
    }
}