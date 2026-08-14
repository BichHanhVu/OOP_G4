const API_BASE = "/api";

async function apiRequest(path, options = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: "Lỗi không xác định" }));
        throw new Error(err.message || "Lỗi không xác định");
    }
    if (res.status === 204) return null;
    return res.json();
}
