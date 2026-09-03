document.getElementById("actualReturnDate").value = new Date()
  .toISOString()
  .slice(0, 10);

document
  .getElementById("returnForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      const result = await apiRequest("/returns", {
        method: "POST",
        body: JSON.stringify({
          ticketId: document.getElementById("ticketId").value.trim(),
          actualReturnDate: document.getElementById("actualReturnDate").value,
        }),
      });
      document.getElementById("lateDays").textContent = result.lateDays;
      document.getElementById("fineAmount").textContent =
        `${Number(result.fineAmount).toLocaleString("vi-VN")} đ`;
      document.getElementById("result").hidden = false;
      showReturnMessage("Trả sách thành công", false);
    } catch (error) {
      document.getElementById("result").hidden = true;
      showReturnMessage(error.message, true);
    }
  });

function showReturnMessage(text, error) {
  const element = document.getElementById("message");
  element.textContent = text;
  element.className = error ? "error" : "success";
}
