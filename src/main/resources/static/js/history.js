document.addEventListener("DOMContentLoaded", async () => {
    try {
        const records = await apiRequest("/returns");
        const body = document.getElementById("historyBody");
        body.replaceChildren();
        for (const record of records) {
            const row = document.createElement("tr");
            for (const value of [record.returnId, record.ticketId, record.actualReturnDate,
                record.lateDays, `${Number(record.fineAmount).toLocaleString("vi-VN")} đ`]) {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            }
            body.appendChild(row);
        }
    } catch (error) {
        const message = document.getElementById("message");
        message.textContent = error.message;
        message.className = "error";
    }
});
