let allTickets = [];
let selectedTicket = null;


document.addEventListener("DOMContentLoaded", () => {

    loadTickets();

    document
        .getElementById("ticketSearch")
        .addEventListener("input", filterTickets);

    document
        .getElementById("ticketStatus")
        .addEventListener("change", filterTickets);


    // ================= RENEW MODAL =================

    const closeButton =
        document.getElementById("closeRenewModal");

    const cancelButton =
        document.getElementById("cancelRenew");

    const confirmButton =
        document.getElementById("confirmRenew");


    if (closeButton) {
        closeButton.addEventListener(
            "click",
            closeRenewModal
        );
    }


    if (cancelButton) {
        cancelButton.addEventListener(
            "click",
            closeRenewModal
        );
    }


    if (confirmButton) {
        confirmButton.addEventListener(
            "click",
            confirmRenewal
        );
    }

});


// ======================================================
// LOAD TICKETS
// ======================================================

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


        allTickets =
            await response.json();


        renderTickets(allTickets);


    } catch (error) {

        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="error">
                    ${error.message}
                </td>
            </tr>
        `;

    }

}


// ======================================================
// FILTER
// ======================================================

function filterTickets() {

    const keyword =
        document
            .getElementById("ticketSearch")
            .value
            .toLowerCase()
            .trim();


    const status =
        document
            .getElementById("ticketStatus")
            .value;


    const today =
        new Date()
            .toISOString()
            .slice(0, 10);


    const filteredTickets =
        allTickets.filter(ticket => {


            // ------------------------------------------
            // Kiểm tra quá hạn
            // ------------------------------------------

            const overdue =
                ticket.status === "BORROWING" &&
                ticket.dueDate &&
                ticket.dueDate < today;


            // ------------------------------------------
            // Tìm theo mã phiếu hoặc mã bạn đọc
            // ------------------------------------------

            const matchesText =
                (ticket.ticketId || "")
                    .toLowerCase()
                    .includes(keyword)

                ||

                (ticket.readerId || "")
                    .toLowerCase()
                    .includes(keyword);


            // ------------------------------------------
            // Lọc trạng thái
            // ------------------------------------------

            const matchesStatus =
                !status

                ||

                (
                    status === "OVERDUE"
                        ? overdue
                        : ticket.status === status
                );


            return (
                matchesText &&
                matchesStatus
            );

        });


    renderTickets(filteredTickets);

}


// ======================================================
// RENDER TICKETS
// ======================================================

function renderTickets(tickets) {

    const tbody =
        document.getElementById("ticketsTableBody");


    tbody.innerHTML = "";


    // ------------------------------------------
    // Không có dữ liệu
    // ------------------------------------------

    if (!tickets.length) {

        tbody.innerHTML = `
            <tr>
                <td colspan="7">
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


        // ------------------------------------------
        // Kiểm tra quá hạn
        // ------------------------------------------

        const overdue =
            ticket.status === "BORROWING" &&
            ticket.dueDate &&
            ticket.dueDate < today;


        // ------------------------------------------
        // Tên trạng thái
        // ------------------------------------------

        const label =
            overdue
                ? "Quá hạn"
                : ticket.status === "RETURNED"
                    ? "Đã trả"
                    : "Đang mượn";


        // ------------------------------------------
        // CSS class của badge
        // ------------------------------------------

        const badge =
            overdue
                ? "status-overdue"
                : ticket.status === "RETURNED"
                    ? "status-returned"
                    : "status-borrowing";


        // ------------------------------------------
        // Danh sách sách
        // ------------------------------------------

        const items =
            (ticket.items || [])
                .map(
                    item =>
                        `${item.bookId} (${item.quantity})`
                )
                .join(", ")
            || "Chưa có sách";


        // ------------------------------------------
        // Tạo dòng
        // ------------------------------------------

        const row =
            document.createElement("tr");


        // ------------------------------------------
        // Các thông tin cơ bản
        //
        // 1. Mã phiếu
        // 2. Mã bạn đọc
        // 3. Ngày mượn
        // 4. Hạn trả
        // ------------------------------------------

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

            td.textContent =
                value || "";

            row.appendChild(td);

        }


        // ------------------------------------------
        // Cột trạng thái
        // ------------------------------------------

        const statusTd =
            document.createElement("td");


        statusTd.innerHTML = `
            <span class="status-badge ${badge}">
                ${label}
            </span>
        `;


        row.appendChild(statusTd);


        // ------------------------------------------
        // Cột sách
        // ------------------------------------------

        const itemsTd =
            document.createElement("td");


        itemsTd.textContent =
            items;


        row.appendChild(itemsTd);


        // ==================================================
        // CỘT THAO TÁC - GIA HẠN
        // ==================================================

        const actionTd =
            document.createElement("td");


        // Chỉ được gia hạn khi:
        //
        // 1. Phiếu đang BORROWING
        // 2. Có dueDate
        // 3. Chưa quá hạn
        // 4. Chưa gia hạn lần nào

        const canRenew =
            ticket.status === "BORROWING" &&
            ticket.dueDate &&
            ticket.dueDate >= today &&
            (ticket.renewalCount || 0) < 1;


        if (canRenew) {

            const renewButton =
                document.createElement("button");


            renewButton.type =
                "button";


            renewButton.className =
                "btn btn-primary";


            renewButton.innerHTML =
                `<i class="bi bi-calendar-plus"></i> Gia hạn`;


            renewButton.addEventListener(
                "click",
                () => openRenewModal(ticket)
            );


            actionTd.appendChild(
                renewButton
            );


        } else {

            const span =
                document.createElement("span");


            span.textContent =
                "Không thể gia hạn";


            actionTd.appendChild(
                span
            );

        }


        row.appendChild(actionTd);


        // ------------------------------------------
        // Thêm row vào table
        // ------------------------------------------

        tbody.appendChild(row);

    }

}


