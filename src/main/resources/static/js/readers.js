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

function renderTable(readers) {
    const typeLabel = { STUDENT: "Sinh viên thường", PRIORITY_STUDENT: "Sinh viên ưu tiên", LECTURER: "Giảng viên" };
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

        const editBtn = document.createElement("button");
        editBtn.className = "btn btn-secondary";
        editBtn.innerHTML = '<i class="bi bi-pencil"></i> Sửa';
        editBtn.onclick = () => openEditForm(r.id);

        const deleteBtn = document.createElement("button");
        deleteBtn.className = "btn btn-danger";
        deleteBtn.innerHTML = '<i class="bi bi-trash"></i> Xóa';
        deleteBtn.onclick = () => deleteReader(r.id);

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
document.addEventListener("DOMContentLoaded", () => loadReaders());
