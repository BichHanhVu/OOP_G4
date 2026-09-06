let currentPage = 0;
let totalPages = 0;

async function loadReaders() {
    try {
        const params = new URLSearchParams();
        const keyword = document.getElementById("searchInput").value.trim();
        const type = document.getElementById("filterType").value;
        const sortBy = document.getElementById("sortBy").value;
        const sortDirection = document.getElementById("sortDirection").value;
        const size = document.getElementById("pageSize").value;

        if (keyword) params.set("keyword", keyword);
        if (type) params.set("type", type);
        params.set("sortBy", sortBy);
        params.set("sortDirection", sortDirection);
        params.set("page", currentPage);
        params.set("size", size);

        const result = await apiRequest(`/readers?${params.toString()}`);
        renderTable(result.content);
        renderPagination(result);
    } catch (e) {
        showMessage(e.message, true);
    }
}

function onFilterChanged() {
    currentPage = 0;
    loadReaders();
}

function goToPage(page) {
    if (page < 0 || page >= totalPages) {
        return;
    }
    currentPage = page;
    loadReaders();
}

function renderPagination(result) {
    totalPages = result.totalPages;
    const pageInfo = document.getElementById("pageInfo");
    const total = result.totalElements;

    if (total === 0) {
        pageInfo.textContent = "Không có bạn đọc nào";
    } else {
        const start = result.page * result.size + 1;
        const end = Math.min(start + result.content.length - 1, total);
        pageInfo.textContent = `Hiển thị ${start}-${end} trong tổng số ${total} bạn đọc`;
    }

    document.getElementById("prevPageBtn").disabled = !result.hasPrevious;
    document.getElementById("nextPageBtn").disabled = !result.hasNext;
}

function readerTypeLabel() {
    return { STUDENT: "Sinh viên thường", PRIORITY_STUDENT: "Sinh viên ưu tiên", LECTURER: "Giảng viên" };
}

function renderTable(readers) {
    const typeLabel = readerTypeLabel();
    const tbody = document.getElementById("readerTableBody");
    const emptyState = document.getElementById("emptyState");
    tbody.innerHTML = "";

    if (readers.length === 0) {
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    readers.forEach(r => {
        const tr = document.createElement("tr");

        const cellValues = [
            r.id,
            r.name,
            r.phoneNumber,
            typeLabel[r.type] || r.type,
            r.maxBorrowLimit,
        ];
        cellValues.forEach((value) => {
            const td = document.createElement("td");
            td.textContent = value;
            tr.appendChild(td);
        });

        const actionTd = document.createElement("td");
        actionTd.style.display = "flex";
        actionTd.style.gap = "8px";

        const detailBtn = document.createElement("button");
        detailBtn.className = "btn btn-outline";
        detailBtn.innerHTML = '<i class="bi bi-eye"></i> Chi tiết';
        detailBtn.onclick = () => openDetail(r.id);

        const editBtn = document.createElement("button");
        editBtn.className = "btn btn-secondary";
        editBtn.innerHTML = '<i class="bi bi-pencil"></i> Sửa';
        editBtn.onclick = () => openEditForm(r.id);

        const deleteBtn = document.createElement("button");
        deleteBtn.className = "btn btn-danger";
        deleteBtn.innerHTML = '<i class="bi bi-trash"></i> Xóa';
        deleteBtn.onclick = () => deleteReader(r.id);

        actionTd.appendChild(detailBtn);
        actionTd.appendChild(editBtn);
        actionTd.appendChild(deleteBtn);
        tr.appendChild(actionTd);

        tbody.appendChild(tr);
    });
}

function openCreateForm() {
    document.getElementById("formTitle").textContent = "Thêm bạn đọc";
    document.getElementById("editingId").value = "";
    document.getElementById("nameInput").value = "";
    document.getElementById("phoneInput").value = "";
    document.getElementById("typeInput").value = "STUDENT";
    document.getElementById("readerForm").style.display = "block";
}

async function openEditForm(id) {
    try {
        const reader = await apiRequest(`/readers/${id}`);
        document.getElementById("formTitle").textContent = "Sửa bạn đọc";
        document.getElementById("editingId").value = reader.id;
        document.getElementById("nameInput").value = reader.name;
        document.getElementById("phoneInput").value = reader.phoneNumber;
        document.getElementById("typeInput").value = reader.type;
        document.getElementById("readerForm").style.display = "block";
    } catch (e) {
        showMessage(e.message, true);
    }
}

function closeForm() {
    document.getElementById("readerForm").style.display = "none";
}

async function submitForm() {
    const id = document.getElementById("editingId").value;
    const payload = {
        name: document.getElementById("nameInput").value,
        phoneNumber: document.getElementById("phoneInput").value,
        type: document.getElementById("typeInput").value,
    };
    try {
        if (id) {
            await apiRequest(`/readers/${id}`, {
                method: "PUT",
                body: JSON.stringify(payload),
            });
            showMessage("Cập nhật thành công");
        } else {
            await apiRequest(`/readers`, {
                method: "POST",
                body: JSON.stringify(payload),
            });
            showMessage("Thêm bạn đọc thành công");
        }
        closeForm();
        loadReaders();
        loadStatistics();
    } catch (e) {
        showMessage(e.message, true);
    }
}

async function deleteReader(id) {
    if (!confirm(`Xóa bạn đọc ${id}?`)) return;
    try {
        await apiRequest(`/readers/${id}`, { method: "DELETE" });
        showMessage("Xóa thành công");
        loadReaders();
        loadStatistics();
    } catch (e) {
        showMessage(e.message, true);
    }
}

function showMessage(text, isError = false) {
    const el = document.getElementById("message");
    el.textContent = text;
    el.className = isError ? "error" : "success";
    setTimeout(() => {
        el.textContent = "";
    }, 3000);
}

async function importCsv() {
    const fileInput = document.getElementById("importFileInput");
    if (!fileInput.files.length) {
        showMessage("Vui lòng chọn file CSV", true);
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        const res = await fetch(`${API_BASE}/readers/import`, { method: "POST", body: formData });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: "Import thất bại" }));
            throw new Error(err.message || "Import thất bại");
        }
        const summary = await res.json();
        renderImportSummary(summary);
        showMessage(`Import xong: ${summary.successCount}/${summary.totalRows} thành công`);
        fileInput.value = "";
        loadReaders();
        loadStatistics();
    } catch (e) {
        showMessage(e.message, true);
    }
}

