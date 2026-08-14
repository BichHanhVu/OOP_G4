document.addEventListener('DOMContentLoaded', loadTickets);

async function loadTickets() {
    const tbody = document.getElementById('ticketsTableBody');

    try {
        const response = await fetch('/api/borrow-tickets');
        if (!response.ok) {
            tbody.innerHTML = '<tr><td colspan="6" style="color:red;">Không thể tải dữ liệu phiếu mượn!</td></tr>';
            return;
        }

        const tickets = await response.json();
        tbody.innerHTML = '';

        if (tickets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">Chưa có phiếu mượn nào.</td></tr>';
            return;
        }

        tickets.forEach(ticket => {
            let itemsText = '<i>Chưa có sách</i>';
            if (ticket.items && ticket.items.length > 0) {
                itemsText = ticket.items.map(item => `• Sách: <b>${item.bookId}</b> (Số lượng: ${item.quantity})`).join('<br>');
            }

            const row = `
                <tr>
                    <td><b>${ticket.ticketId}</b></td>
                    <td>${ticket.readerId}</td>
                    <td>${ticket.borrowDate || ''}</td>
                    <td>${ticket.dueDate || ''}</td>
                    <td class="status">${ticket.status || ''}</td>
                    <td>${itemsText}</td>
                </tr>
            `;
            tbody.innerHTML += row;
        });
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="6" style="color:#400909;">Lỗi kết nối tới máy chủ!</td></tr>';
    }
}