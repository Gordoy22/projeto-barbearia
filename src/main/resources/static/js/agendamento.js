document.addEventListener("DOMContentLoaded", () => {
    const servico = document.getElementById("servicoId");
    const horaInicio = document.getElementById("horaInicio");
    const horaFim = document.getElementById("horaFim");
    const valor = document.getElementById("valor");
    const duracao = document.getElementById("duracaoInfo");

    const formatMoney = (amount) => {
        return Number(amount).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
    };

    const toTime = (date) => date.toTimeString().slice(0, 5);

    const atualizarHorarioFinal = (duracaoMinutos) => {
        if (!horaInicio?.value || !duracaoMinutos) {
            return;
        }
        const [hours, minutes] = horaInicio.value.split(":").map(Number);
        const end = new Date();
        end.setHours(hours, minutes + Number(duracaoMinutos), 0, 0);
        if (horaFim) {
            horaFim.value = toTime(end);
        }
    };

    const carregarServico = async () => {
        if (!servico?.value) {
            return;
        }
        const response = await fetch(`/servicos/${servico.value}/json`);
        if (!response.ok) {
            return;
        }
        const data = await response.json();
        if (valor) {
            valor.value = data.valor;
        }
        if (duracao) {
            duracao.textContent = `${data.duracaoMinutos} minutos`;
        }
        atualizarHorarioFinal(data.duracaoMinutos);
        servico.dataset.duracao = data.duracaoMinutos;
    };

    servico?.addEventListener("change", carregarServico);
    horaInicio?.addEventListener("change", () => atualizarHorarioFinal(servico?.dataset.duracao));

    if (servico?.value) {
        carregarServico();
    }
});
