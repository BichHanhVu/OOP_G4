const API_BASE_URL = "/api";

async function apiRequest(endpoint, options = {}) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    let message = "Đã xảy ra lỗi.";

    try {
      const errorData = await response.json();
      message = errorData.message || message;
    } catch (error) {
      console.error("Không thể đọc nội dung lỗi:", error);
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
