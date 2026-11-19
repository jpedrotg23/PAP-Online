/**
 * Handlers dos botões "Remarcar" e "Cancelar" (aluno-inicial + aluno-historico).
 * Estilos são injetados aqui; os botões têm a mesma altura do .portal-button
 * para não ficarem desalinhados ao lado de "Ver detalhes".
 */
(function () {
  'use strict';
  if (window.__papAtendimentoAcoesLoaded) return;
  window.__papAtendimentoAcoesLoaded = true;

  const PRAZO_MS = 24 * 60 * 60 * 1000;
  const MS_MIN = 60 * 1000;
  const MS_HORA = 60 * MS_MIN;
  const MS_DIA = 24 * MS_HORA;

  // ============ Estilos ============
  function garantirEstilos() {
    if (document.getElementById('papAtendimentoAcoesStyles')) return;
    const style = document.createElement('style');
    style.id = 'papAtendimentoAcoesStyles';
    style.textContent = `
      /* Botões — alinhados ao .portal-button (min-height 42px, padding 0 16px) */
      .pap-atendimento-acoes { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
      .pap-atendimento-acao {
        display: inline-flex; align-items: center; justify-content: center;
        min-height: 42px; padding: 0 16px; box-sizing: border-box;
        border: 1.5px solid #c7d9f5; border-radius: 9px;
        background: #fff; color: #2059dc;
        font: 700 12px Inter, Arial, sans-serif;
        cursor: pointer; white-space: nowrap;
        transition: background .15s, border-color .15s, color .15s, box-shadow .15s;
      }
      .pap-atendimento-acao:hover { background: #f0f5ff; border-color: #7ba1f5; }
      .pap-atendimento-acao:focus-visible { outline: none; box-shadow: 0 0 0 3px rgba(32,89,220,.20); }
      .pap-atendimento-acao--perigo { border-color: #f7c9c4; color: #b42318; }
      .pap-atendimento-acao--perigo:hover { background: #fdecea; border-color: #eab1a9; }
      .pap-atendimento-acao--perigo:focus-visible { box-shadow: 0 0 0 3px rgba(180,35,24,.15); }

      /* Modal */
      .pap-modal-overlay {
        position: fixed; inset: 0;
        background: rgba(15,23,42,.55);
        backdrop-filter: blur(2px);
        display: grid; place-items: center; padding: 16px; z-index: 9999;
        animation: papModalFade .18s ease;
      }
      @keyframes papModalFade { from { opacity: 0; } to { opacity: 1; } }
      .pap-modal {
        background: #fff; width: min(560px, 100%); max-height: 90vh;
        border-radius: 14px; box-shadow: 0 20px 60px rgba(0,0,0,.30);
        display: flex; flex-direction: column; overflow: hidden;
        animation: papModalSlide .20s ease;
      }
      @keyframes papModalSlide {
        from { opacity: 0; transform: translateY(8px) scale(.98); }
        to   { opacity: 1; transform: translateY(0) scale(1); }
      }
      .pap-modal-header {
        display: flex; align-items: center; justify-content: space-between;
        padding: 16px 20px; border-bottom: 1px solid #e4eaf2;
      }
      .pap-modal-header h3 { margin: 0; font-size: 15px; color: #1f2937; font-weight: 700; }
      .pap-modal-close {
        background: none; border: 0; font-size: 22px; line-height: 1;
        cursor: pointer; color: #6b7280; padding: 4px 8px; border-radius: 6px;
        transition: background .15s;
      }
      .pap-modal-close:hover { background: #f1f4f9; }
      .pap-modal-body { padding: 18px 20px; overflow-y: auto; flex: 1; }
      .pap-modal-footer {
        display: flex; justify-content: flex-end; gap: 8px;
        padding: 14px 20px; border-top: 1px solid #e4eaf2; background: #fafbfd;
      }
      .pap-modal-btn {
        min-height: 40px; padding: 0 18px; border-radius: 8px; border: 0;
        font: 700 12px Inter, Arial, sans-serif; cursor: pointer;
        transition: opacity .15s, background .15s;
      }
      .pap-modal-btn[disabled] { opacity: .55; cursor: not-allowed; }
      .pap-modal-btn--ghost { background: #eef1f6; color: #374151; }
      .pap-modal-btn--ghost:hover:not([disabled]) { background: #e2e7f0; }
      .pap-modal-btn--primary { background: #2059dc; color: #fff; }
      .pap-modal-btn--primary:hover:not([disabled]) { background: #1a4dc4; }
      .pap-modal-btn--danger { background: #c82333; color: #fff; }
      .pap-modal-btn--danger:hover:not([disabled]) { background: #a81c2a; }

      .pap-modal-text { font-size: 13px; color: #374151; line-height: 1.55; margin: 0 0 12px; }
      .pap-modal-info {
        background: #f7faff; border: 1px solid #d6e3fb; border-radius: 10px;
        padding: 12px 14px; margin-bottom: 12px;
      }
      .pap-modal-info-row {
        display: flex; justify-content: space-between; gap: 12px;
        font-size: 12px; padding: 5px 0;
      }
      .pap-modal-info-row + .pap-modal-info-row {
        border-top: 1px dashed #d6e3fb; margin-top: 5px; padding-top: 10px;
      }
      .pap-modal-info-label { color: #6b7280; font-weight: 500; }
      .pap-modal-info-value { color: #1f2937; font-weight: 600; text-align: right; }
      .pap-modal-previsao {
        padding: 10px 14px; border-radius: 8px; font-size: 12px;
        line-height: 1.5; margin-bottom: 12px;
      }
      .pap-modal-previsao--alerta { background: #fff4e5; border: 1px solid #f5c97a; color: #8a5a00; }
      .pap-modal-previsao--ok     { background: #e9f7f0; border: 1px solid #a8dcc3; color: #0b6b47; }
      .pap-modal-warning {
        background: #fff8e6; border: 1px solid #f5d67a; border-radius: 10px;
        padding: 12px 14px; margin-bottom: 14px;
      }
      .pap-modal-warning b { display: block; color: #8a5a00; margin-bottom: 4px; font-size: 12px; }
      .pap-modal-warning p { margin: 0; font-size: 12px; color: #6b4d00; line-height: 1.5; }
      .pap-modal-warning p + p { margin-top: 6px; }

      .pap-modal-loading { text-align: center; color: #6b7280; font-size: 12px; padding: 20px 0; }
      .pap-modal-empty {
        text-align: center; color: #6b7280; font-size: 12px;
        padding: 20px 12px; border: 1px dashed #dfe6f1; border-radius: 10px; background: #fbfcfe;
      }
      .pap-modal-empty b { display: block; color: #34415a; margin-bottom: 4px; font-size: 13px; }
      .pap-modal-empty p { margin: 0; }

      /* Lista de horários */
      .pap-modal-horarios { display: grid; gap: 12px; }
      .pap-modal-grupo-dia {
        border: 1px solid #e4eaf2; border-radius: 10px; padding: 12px 14px; background: #fff;
      }
      .pap-modal-dia-header {
        display: flex; justify-content: space-between; align-items: baseline; gap: 8px;
        margin-bottom: 10px; font-size: 12px; color: #34415a;
      }
      .pap-modal-dia-header b { font-weight: 700; text-transform: capitalize; }
      .pap-modal-dia-header span {
        font-size: 11px; color: #6b7280; background: #f1f4f9;
        padding: 2px 8px; border-radius: 99px;
      }
      .pap-modal-dia-slots { display: flex; flex-wrap: wrap; gap: 6px; }
      .pap-modal-slot {
        padding: 8px 14px; border: 1.5px solid #dfe6f1; border-radius: 8px;
        background: #fff; color: #34415a;
        font: 600 12px Inter, Arial, sans-serif; cursor: pointer;
        transition: border-color .12s, background .12s, color .12s;
      }
      .pap-modal-slot:hover { border-color: #7ba1f5; background: #f7faff; color: #2059dc; }
      .pap-modal-slot--selected,
      .pap-modal-slot--selected:hover { background: #2059dc; color: #fff; border-color: #2059dc; }

      @media (max-width: 480px) {
        .pap-modal-footer { flex-direction: column-reverse; }
        .pap-modal-btn { width: 100%; }
      }
    `;
    document.head.appendChild(style);
  }

  // ============ Helpers ============
  function normalizarHora(hora) {
    if (!hora) return '00:00';
    return hora.length > 5 ? hora.substring(0, 5) : hora;
  }

  function montarDataHora(horario) {
    return new Date(`${horario.data}T${normalizarHora(horario.horaInicio)}:00`);
  }

  function formatarDataHoraCompleta(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return iso;
    return d.toLocaleDateString('pt-BR', {
      weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'
    }) + ' às ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  function formatarRestante(ms) {
    if (ms <= 0) return 'já passou';
    const dias = Math.floor(ms / MS_DIA);
    const horas = Math.floor((ms % MS_DIA) / MS_HORA);
    const mins = Math.floor((ms % MS_HORA) / MS_MIN);
    if (dias > 0) return `${dias}d ${horas}h ${mins}min`;
    if (horas > 0) return `${horas}h ${mins}min`;
    return `${mins}min`;
  }

  function escHandler(ev) { if (ev.key === 'Escape') fecharModal(); }

  // ============ Modal base ============
  function abrirModal(opts) {
    fecharModal();
    const overlay = document.createElement('div');
    overlay.className = 'pap-modal-overlay';
    overlay.id = 'papAtendimentoModal';
    overlay.innerHTML = `
      <div class="pap-modal" role="dialog" aria-modal="true" aria-labelledby="papModalTitulo">
        <header class="pap-modal-header">
          <h3 id="papModalTitulo">${opts.titulo || ''}</h3>
          <button type="button" class="pap-modal-close" aria-label="Fechar">×</button>
        </header>
        <div class="pap-modal-body">${opts.corpo || ''}</div>
        <footer class="pap-modal-footer">
          <button type="button" class="pap-modal-btn pap-modal-btn--ghost" data-modal-fechar>
            ${opts.labelCancelar || 'Voltar'}
          </button>
          ${opts.labelConfirmar
        ? `<button type="button" class="pap-modal-btn pap-modal-btn--${opts.perigo ? 'danger' : 'primary'}"
                       data-modal-confirmar ${opts.confirmarDesabilitado ? 'disabled' : ''}>${opts.labelConfirmar}</button>`
        : ''}
        </footer>
      </div>`;
    document.body.appendChild(overlay);

    overlay.querySelector('.pap-modal-close').addEventListener('click', fecharModal);
    overlay.querySelector('[data-modal-fechar]').addEventListener('click', fecharModal);
    overlay.addEventListener('click', (ev) => { if (ev.target === overlay) fecharModal(); });
    document.addEventListener('keydown', escHandler);

    const btnConfirmar = overlay.querySelector('[data-modal-confirmar]');
    if (btnConfirmar && opts.onConfirmar) {
      btnConfirmar.addEventListener('click', () => opts.onConfirmar(btnConfirmar, overlay));
    }
    return overlay;
  }

  function fecharModal() {
    const el = document.getElementById('papAtendimentoModal');
    if (el) el.remove();
    document.removeEventListener('keydown', escHandler);
  }

  async function extrairMensagem(resposta, padrao) {
    try {
      const corpo = await resposta.json();
      return corpo && corpo.message ? corpo.message : padrao;
    } catch (_) { return padrao; }
  }

  // ============ Cancelar ============
  async function cancelarAtendimento(id) {
    let atendimento = null;
    try {
      const r = await fetch(`/atendimentos/${id}`, { credentials: 'include' });
      if (r.ok) atendimento = await r.json();
    } catch (_) { /* segue sem dados */ }

    let blocoInfo = '';
    if (atendimento && atendimento.dataHoraInicio) {
      const restanteMs = new Date(atendimento.dataHoraInicio).getTime() - Date.now();
      const vaiVirarFalta = restanteMs < PRAZO_MS;
      blocoInfo = `
        <div class="pap-modal-info">
          <div class="pap-modal-info-row">
            <span class="pap-modal-info-label">Agendado para</span>
            <span class="pap-modal-info-value">${formatarDataHoraCompleta(atendimento.dataHoraInicio)}</span>
          </div>
          <div class="pap-modal-info-row">
            <span class="pap-modal-info-label">Faltam</span>
            <span class="pap-modal-info-value">${formatarRestante(restanteMs)}</span>
          </div>
        </div>
        <div class="pap-modal-previsao ${vaiVirarFalta ? 'pap-modal-previsao--alerta' : 'pap-modal-previsao--ok'}">
          ${vaiVirarFalta
          ? 'Como faltam <b>menos de 24h</b>, o cancelamento será registrado como <b>FALTA</b>.'
          : 'Como faltam <b>24h ou mais</b>, o cancelamento será normal (sem falta).'}
        </div>`;
    }

    abrirModal({
      titulo: 'Cancelar atendimento',
      corpo: `
        ${blocoInfo}
        <div class="pap-modal-warning">
          <b>Regra de faltas</b>
          <p>Três faltas no semestre suspendem novos agendamentos até contato com a coordenação do PAP.</p>
        </div>
        <p class="pap-modal-text">Tem certeza de que deseja cancelar este atendimento?</p>`,
      labelCancelar: 'Não, manter',
      labelConfirmar: 'Sim, cancelar',
      perigo: true,
      onConfirmar: async (btn) => {
        btn.disabled = true;
        btn.textContent = 'Cancelando…';
        try {
          const r = await fetch(`/atendimentos/${id}/cancelar`, {
            method: 'PATCH',
            credentials: 'include'
          });
          if (!r.ok) throw new Error(await extrairMensagem(r, 'Não foi possível cancelar.'));
          const atualizado = await r.json();
          fecharModal();
          const virouFalta = atualizado.status === 'FALTA';
          const quando = atualizado.dataHoraInicio
              ? formatarDataHoraCompleta(atualizado.dataHoraInicio)
              : '';
          window.alert(virouFalta
              ? `Cancelamento registrado como FALTA.\n\nO atendimento era para ${quando}.`
              : `Atendimento cancelado com sucesso.\n\nO atendimento era para ${quando}.`);
          window.location.reload();
        } catch (e) {
          window.alert(e.message || 'Erro ao cancelar.');
          btn.disabled = false;
          btn.textContent = 'Sim, cancelar';
        }
      }
    });
  }

  // ============ Remarcar ============
  async function remarcarAtendimento(id, profissionalId) {
    let atendimentoAtual = null;
    try {
      const r = await fetch(`/atendimentos/${id}`, { credentials: 'include' });
      if (r.ok) atendimentoAtual = await r.json();
    } catch (_) {}

    const blocoAtual = atendimentoAtual && atendimentoAtual.dataHoraInicio
        ? `<div class="pap-modal-info">
           <div class="pap-modal-info-row">
             <span class="pap-modal-info-label">Atendimento atual</span>
             <span class="pap-modal-info-value">${formatarDataHoraCompleta(atendimentoAtual.dataHoraInicio)}</span>
           </div>
           <div class="pap-modal-info-row">
             <span class="pap-modal-info-label">Remarcar para</span>
             <span class="pap-modal-info-value" id="papRemarcarEscolhido">—</span>
           </div>
         </div>`
        : '';

    const overlay = abrirModal({
      titulo: 'Remarcar atendimento',
      corpo: `
        <div class="pap-modal-warning">
          <b>Prazo de remarcação</b>
          <p>Só é possível remarcar com <b>24 horas ou mais</b> de antecedência do horário atual.</p>
        </div>
        ${blocoAtual}
        <p class="pap-modal-text">Escolha um novo horário disponível:</p>
        <div id="papRemarcarConteudo" class="pap-modal-loading">Carregando horários disponíveis…</div>`,
      labelCancelar: 'Cancelar',
      labelConfirmar: 'Confirmar remarcação',
      confirmarDesabilitado: true,
      onConfirmar: async (btn) => {
        const horarioId = overlay.dataset.horarioSelecionado;
        if (!horarioId) return;
        btn.disabled = true;
        btn.textContent = 'Remarcando…';
        try {
          const r = await fetch(`/atendimentos/${id}/remarcar`, {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ horarioId: Number(horarioId) })
          });
          if (!r.ok) throw new Error(await extrairMensagem(r, 'Não foi possível remarcar.'));
          const atualizado = await r.json();
          fecharModal();
          window.alert(`Atendimento remarcado para ${formatarDataHoraCompleta(atualizado.dataHoraInicio)}.`);
          window.location.reload();
        } catch (e) {
          window.alert(e.message || 'Erro ao remarcar.');
          btn.disabled = false;
          btn.textContent = 'Confirmar remarcação';
        }
      }
    });

    const conteudo = overlay.querySelector('#papRemarcarConteudo');
    const escolhido = overlay.querySelector('#papRemarcarEscolhido');
    const btnConfirmar = overlay.querySelector('[data-modal-confirmar]');

    try {
      const r = await fetch(`/horarios/profissional/${profissionalId}/disponiveis`, {
        credentials: 'include'
      });
      if (!r.ok) throw new Error(await extrairMensagem(r, 'Falha ao carregar horários.'));
      const horarios = await r.json();

      const agora = Date.now();
      const filtrados = horarios
          .filter((h) => {
            const t = montarDataHora(h).getTime();
            if (t < agora + PRAZO_MS) return false;
            if (atendimentoAtual && h.id === atendimentoAtual.horarioId) return false;
            return true;
          })
          .sort((a, b) => montarDataHora(a) - montarDataHora(b));

      if (!filtrados.length) {
        conteudo.className = 'pap-modal-empty';
        conteudo.innerHTML = `<b>Nenhum horário disponível</b><p>Não há horários com pelo menos 24h de antecedência. Tente novamente mais tarde.</p>`;
        return;
      }

      const grupos = {};
      filtrados.forEach((h) => {
        if (!grupos[h.data]) grupos[h.data] = [];
        grupos[h.data].push(h);
      });
      const dias = Object.keys(grupos).sort();

      conteudo.className = 'pap-modal-horarios';
      conteudo.innerHTML = dias.map((dia) => {
        const dataObj = new Date(dia + 'T00:00:00');
        const dataFmt = dataObj.toLocaleDateString('pt-BR', {
          weekday: 'short', day: '2-digit', month: 'long'
        });
        const horariosDoDia = grupos[dia].sort((a, b) =>
            normalizarHora(a.horaInicio).localeCompare(normalizarHora(b.horaInicio))
        );
        return `
          <div class="pap-modal-grupo-dia">
            <div class="pap-modal-dia-header">
              <b>${dataFmt}</b>
              <span>${horariosDoDia.length} horário${horariosDoDia.length > 1 ? 's' : ''}</span>
            </div>
            <div class="pap-modal-dia-slots">
              ${horariosDoDia.map((h) => `
                <button type="button" class="pap-modal-slot" data-horario-id="${h.id}"
                        data-inicio="${normalizarHora(h.horaInicio)}" data-data="${h.data}">
                  ${normalizarHora(h.horaInicio)} – ${normalizarHora(h.horaFim)}
                </button>
              `).join('')}
            </div>
          </div>`;
      }).join('');

      conteudo.addEventListener('click', (ev) => {
        const btn = ev.target.closest('[data-horario-id]');
        if (!btn) return;
        conteudo.querySelectorAll('.pap-modal-slot').forEach((b) => b.classList.remove('pap-modal-slot--selected'));
        btn.classList.add('pap-modal-slot--selected');
        overlay.dataset.horarioSelecionado = btn.dataset.horarioId;
        if (escolhido) {
          const d = new Date(`${btn.dataset.data}T${btn.dataset.inicio}:00`);
          escolhido.textContent = d.toLocaleDateString('pt-BR', {
            weekday: 'short', day: '2-digit', month: 'short'
          }) + ' às ' + btn.dataset.inicio;
        }
        if (btnConfirmar) btnConfirmar.disabled = false;
      });
    } catch (e) {
      conteudo.className = 'pap-modal-empty';
      conteudo.innerHTML = `<b>Não foi possível carregar</b><p>${e.message || 'Tente novamente.'}</p>`;
    }
  }

  // ============ Delegação global ============
  document.addEventListener('click', (ev) => {
    const btn = ev.target.closest('[data-atendimento-acao]');
    if (!btn) return;
    ev.preventDefault();
    const acao = btn.dataset.atendimentoAcao;
    const id = btn.dataset.atendimentoId;
    const profissionalId = btn.dataset.profissionalId;
    if (!id) return;
    if (acao === 'cancelar') cancelarAtendimento(id);
    else if (acao === 'remarcar') remarcarAtendimento(id, profissionalId);
  });

  garantirEstilos();
})();