function renderImportSummary(summary) {
    const el = document.getElementById("importResult");
    el.style.display = "block";
    el.innerHTML = "";

    const title = document.createElement("p");
    title.textContent = `Tổng ${summary.totalRows} dòng — ${summary.successCount} thành công, ${summary.failureCount} lỗi`;
    el.appendChild(title);

    if (summary.failureCount > 0) {
        const table = document.createElement("table");
        table.innerHTML = "<thead><tr><th>Dòng</th><th>Kết quả</th></tr></thead>";
        const tbody = document.createElement("tbody");
        summary.results.filter(r => !r.success).forEach(r => {
            const tr = document.createElement("tr");
            const tdRow = document.createElement("td");
            tdRow.textContent = r.rowNumber;
            const tdMsg = document.createElement("td");
            tdMsg.textContent = r.message;
            tr.appendChild(tdRow);
            tr.appendChild(tdMsg);
            tbody.appendChild(tr);
        });
        table.appendChild(tbody);
        el.appendChild(table);
    }
}

async function loadStatistics() {
    try {
        const stats = await apiRequest("/readers/statistics");
        document.getElementById("statTotalReaders").textContent = stats.totalReaders;
        document.getElementById("statBorrowing").textContent = stats.currentlyBorrowingReaderCount;
        document.getElementById("statOverdue").textContent = stats.overdueReaderCount;
        document.getElementById("statReachedLimit").textContent = stats.reachedLimitReaderCount;
        renderStatsByType(stats.countByType);
    } catch (e) {
        showMessage(e.message, true);
    }
}

function renderStatsByType(countByType) {
    const typeLabel = readerTypeLabel();
    const tbody = document.getElementById("statsByTypeBody");
    tbody.innerHTML = "";

    Object.keys(countByType).forEach(type => {
        const tr = document.createElement("tr");

        const tdType = document.createElement("td");
        tdType.textContent = typeLabel[type] || type;

        const tdCount = document.createElement("td");
        tdCount.textContent = countByType[type];

        tr.appendChild(tdType);
        tr.appendChild(tdCount);
        tbody.appendChild(tr);
    });
}

async function openDetail(id) {
    try {
        const detail = await apiRequest(`/readers/${id}/detail`);
        const typeLabel = readerTypeLabel();

        document.getElementById("detailReaderName").textContent = `${detail.name} (${detail.id})`;
        document.getElementById("detailReaderMeta").textContent =
            `${typeLabel[detail.type] || detail.type} — SĐT: ${detail.phoneNumber} — Giới hạn mượn: ${detail.maxBorrowLimit}`;

        renderDetailSummary(detail.borrowSummary);
        renderDetailTickets(detail.borrowSummary.tickets);

        document.getElementById("readerDetailPanel").style.display = "block";
        document.getElementById("readerDetailPanel").scrollIntoView({ behavior: "smooth" });
    } catch (e) {
        showMessage(e.message, true);
    }
}

function renderDetailSummary(summary) {
    const summaryEl = document.getElementById("detailSummary");
    summaryEl.innerHTML = "";

    const items = [
        ["Đang giữ", summary.currentlyBorrowedCount],
        ["Phiếu đang mượn", summary.activeTicketCount],
        ["Phiếu quá hạn", summary.overdueTicketCount],
        ["Đã đạt giới hạn mượn", summary.reachedLimit ? "Có" : "Không"],
    ];

    items.forEach(([label, value]) => {
        const p = document.createElement("p");
        p.textContent = `${label}: ${value}`;
        summaryEl.appendChild(p);
    });
}

function renderDetailTickets(tickets) {
    const tbody = document.getElementById("detailTicketsBody");
    const emptyState = document.getElementById("detailEmptyState");
    tbody.innerHTML = "";

    if (tickets.length === 0) {
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    tickets.forEach(t => {
        const tr = document.createElement("tr");
        if (t.overdue) {
            tr.style.color = "#c0392b";
        }

        const cells = [t.ticketId, t.borrowDate, t.dueDate, t.status + (t.overdue ? " (quá hạn)" : "")];
        cells.forEach(value => {
            const td = document.createElement("td");
            td.textContent = value;
            tr.appendChild(td);
        });

        const tdBooks = document.createElement("td");
        tdBooks.textContent = t.books.map(b => `${b.title} x${b.quantity}`).join(", ");
        tr.appendChild(tdBooks);

        tbody.appendChild(tr);
    });
}

function closeDetail() {
    document.getElementById("readerDetailPanel").style.display = "none";
}

document.addEventListener("DOMContentLoaded", () => {
    loadReaders();
    loadStatistics();
});