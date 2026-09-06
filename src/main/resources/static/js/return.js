let borrowingTickets = [];

document.addEventListener("DOMContentLoaded", () => {
  setDefaultReturnDate();
  initializeReturnForm();
  initializeSidebar();
  loadBorrowingTickets();
});

function setDefaultReturnDate() {
  const dateInput = document.getElementById("actualReturnDate");
  if (!dateInput) return;

  const today = new Date();
  const localDate = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0"),
  ].join("-");

  dateInput.value = localDate;
}

function initializeReturnForm() {
  const form = document.getElementById("returnForm");
  const ticketSelect = document.getElementById("ticketId");

  if (!form || !ticketSelect) return;

  ticketSelect.addEventListener("change", showSelectedTicket);
  form.addEventListener("submit", submitReturn);
}

async function loadBorrowingTickets() {
  const ticketSelect = document.getElementById("ticketId");
  const submitButton = document.getElementById("returnSubmit");

  if (!ticketSelect || !submitButton) return;

  ticketSelect.disabled = true;
  submitButton.disabled = true;

  try {
    const tickets = await apiRequest("/borrow-tickets");

    borrowingTickets = (tickets || []).filter(
      (ticket) => ticket.status === "BORROWING",
    );

    renderTicketOptions();
  } catch (error) {
    borrowingTickets = [];

    ticketSelect.innerHTML =
      '<option value="">Không thể tải danh sách phiếu</option>';

    showReturnMessage(
      error.message || "Không thể tải danh sách phiếu mượn.",
      true,
    );
  } finally {
    ticketSelect.disabled = false;
  }
}

function renderTicketOptions() {
  const ticketSelect = document.getElementById("ticketId");
  const submitButton = document.getElementById("returnSubmit");

  if (!ticketSelect || !submitButton) return;

  ticketSelect.replaceChildren();

  const defaultOption = document.createElement("option");
  defaultOption.value = "";

  if (borrowingTickets.length === 0) {
    defaultOption.textContent = "Không có phiếu đang mượn";
    ticketSelect.appendChild(defaultOption);
    ticketSelect.disabled = true;
    submitButton.disabled = true;
    return;
  }

  defaultOption.textContent = "Chọn phiếu mượn";
  ticketSelect.appendChild(defaultOption);

  for (const ticket of borrowingTickets) {
    const option = document.createElement("option");

    option.value = ticket.ticketId;
    option.textContent = `${ticket.ticketId} — Bạn đọc: ${ticket.readerId}`;

    ticketSelect.appendChild(option);
  }

  ticketSelect.disabled = false;
  submitButton.disabled = false;
}

function showSelectedTicket() {
  const ticketId = document.getElementById("ticketId").value;
  const detailsSection = document.getElementById("ticketDetails");
  const submitButton = document.getElementById("returnSubmit");
  const resultSection = document.getElementById("result");

  resultSection.hidden = true;
  clearReturnMessage();

  const ticket = borrowingTickets.find((item) => item.ticketId === ticketId);

  if (!ticket) {
    detailsSection.hidden = true;
    submitButton.disabled = true;
    return;
  }

  submitButton.disabled = false;
  detailsSection.hidden = false;

  setText("detailTicketId", ticket.ticketId || "—");
  setText("detailReaderId", ticket.readerId || "—");
  setText("detailBorrowDate", formatDate(ticket.borrowDate));
  setText("detailDueDate", formatDate(ticket.dueDate));

  renderTicketItems(ticket.items || []);
  updateReturnDateLimit(ticket);
  updateOverdueWarning(ticket);
}

function renderTicketItems(items) {
  const list = document.getElementById("detailItems");
  if (!list) return;

  list.replaceChildren();

  if (items.length === 0) {
    const item = document.createElement("li");
    item.textContent = "Phiếu chưa có thông tin sách.";
    list.appendChild(item);
    return;
  }

  for (const book of items) {
    const item = document.createElement("li");
    item.textContent = `${book.bookId} — Số lượng: ${book.quantity}`;
    list.appendChild(item);
  }
}

function updateReturnDateLimit(ticket) {
  const dateInput = document.getElementById("actualReturnDate");
  if (!dateInput) return;

  dateInput.min = ticket.borrowDate || "";
}

function updateOverdueWarning(ticket) {
  const warning = document.getElementById("overdueWarning");
  if (!warning) return;

  const today = getLocalDateString();

  if (ticket.dueDate && ticket.dueDate < today) {
    warning.textContent =
      "Phiếu này đã quá hạn. Hệ thống sẽ tính tiền phạt khi xác nhận trả sách.";
  } else {
    warning.textContent = "";
  }
}

async function submitReturn(event) {
  event.preventDefault();

  const ticketSelect = document.getElementById("ticketId");
  const dateInput = document.getElementById("actualReturnDate");
  const submitButton = document.getElementById("returnSubmit");
  const resultSection = document.getElementById("result");

  const ticketId = ticketSelect.value;
  const actualReturnDate = dateInput.value;

  if (!ticketId) {
    showReturnMessage("Vui lòng chọn phiếu mượn.", true);
    return;
  }

  if (!actualReturnDate) {
    showReturnMessage("Vui lòng chọn ngày trả thực tế.", true);
    return;
  }

  submitButton.disabled = true;
  submitButton.innerHTML =
    '<i class="bi bi-hourglass-split"></i> Đang xử lý...';

  resultSection.hidden = true;
  clearReturnMessage();

  try {
    const result = await apiRequest("/returns", {
      method: "POST",
      body: JSON.stringify({
        ticketId,
        actualReturnDate,
      }),
    });

    setText("lateDays", formatNumber(result.lateDays));
    setText("fineAmount", `${formatNumber(result.fineAmount)} đ`);

    resultSection.hidden = false;

    showReturnMessage("Trả sách thành công.", false);

    await loadBorrowingTickets();

    ticketSelect.value = "";
    document.getElementById("ticketDetails").hidden = true;
  } catch (error) {
    resultSection.hidden = true;

    showReturnMessage(error.message || "Không thể thực hiện trả sách.", true);
  } finally {
    submitButton.innerHTML =
      '<i class="bi bi-check2-circle"></i> Xác nhận trả sách';

    submitButton.disabled =
      borrowingTickets.length === 0 || ticketSelect.value === "";
  }
}

function showReturnMessage(text, isError) {
  const element = document.getElementById("message");
  if (!element) return;

  element.textContent = text;
  element.className = isError ? "error" : "success";
}

function clearReturnMessage() {
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

function getLocalDateString() {
  const date = new Date();

  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0"),
  ].join("-");
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
