(function () {
    'use strict';

    // Mostrar/ocultar senha (mesmo comportamento do app.js)
    document.querySelectorAll('[data-toggle]').forEach(button => {
        button.addEventListener('click', () => {
            const input = document.getElementById(button.dataset.toggle);
            if (input) input.type = input.type === 'password' ? 'text' : 'password';
        });
    });


    // 1 — Solicitar código

    const formRecuperar = document.getElementById('recoverForm');
    if (formRecuperar) {
        formRecuperar.addEventListener('submit', async (e) => {
            e.preventDefault();
            const identificador = document.getElementById('recoverContact').value.trim();
            const notice = document.getElementById('recoverNotice');

            if (!identificador) {
                notice.textContent = 'Informe seu e-mail ou telefone.';
                notice.className = 'notice notice-error';
                return;
            }

            const btn = formRecuperar.querySelector('button[type="submit"]');
            const textoOriginal = btn.textContent;
            btn.disabled = true;
            btn.textContent = 'Enviando…';

            try {
                const r = await fetch('/auth/recuperar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ identifier: identificador })
                });

                if (!r.ok) {
                    const erro = await r.json().catch(() => ({}));
                    throw new Error(erro.message || 'Não foi possível enviar o código.');
                }

                const data = await r.json();
                sessionStorage.setItem('recuperar_identificador', identificador);

                notice.textContent = data.codigoDemo
                    ? `Código enviado! (Demo: ${data.codigoDemo})`
                    : 'Código enviado para seu e-mail/telefone.';
                notice.className = 'notice notice-success';

                setTimeout(() => { window.location.href = '/recuperar/recuperar-2.html'; }, 1800);
            } catch (err) {
                notice.textContent = err.message || 'Erro ao enviar o código.';
                notice.className = 'notice notice-error';
            } finally {
                btn.disabled = false;
                btn.textContent = textoOriginal;
            }
        });
    }

    // 2 — Validar código de 6 dígitos

    const formCodigo = document.getElementById('codeForm');
    if (formCodigo) {
        const inputs = [...document.querySelectorAll('.code-inputs input')];

        // Se não tiver identificador salvo, volta ao passo 1
        if (!sessionStorage.getItem('recuperar_identificador')) {
            window.location.href = '/recuperar/recuperar-1.html';
            return;
        }

        // Comportamento dos inputs: avança, volta, cola
        inputs.forEach((input, i) => {
            input.addEventListener('input', () => {
                input.value = input.value.replace(/\D/g, '').slice(0, 1);
                if (input.value && inputs[i + 1]) inputs[i + 1].focus();
            });
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && !input.value && inputs[i - 1]) {
                    inputs[i - 1].focus();
                }
            });
            input.addEventListener('paste', (e) => {
                e.preventDefault();
                const texto = (e.clipboardData || window.clipboardData).getData('text');
                const digitos = texto.replace(/\D/g, '').slice(0, 6).split('');
                digitos.forEach((d, idx) => { if (inputs[idx]) inputs[idx].value = d; });
                if (digitos.length && inputs[digitos.length - 1]) {
                    inputs[digitos.length - 1].focus();
                }
            });
        });

        // Reenviar código
        const resend = document.getElementById('resendCode');
        if (resend) {
            resend.addEventListener('click', async (e) => {
                e.preventDefault();
                const identificador = sessionStorage.getItem('recuperar_identificador');
                const notice = document.getElementById('codeNotice');
                try {
                    const r = await fetch('/auth/recuperar', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ identifier: identificador })
                    });
                    const data = await r.json();
                    notice.textContent = data.codigoDemo
                        ? `Novo código! (Demo: ${data.codigoDemo})`
                        : 'Novo código enviado.';
                    notice.className = 'notice notice-success';
                } catch (err) {
                    notice.textContent = err.message || 'Erro ao reenviar.';
                    notice.className = 'notice notice-error';
                }
            });
        }

        formCodigo.addEventListener('submit', async (e) => {
            e.preventDefault();
            const identificador = sessionStorage.getItem('recuperar_identificador');
            const codigo = inputs.map(i => i.value).join('');
            const notice = document.getElementById('codeNotice');

            if (codigo.length !== 6) {
                notice.textContent = 'Digite os 6 dígitos do código.';
                notice.className = 'notice notice-error';
                return;
            }

            const btn = formCodigo.querySelector('button[type="submit"]');
            const textoOriginal = btn.textContent;
            btn.disabled = true;
            btn.textContent = 'Verificando…';

            try {
                const r = await fetch('/auth/validar-codigo', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ identifier: identificador, codigo })
                });

                if (!r.ok) {
                    const erro = await r.json().catch(() => ({}));
                    throw new Error(erro.message || 'Código inválido.');
                }

                sessionStorage.setItem('recuperar_codigo', codigo);
                notice.textContent = 'Código validado!';
                notice.className = 'notice notice-success';

                setTimeout(() => { window.location.href = '/recuperar/recuperar-3.html'; }, 900);
            } catch (err) {
                notice.textContent = err.message || 'Erro ao validar código.';
                notice.className = 'notice notice-error';
            } finally {
                btn.disabled = false;
                btn.textContent = textoOriginal;
            }
        });
    }

    // 3 - Redefinir senha

    const formReset = document.getElementById('resetForm');
    if (formReset) {
        const identificador = sessionStorage.getItem('recuperar_identificador');
        const codigo = sessionStorage.getItem('recuperar_codigo');

        // Se faltar contexto, volta ao início
        if (!identificador || !codigo) {
            window.location.href = '/recuperar/recuperar-1.html';
            return;
        }

        formReset.addEventListener('submit', async (e) => {
            e.preventDefault();
            const novaSenha = document.getElementById('resetPassword').value;
            const confirmar = document.getElementById('resetConfirm').value;
            const notice = document.getElementById('resetNotice');

            if (novaSenha.length < 8 || !/[A-Z]/.test(novaSenha) || !/[a-z]/.test(novaSenha) || !/\d/.test(novaSenha)) {
                notice.textContent = 'Senha fraca: mínimo 8 caracteres, com maiúscula, minúscula e número.';
                notice.className = 'notice notice-error';
                return;
            }
            if (novaSenha !== confirmar) {
                notice.textContent = 'As senhas não coincidem.';
                notice.className = 'notice notice-error';
                return;
            }

            const btn = formReset.querySelector('button[type="submit"]');
            const textoOriginal = btn.textContent;
            btn.disabled = true;
            btn.textContent = 'Redefinindo…';

            try {
                const r = await fetch('/auth/redefinir-senha', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        identifier: identificador,
                        codigo,
                        novaSenha
                    })
                });

                if (!r.ok) {
                    const erro = await r.json().catch(() => ({}));
                    throw new Error(erro.message || 'Erro ao redefinir senha.');
                }

                sessionStorage.removeItem('recuperar_identificador');
                sessionStorage.removeItem('recuperar_codigo');

                notice.textContent = 'Senha redefinida com sucesso! Redirecionando para o login…';
                notice.className = 'notice notice-success';

                setTimeout(() => { window.location.href = '/index.html'; }, 2000);
            } catch (err) {
                notice.textContent = err.message || 'Erro ao redefinir senha.';
                notice.className = 'notice notice-error';
            } finally {
                btn.disabled = false;
                btn.textContent = textoOriginal;
            }
        });
    }
})();