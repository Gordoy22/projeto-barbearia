document.addEventListener("DOMContentLoaded", () => {
    const maskPhone = (input) => {
        input.addEventListener("input", () => {
            const digits = input.value.replace(/\D/g, "").slice(0, 11);
            if (digits.length <= 10) {
                input.value = digits.replace(/(\d{2})(\d{4})(\d{0,4})/, (match, ddd, part1, part2) => {
                    return part2 ? `(${ddd}) ${part1}-${part2}` : part1 ? `(${ddd}) ${part1}` : `(${ddd}`;
                });
            } else {
                input.value = digits.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
            }
        });
    };

    document.querySelectorAll("[data-mask=phone]").forEach(maskPhone);
});
