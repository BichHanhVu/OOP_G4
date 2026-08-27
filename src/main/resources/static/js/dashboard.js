document.addEventListener("DOMContentLoaded", loadDashboard);

/**
 * Tải dữ liệu thống kê từ backend.
 */
async function loadDashboard() {
    const errorElement = document.getElementById("dashboardError");

    try {
        errorElement.textContent = "";

        const data = await apiRequest("/dashboard");

        const totalReaders = Number(data.totalReaders ?? 0);
        const totalBooks = Number(data.totalBooks ?? 0);
        const borrowingTickets = Number(data.borrowingTickets ?? 0);
        const overdueTickets = Number(data.overdueTickets ?? 0);
        const totalFineAmount = Number(data.totalFineAmount ?? 0);

        // Bốn thẻ thống kê chính
        setText("totalReaders", formatNumber(totalReaders));
        setText("totalBooks", formatNumber(totalBooks));
        setText("borrowingTickets", formatNumber(borrowingTickets));
        setText("overdueTickets", formatNumber(overdueTickets));

        // Danh sách tình trạng thư viện
        setText("overviewReaders", formatNumber(totalReaders));
        setText("overviewBooks", formatNumber(totalBooks));
        setText("overviewBorrowing", formatNumber(borrowingTickets));
        setText("overviewOverdue", formatNumber(overdueTickets));

        // Tổng tiền phạt
        setText(
            "totalFineAmount",
            `${formatNumber(totalFineAmount)} đ`
        );
    } catch (error) {
        console.error("Không thể tải dashboard:", error);

        errorElement.textContent =
            error.message || "Không thể tải dữ liệu dashboard.";
    }
});

/**
 * Gán nội dung cho phần tử nếu phần tử tồn tại.
 */
function setText(elementId, value) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = value;
    }
}

/**
 * Định dạng số theo cách hiển thị của Việt Nam.
 */
function formatNumber(value) {
    return Number(value).toLocaleString("vi-VN");
}