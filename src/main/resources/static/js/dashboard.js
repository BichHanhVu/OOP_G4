document.addEventListener("DOMContentLoaded", () => {
  loadDashboard();
  displayCurrentDate();
  initializeSidebar();
});

/**
 * Tải dữ liệu thống kê từ backend.
 */
async function loadDashboard() {
  setText("dashboardError", "");

  try {
    const data = await apiRequest("/dashboard");

    const totalReaders = Number(data.totalReaders ?? 0);
    const totalBooks = Number(data.totalBooks ?? 0);
    const borrowingTickets = Number(data.borrowingTickets ?? 0);
    const overdueTickets = Number(data.overdueTickets ?? 0);
    const totalFineAmount = Number(data.totalFineAmount ?? 0);

    setText("totalReaders", formatNumber(totalReaders));
    setText("totalBooks", formatNumber(totalBooks));
    setText("borrowingTickets", formatNumber(borrowingTickets));
    setText("overdueTickets", formatNumber(overdueTickets));

    setText("overviewReaders", formatNumber(totalReaders));
    setText("overviewBooks", formatNumber(totalBooks));
    setText("overviewBorrowing", formatNumber(borrowingTickets));
    setText("overviewOverdue", formatNumber(overdueTickets));

    setText("totalFineAmount", `${formatNumber(totalFineAmount)} đ`);
  } catch (error) {
    console.error("Không thể tải dashboard:", error);

    setText(
      "dashboardError",
      error.message || "Không thể tải dữ liệu dashboard.",
    );
  }
}

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

function displayCurrentDate() {
  const currentDateElement = document.getElementById("currentDate");
  if (!currentDateElement) return;

  const currentDate = new Intl.DateTimeFormat("vi-VN", {
    weekday: "long",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date());

  currentDateElement.innerHTML = `
    <i class="bi bi-calendar3"></i>
    <span>${currentDate}</span>
  `;
}

function initializeSidebar() {
  const menuToggle = document.getElementById("menuToggle");
  const appSidebar = document.getElementById("appSidebar");

  if (!menuToggle || !appSidebar) return;

  menuToggle.addEventListener("click", (event) => {
    event.stopPropagation();
    appSidebar.classList.toggle("open");
  });

  document.addEventListener("click", (event) => {
    const clickedInsideSidebar = appSidebar.contains(event.target);
    const clickedMenuButton = menuToggle.contains(event.target);

    if (
      window.innerWidth <= 768 &&
      !clickedInsideSidebar &&
      !clickedMenuButton
    ) {
      appSidebar.classList.remove("open");
    }
  });
}
