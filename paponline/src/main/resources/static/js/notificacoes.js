(function () {
    'use strict';

    const POLL_MS = 60000;
    const DEBUG = false; // mude para true se quiser ver logs no console

    function log() {
        if (DEBUG) console.log('[notif]', ...arguments);
    }

    const state = {
        usuarioId: null,
        perfil: null,
        notificacoes: [],
        aberto: false,
        carregando: false,
        inicializado: false
    };

    function escapeHtml(str) {
        return String(str || '').replace(/[&<>"']/g, s => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        })[s]);
    }

    function fmtTempo(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        const agora = new Date();
        const seg = Math.floor((agora - d) / 1000);
        if (seg < 60) return 'agora';
        if (seg < 3600) return Math.floor(seg / 60) + ' min atrás';
        if (seg < 86400) return Math.floor(seg / 3600) + ' h atrás';
        return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
    }

    function urlPorTipo(tipo) {
        const isProf = state.perfil === 'PROFISSIONAL';
        switch (tipo) {
            case 'AGENDAMENTO':
            case 'LEMBRETE_24H':
                return isProf
                    ? '/profissional/profissional-agenda.html'
                    : '/aluno/aluno-historico.html';
            case 'SOLICITACAO':
                return isProf
                    ? '/profissional/profissional-solicitacoes.html'
                    : '/aluno/aluno-adaptacoes.html';
            default:
                return null;
        }
    }

    function garantirPainel() {
        let painel = document.getElementById('papNotifPainel');
        if (painel) return painel;

        painel = document.createElement('div');
        painel.id = 'papNotifPainel';
        painel.style.cssText = [
            'position: fixed',
            'top: 70px',
            'right: 20px',
            'width: 380px',
            'max-width: calc(100vw - 40px)',
            'max-height: 70vh',
            'background: #fff',
            'border: 1px solid #dfe6f1',
            'border-radius: 14px',
            'box-shadow: 0 20px 60px rgba(10,26,63,.25)',
            'z-index: 100000',
            'display: none',
            'flex-direction: column',
            'overflow: hidden',
            'font-family: Inter, Arial, sans-serif'
        ].join(';');
        document.body.appendChild(painel);
        return painel;
    }

    function renderizar() {
        const painel = garantirPainel();
        const naoLidas = state.notificacoes.filter(n => !n.lida).length;

        painel.innerHTML = `
            <div style="padding:14px 18px;border-bottom:1px solid #edf0f5;display:flex;align-items:center;justify-content:space-between;gap:8px">
                <div>
                    <strong style="font-size:14px;color:#0f1b33">Notificações</strong>
                    ${naoLidas > 0
            ? `<span style="margin-left:6px;font-size:11px;color:#64738d">${naoLidas} não lida${naoLidas > 1 ? 's' : ''}</span>`
            : ''}
                </div>
                ${naoLidas > 0
            ? `<button type="button" data-marcar-todas style="border:0;background:none;color:#2059dc;font-size:11px;font-weight:700;cursor:pointer;padding:4px">Marcar todas como lidas</button>`
            : ''}
            </div>
            <div style="flex:1;overflow-y:auto;padding:6px" data-lista></div>
        `;

        const lista = painel.querySelector('[data-lista]');
        if (!state.notificacoes.length) {
            lista.innerHTML = `
                <div style="padding:30px 20px;text-align:center;color:#8290a6;font-size:12px">
                    Nenhuma notificação por aqui.
                </div>`;
        } else {
            lista.innerHTML = state.notificacoes.map(n => `
                <div data-notif-id="${n.id}" style="
                    display:flex;gap:10px;padding:10px 12px;border-radius:8px;cursor:pointer;
                    background:${n.lida ? 'transparent' : '#f0f5ff'};
                    margin-bottom:2px;
                ">
                    <span style="width:8px;height:8px;border-radius:50%;margin-top:6px;flex-shrink:0;background:${n.lida ? 'transparent' : '#2059dc'}"></span>
                    <div style="flex:1;min-width:0">
                        <div style="font-size:12px;font-weight:700;color:#0f1b33;margin-bottom:2px">
                            ${escapeHtml(n.titulo)}
                        </div>
                        <div style="font-size:11px;color:#52617a;line-height:1.4">
                            ${escapeHtml(n.mensagem)}
                        </div>
                        <div style="font-size:10px;color:#94a0b3;margin-top:4px">
                            ${fmtTempo(n.criadaEm)}
                        </div>
                    </div>
                </div>
            `).join('');

            lista.querySelectorAll('[data-notif-id]').forEach(el => {
                el.addEventListener('click', () => clicar(Number(el.dataset.notifId)));
            });
        }

        const btnTodas = painel.querySelector('[data-marcar-todas]');
        if (btnTodas) {
            btnTodas.addEventListener('click', marcarTodasComoLidas);
        }
    }

    function atualizarBadge() {
        const badge = document.getElementById('badgeNotificacoes');
        if (!badge) return;
        const naoLidas = state.notificacoes.filter(n => !n.lida).length;
        if (naoLidas > 0) {
            badge.textContent = naoLidas > 9 ? '9+' : String(naoLidas);
            badge.style.display = '';
        } else {
            badge.style.display = 'none';
        }
    }

    async function carregar() {
        if (!state.usuarioId || state.carregando) return;
        state.carregando = true;
        try {
            const r = await fetch(`/notificacoes/usuario/${state.usuarioId}`, {
                credentials: 'include'
            });
            if (!r.ok) return;
            state.notificacoes = (await r.json()) || [];
            atualizarBadge();
            if (state.aberto) renderizar();
        } catch (_) {
        } finally {
            state.carregando = false;
        }
    }

    async function marcarLida(id) {
        try {
            const r = await fetch(`/notificacoes/${id}/lida`, {
                method: 'PATCH',
                credentials: 'include'
            });
            if (!r.ok) return false;
            const n = state.notificacoes.find(x => x.id === id);
            if (n) {
                n.lida = true;
                n.lidaEm = new Date().toISOString();
            }
            atualizarBadge();
            if (state.aberto) renderizar();
            return true;
        } catch (_) {
            return false;
        }
    }

    async function clicar(id) {
        const n = state.notificacoes.find(x => x.id === id);
        if (!n) return;

        if (!n.lida) await marcarLida(id);

        const destino = urlPorTipo(n.tipo);
        if (destino) {
            fechar();
            window.location.href = destino;
        }
    }

    async function marcarTodasComoLidas() {
        const naoLidas = state.notificacoes.filter(n => !n.lida);
        for (const n of naoLidas) {
            await marcarLida(n.id);
        }
    }

    async function garantirSessao() {
        if (state.usuarioId) return true;
        try {
            const r = await fetch('/auth/session', { credentials: 'include' });
            if (!r.ok) return false;
            const u = await r.json();
            state.usuarioId = u.id;
            state.perfil = u.tipo;
            return true;
        } catch (_) {
            return false;
        }
    }

    function abrir() {
        state.aberto = true;
        const painel = garantirPainel();
        painel.style.display = 'flex';
        renderizar();
        // Carrega dados só quando abre — sessão pode não ter sido buscada ainda
        garantirSessao().then(ok => {
            if (ok) {
                carregar();
            } else {
                log('sessão indisponível ao abrir painel');
                state.notificacoes = [];
                renderizar();
            }
        });
    }

    function fechar() {
        const painel = document.getElementById('papNotifPainel');
        if (painel) painel.style.display = 'none';
        state.aberto = false;
    }

    function toggle(e) {
        if (e) {
            e.preventDefault();
            e.stopPropagation();
        }
        if (state.aberto) fechar();
        else abrir();
    }

    // Delegação: sobrevive a re-renderizações do header (portais.js, auth.js etc.)
    function ligarDelegacao() {
        document.addEventListener('click', (e) => {
            const btn = e.target.closest && e.target.closest('[aria-label="Notificações"], [data-pap-bell]');
            if (!btn) return;
            log('clique no sino via delegação');
            toggle(e);
        }, false);

        document.addEventListener('click', (e) => {
            if (!state.aberto) return;
            const painel = document.getElementById('papNotifPainel');
            if (!painel) return;
            if (painel.contains(e.target)) return;
            const btn = e.target.closest && e.target.closest('[aria-label="Notificações"], [data-pap-bell]');
            if (btn) return;
            fechar();
        }, false);

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') fechar();
        });
    }

    async function inicializar() {
        if (state.inicializado) return;
        state.inicializado = true;

        log('inicializando');

        // Liga o clique IMEDIATAMENTE — sem depender de /auth/session
        ligarDelegacao();

        // Marca o botão visualmente como clicável (opcional)
        document
            .querySelectorAll('[aria-label="Notificações"], [data-pap-bell]')
            .forEach(btn => {
                if (btn.dataset.papBellBound === '1') return;
                btn.dataset.papBellBound = '1';
                btn.style.cursor = 'pointer';
            });

        // Sessão e badge em paralelo, sem bloquear o clique
        const ok = await garantirSessao();
        if (!ok) {
            log('sem sessão — badge e polling inativos');
            return;
        }
        await carregar();
        setInterval(carregar, POLL_MS);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', inicializar);
    } else {
        inicializar();
    }
})();