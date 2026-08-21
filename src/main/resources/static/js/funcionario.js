document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-mask=phone]").forEach((input) => {
        input.addEventListener("input", () => {
            const digits = input.value.replace(/\D/g, "").slice(0, 11);
            input.value = digits.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
        });
    });
});
