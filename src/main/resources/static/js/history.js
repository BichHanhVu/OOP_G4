let allReturnRecords = [];

document.addEventListener("DOMContentLoaded", () => {
  initializeHistoryFilters();
  initializeSidebar();
  loadReturnHistory();
});

function initializeHistoryFilters() {
  const searchInput = document.getElementById("historySearch");
  const filterSelect = document.getElementById("historyFilter");

  if (searchInput) {
    searchInput.addEventListener("input", filterReturnHistory);
  }

  if (filterSelect) {
    filterSelect.addEventListener("change", filterReturnHistory);
  }
}

async function loadReturnHistory() {
  const tableBody = document.getElementById("historyBody");

  if (!tableBody) return;

  tableBody.innerHTML = `
    <tr>
      <td colspan="6" class="loading-state">
        Đang tải lịch sử trả sách...
      </td>
    </tr>
  `;

  clearHistoryMessage();

  try {
    const records = await apiRequest("/returns");

    allReturnRecords = Array.isArray(records) ? records : [];

    updateHistoryStatistics(allReturnRecords);
    renderReturnHistory(allReturnRecords);
  } catch (error) {
    allReturnRecords = [];

    updateHistoryStatistics([]);

    tableBody.innerHTML = `
      <tr>
        <td colspan="6" class="empty-state">
          Không thể tải lịch sử trả sách.
        </td>
      </tr>
    `;

    showHistoryMessage(
      error.message || "Không thể tải lịch sử trả sách.",
      true,
    );
  }
}

function filterReturnHistory() {
  const keyword = (document.getElementById("historySearch")?.value || "")
    .toLowerCase()
    .trim();

  const filter = document.getElementById("historyFilter")?.value || "";

  const filteredRecords = allReturnRecords.filter((record) => {
    const returnId = String(record.returnId || "").toLowerCase();
    const ticketId = String(record.ticketId || "").toLowerCase();
    const lateDays = Number(record.lateDays || 0);

    const matchesKeyword =
      returnId.includes(keyword) || ticketId.includes(keyword);

    const matchesFilter =
      filter === "" ||
      (filter === "on-time" && lateDays === 0) ||
      (filter === "late" && lateDays > 0);

    return matchesKeyword && matchesFilter;
  });

  renderReturnHistory(filteredRecords);
}

function renderReturnHistory(records) {
  const tableBody = document.getElementById("historyBody");
  if (!tableBody) return;

  tableBody.replaceChildren();

  if (records.length === 0) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");

    cell.colSpan = 6;
    cell.className = "empty-state";
    cell.textContent = "Không có lịch sử trả sách phù hợp.";

    row.appendChild(cell);
    tableBody.appendChild(row);

    return;
  }

  for (const record of records) {
    const row = document.createElement("tr");
    const lateDays = Number(record.lateDays || 0);
    const fineAmount = Number(record.fineAmount || 0);

    appendCell(row, record.returnId || "—", true);
    appendCell(row, record.ticketId || "—");
    appendCell(row, formatDate(record.actualReturnDate));
    appendCell(row, formatNumber(lateDays));
    appendCell(row, `${formatNumber(fineAmount)} đ`);

    const statusCell = document.createElement("td");
    const badge = document.createElement("span");

    badge.className =
      lateDays > 0
        ? "status-badge status-overdue"
        : "status-badge status-returned";

    badge.textContent = lateDays > 0 ? "Trả quá hạn" : "Đúng hạn";

    statusCell.appendChild(badge);
    row.appendChild(statusCell);

    tableBody.appendChild(row);
  }
}

function appendCell(row, value, strong = false) {
  const cell = document.createElement("td");

  if (strong) {
    const strongElement = document.createElement("strong");
    strongElement.textContent = value;
    cell.appendChild(strongElement);
  } else {
    cell.textContent = value;
  }

  row.appendChild(cell);
}

function updateHistoryStatistics(records) {
  const totalReturns = records.length;

  const lateReturns = records.filter(
    (record) => Number(record.lateDays || 0) > 0,
  ).length;

  const totalFine = records.reduce(
    (sum, record) => sum + Number(record.fineAmount || 0),
    0,
  );

  setText("totalReturns", formatNumber(totalReturns));
  setText("lateReturns", formatNumber(lateReturns));
  setText("historyFineAmount", `${formatNumber(totalFine)} đ`);
}

function showHistoryMessage(text, isError) {
  const element = document.getElementById("message");
  if (!element) return;

  element.textContent = text;
  element.className = isError ? "error" : "success";
}

function clearHistoryMessage() {
  const element = document.getElementById("message");
  if (!element) return;

  element.textContent = "";
  element.className = "";
}

function setText(elementId, value) {
  const element = document.getElementById(elementId);

  if (element) {
    element.textContent = value;
  }
}

function formatNumber(value) {
  return Number(value ?? 0).toLocaleString("vi-VN");
}

function formatDate(value) {
  if (!value) return "—";

  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
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
