document.addEventListener("DOMContentLoaded", async () => {
    try {
        const data = await apiRequest("/dashboard");
        for (const [id, value] of Object.entries({
            totalReaders: data.totalReaders,
            totalBooks: data.totalBooks,
            borrowingTickets: data.borrowingTickets,
            overdueTickets: data.overdueTickets,
            totalFineAmount: `${Number(data.totalFineAmount).toLocaleString("vi-VN")} đ`
        })) document.getElementById(id).textContent = value;
    } catch (error) {
        document.getElementById("dashboardError").textContent = error.message;
    }
});