// ======================================================
// OPEN RENEW MODAL
// ======================================================

function openRenewModal(ticket) {

    selectedTicket =
        ticket;


    const ticketIdElement =
        document.getElementById("renewTicketId");


    const currentDueDateElement =
        document.getElementById("currentDueDate");


    const newDueDateElement =
        document.getElementById("newDueDate");


    const messageElement =
        document.getElementById("renewMessage");


    if (ticketIdElement) {

        ticketIdElement.textContent =
            ticket.ticketId || "";

    }


    if (currentDueDateElement) {

        currentDueDateElement.textContent =
            ticket.dueDate || "";

    }


    if (newDueDateElement) {

        newDueDateElement.value =
            "";

        // Ngày tối thiểu = ngày sau hạn cũ
        if (ticket.dueDate) {

            const oldDate =
                new Date(
                    ticket.dueDate + "T00:00:00"
                );


            oldDate.setDate(
                oldDate.getDate() + 1
            );


            const minDate =
                oldDate
                    .toISOString()
                    .slice(0, 10);


            newDueDateElement.min =
                minDate;

        }

    }


    if (messageElement) {

        messageElement.textContent =
            "";

        messageElement.style.display =
            "none";

    }


    const modal =
        document.getElementById("renewModal");


    if (modal) {

        modal.style.display =
            "flex";

    }

}


// ======================================================
// CLOSE RENEW MODAL
// ======================================================

function closeRenewModal() {

    selectedTicket =
        null;


    const modal =
        document.getElementById("renewModal");


    if (modal) {

        modal.style.display =
            "none";

    }

}


// ======================================================
// CONFIRM RENEWAL
// ======================================================

async function confirmRenewal() {

    if (!selectedTicket) {

        return;

    }


    const newDueDate =
        document
            .getElementById("newDueDate")
            .value;


    // ------------------------------------------
    // Kiểm tra ngày
    // ------------------------------------------

    if (!newDueDate) {

        showRenewMessage(
            "Vui lòng chọn ngày hẹn trả mới."
        );

        return;

    }


    // ------------------------------------------
    // Kiểm tra ngày mới phải sau hạn cũ
    // ------------------------------------------

    if (
        selectedTicket.dueDate &&
        newDueDate <= selectedTicket.dueDate
    ) {

        showRenewMessage(
            "Ngày hẹn trả mới phải sau ngày hẹn trả hiện tại."
        );

        return;

    }


    const button =
        document.getElementById("confirmRenew");


    if (button) {

        button.disabled =
            true;

        button.textContent =
            "Đang xử lý...";

    }


    try {

        // ------------------------------------------
        // Gọi API gia hạn
        // ------------------------------------------

        const response =
            await fetch(
                `/api/borrow-tickets/${encodeURIComponent(
                    selectedTicket.ticketId
                )}/renew`,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        newDueDate:
                        newDueDate
                    })
                }
            );


        // ------------------------------------------
        // Đọc response
        // ------------------------------------------

        let data = null;


        try {

            data =
                await response.json();

        } catch (e) {

            // Response không có JSON
            data = null;

        }


        // ------------------------------------------
        // API trả lỗi
        // ------------------------------------------

        if (!response.ok) {

            throw new Error(
                data?.message ||
                data?.error ||
                "Không thể gia hạn phiếu mượn"
            );

        }


        // ------------------------------------------
        // Thành công
        // ------------------------------------------

        closeRenewModal();


        await loadTickets();


        showMessage(
            "Gia hạn phiếu mượn thành công."
        );


    } catch (error) {

        showRenewMessage(
            error.message
        );

    } finally {

        if (button) {

            button.disabled =
                false;

            button.textContent =
                "Xác nhận gia hạn";

        }

    }

}


// ======================================================
// HIỂN THỊ LỖI TRONG MODAL
// ======================================================

function showRenewMessage(message) {

    const element =
        document.getElementById(
            "renewMessage"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.style.display =
        "block";

}


// ======================================================
// HIỂN THỊ MESSAGE THÀNH CÔNG
// ======================================================

function showMessage(message) {

    // Nếu HTML có element #message
    const element =
        document.getElementById("message");


    if (element) {

        element.textContent =
            message;


        element.style.display =
            "block";


        setTimeout(() => {

            element.textContent =
                "";

            element.style.display =
                "none";

        }, 3000);


        return;

    }


    // Nếu HTML không có #message
    // thì dùng alert để tránh lỗi JS

    alert(message);

}