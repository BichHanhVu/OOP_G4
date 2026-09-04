async function loadReaders(keyword = "", type = "") {
    try {
        const params = new URLSearchParams();
        if (keyword) params.set("keyword", keyword);
        if (type) params.set("type", type);
        const query = params.toString() ? `?${params.toString()}` : "";
        renderTable(await apiRequest(`/readers${query}`));
    } catch (e) {
        showMessage(e.message, true);
    }
}

function renderTable(readers) {
    const typeLabel = { STUDENT: "Sinh viên thường", PRIORITY_STUDENT: "Sinh viên ưu tiên", LECTURER: "Giảng viên" };
    const tbody = document.getElementById("readerTableBody");
    tbody.innerHTML = "";

    readers.forEach(r => {
        const tr = document.createElement("tr");

        const cellValues = [r.id, r.name, r.phoneNumber, typeLabel[r.type] || r.type, r.maxBorrowLimit];
        cellValues.forEach(value => {
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

function searchReaders() {
    loadReaders(
        document.getElementById("searchInput").value,
        document.getElementById("filterType").value
    );
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
            await apiRequest(`/readers/${id}`, { method: "PUT", body: JSON.stringify(payload) });
            showMessage("Cập nhật thành công");
        } else {
            await apiRequest(`/readers`, { method: "POST", body: JSON.stringify(payload) });
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
    setTimeout(() => { el.textContent = ""; }, 3000);
}

document.addEventListener("DOMContentLoaded", () => loadReaders());
 