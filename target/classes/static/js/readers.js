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
    document.getElementById("readerTableBody").innerHTML = readers.map(r => `
        <tr>
            <td>${r.id}</td><td>${r.name}</td><td>${r.phoneNumber}</td>
            <td>${typeLabel[r.type] || r.type}</td><td>${r.maxBorrowLimit}</td>
            <td>
                <button onclick="openEditForm('${r.id}')">Sửa</button>
                <button onclick="deleteReader('${r.id}')">Xóa</button>
            </td>
        </tr>`).join("");
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
    document.getElementById("readerForm").style.display = "block";
}

async function openEditForm(id) {
    const reader = await apiRequest(`/readers/${id}`);
    document.getElementById("formTitle").textContent = "Sửa bạn đọc";
    document.getElementById("editingId").value = reader.id;
    document.getElementById("nameInput").value = reader.name;
    document.getElementById("phoneInput").value = reader.phoneNumber;
    document.getElementById("typeInput").value = reader.type;
    document.getElementById("readerForm").style.display = "block";
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
