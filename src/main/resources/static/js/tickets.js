
let allTickets = [];


document.addEventListener("DOMContentLoaded", () => {

    loadTickets(

    );

    document
        .getElementById("ticketSearch")
        .addEventListener("input", filterTickets);

    document
        .getElementById("ticketStatus")
        .addEventListener("change", filterTickets);

});


async function loadTickets() {

    const tbody =
        document.getElementById("ticketsTableBody");

    try {

        const response =
            await fetch("/api/borrow-tickets");

        if (!response.ok) {
            throw new Error(
                "Không thể tải dữ liệu phiếu mượn"
            );
        }

        allTickets = await response.json();

        renderTickets(allTickets);

    } catch (error) {

        tbody.innerHTML = `
<tr>
<td colspan="6" class="error">
    ${error.message}
</td>
</tr>
`;

    }

}


function filterTickets(

) {

    const keyword =
        document
            .getElementById("ticketSearch")
            .value
            .toLowerCase()
            .trim();

    const status =
        document.getElementById("ticketStatus").value;

    const today =
        new Date()
            .toISOString()
            .slice(0, 10);


    const filteredTickets =
        allTickets.filter(ticket => {

            // Kiểm tra phiếu có quá hạn hay không
            const overdue =
                ticket.status === "BORROWING" &&
                ticket.dueDate &&
                ticket.dueDate < today;


            // Tìm theo mã phiếu hoặc mã bạn đọc
            const matchesText =
                (ticket.ticketId || "")
                    .toLowerCase()
                    .includes(keyword)

                ||

                (ticket.readerId || "")
                    .toLowerCase()
                    .includes(keyword);


            // Lọc theo trạng thái
            const matchesStatus =
                !status

                ||

                (
                    status === "OVERDUE"
                        ? overdue
                        : ticket.status === status
                );


            return matchesText && matchesStatus;

        });


    renderTickets(filteredTickets);

}


function renderTickets(tickets) {

    const tbody =
        document.getElementById("ticketsTableBody");

    tbody.innerHTML = "";


    // Không có dữ liệu
    if (!tickets.length) {

        tbody.innerHTML = `
<tr>
<td colspan="6">
    Không có phiếu phù hợp.
</td>
</tr>
`;

        return;
    }


    const today =
        new Date()
            .toISOString()
            .slice(0, 10);


    for (const ticket of tickets) {


        // Kiểm tra quá hạn
        const overdue =
            ticket.status === "BORROWING" &&
            ticket.dueDate &&
            ticket.dueDate < today;


        // Tên trạng thái hiển thị
        const label =
            overdue
                ? "Quá hạn"
                : ticket.status === "RETURNED"
                    ? "Đã trả"
                    : "Đang mượn";


        // Class badge
        const badge =
            overdue
                ? "status-overdue"
                : ticket.status === "RETURNED"
                    ? "status-returned"
                    : "status-borrowing";


        // Danh sách sách
        const items =
            (ticket.items || [])
                .map(
                    item =>
                        `${item.bookId} (${item.quantity})`
                )
                .join(", ")
            || "Chưa có sách";


        // Tạo dòng
        const row =
            document.createElement("tr");


        // Các thông tin cơ bản
        for (
            const value of [
                ticket.ticketId,
                ticket.readerId,
                ticket.borrowDate || "",
                ticket.dueDate || ""
            ]
        ) {

            const td =
                document.createElement("td");

            td.textContent = value;

            row.appendChild(td);

        }


        // Cột trạng thái
        const statusTd =
            document.createElement("td");

        statusTd.innerHTML = `
<span class="status-badge ${badge}">
    ${label}
</span>
`;

        row.appendChild(statusTd);


        // Cột sách
        const itemsTd =
            document.createElement("td");

        itemsTd.textContent = items;

        row.appendChild(itemsTd);


        tbody.appendChild(row);

    }

}

