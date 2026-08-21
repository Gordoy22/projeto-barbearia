document.addEventListener("DOMContentLoaded", () => {
    const shell = document.getElementById("appShell");
    const toggle = document.getElementById("sidebarToggle");
    if (shell && localStorage.getItem("barber.sidebar") === "collapsed") {
        shell.classList.add("collapsed");
    }
    toggle?.addEventListener("click", () => {
        shell.classList.toggle("collapsed");
        localStorage.setItem("barber.sidebar", shell.classList.contains("collapsed") ? "collapsed" : "open");
    });

    document.querySelectorAll(".app-toast").forEach((toast) => {
        setTimeout(() => toast.remove(), 4000);
    });

    const modalElement = document.getElementById("confirmModal");
    const modalForm = document.getElementById("confirmModalForm");
    const modalMessage = document.getElementById("confirmModalMessage");
    if (modalElement && modalForm) {
        const modal = new bootstrap.Modal(modalElement);
        document.querySelectorAll("[data-confirm]").forEach((button) => {
            button.addEventListener("click", (event) => {
                event.preventDefault();
                modalMessage.textContent = button.getAttribute("data-confirm") || "Deseja continuar?";
                modalForm.setAttribute("action", button.getAttribute("data-action"));
                modal.show();
            });
        });
    }
});
