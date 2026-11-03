(function () {
  const ROUTES = {
    student: 'aluno-inicial.html',
    professional: 'profissional-inicial.html',
    admin: 'admin-inicial.html'
  };
  const PREVIEW_MODE_KEY = 'PAP_PREVIEW_MODE';
  const PREVIEW_ROLE_KEY = 'PAP_PREVIEW_ROLE';

  const current = location.pathname.split('/').pop() || 'index.html';
  const fileRole = current.startsWith('aluno-') ? 'student'
    : current.startsWith('profissional-') ? 'professional'
      : current.startsWith('admin-') ? 'admin'
        : null;

  function normalizeRole(profile) {
    return ({
      aluno: 'student', student: 'student', estudante: 'student',
      profissional: 'professional', professional: 'professional',
      admin: 'admin', administrador: 'admin', administrator: 'admin'
    })[String(profile || '').toLowerCase()] || null;
  }

  function getBackendRole(identity) {
    if (!identity) return null;
    if (typeof identity === 'string') return normalizeRole(identity);
    const candidates = [
      identity.role, identity.profile, identity.tipo, identity.type,
      identity.user?.role, identity.user?.profile, identity.user?.tipo,
      identity.data?.role, identity.data?.profile, identity.data?.tipo
    ];
    for (const candidate of candidates) {
      const role = normalizeRole(candidate);
      if (role) return role;
    }
    return null;
  }

  function isLocalHost() {
    const hostname = String(location.hostname || '').replace(/^\[|\]$/g, '');
    return ['localhost', '127.0.0.1', '::1'].includes(hostname);
  }

  function isProjectGitHubPages() {
    return location.hostname === 'jpedrotg23.github.io'
      && (location.pathname === '/PAP-Online/' || location.pathname.startsWith('/PAP-Online/'));
  }

  function isPreviewEnvironment() {
    return isLocalHost() || isProjectGitHubPages();
  }

  function isBackendConfigured() {
    return Boolean(window.PAP_CONFIG && String(window.PAP_CONFIG.API_BASE_URL || '').trim());
  }

  function canUsePreview() {
    return isPreviewEnvironment()
      && Boolean(window.PAP_CONFIG && window.PAP_CONFIG.ENABLE_PREVIEW === true)
      && !isBackendConfigured();
  }

  function getPreviewRole() {
    try {
      if (!canUsePreview() || sessionStorage.getItem(PREVIEW_MODE_KEY) !== 'true') return null;
      return normalizeRole(sessionStorage.getItem(PREVIEW_ROLE_KEY));
    } catch (_) {
      return null;
    }
  }

  function clearPreview() {
    try {
      sessionStorage.removeItem(PREVIEW_MODE_KEY);
      sessionStorage.removeItem(PREVIEW_ROLE_KEY);
    } catch (_) {}
  }

  function startPreview(role) {
    if (!canUsePreview()) return false;
    const normalizedRole = normalizeRole(role);
    if (!normalizedRole) return false;
    try {
      sessionStorage.setItem(PREVIEW_MODE_KEY, 'true');
      sessionStorage.setItem(PREVIEW_ROLE_KEY, normalizedRole);
    } catch (_) {
      return false;
    }
    location.href = ROUTES[normalizedRole];
    return true;
  }

  function redirectForProfile(identity) {
    const role = getBackendRole(identity);
    if (role && ROUTES[role]) location.href = ROUTES[role];
    return role;
  }

  function showNotice(message, isError = true) {
    const notice = document.getElementById('loginNotice');
    if (!notice) return;
    notice.textContent = message;
    notice.className = isError ? 'notice notice-error' : 'notice notice-success';
  }

  function showPreviewBanner() {
    if (!getPreviewRole() || document.getElementById('pap-preview-banner')) return;
    const banner = document.createElement('div');
    banner.id = 'pap-preview-banner';
    banner.textContent = 'MODO DE DEMONSTRAÇÃO — BACKEND NÃO CONECTADO';
    banner.setAttribute('role', 'status');
    banner.style.cssText = 'position:fixed;z-index:9999;left:50%;bottom:12px;transform:translateX(-50%);max-width:calc(100% - 24px);padding:7px 12px;border:1px solid #d7a94a;border-radius:999px;background:#fff8df;color:#765a16;font-size:10px;font-weight:700;text-align:center;box-shadow:0 4px 12px rgba(0,0,0,.08)';
    document.body.appendChild(banner);
  }

  async function checkSession() {
    const previewRole = getPreviewRole();
    if (!canUsePreview()) clearPreview();

    if (previewRole) {
      showPreviewBanner();
      if (!fileRole) location.href = ROUTES[previewRole];
      else if (previewRole !== fileRole) location.href = ROUTES[previewRole];
      return;
    }

    if (!fileRole || !window.PAPApi || !isBackendConfigured()) return;

    try {
      const identity = await window.PAPApi.auth.session();
      const role = getBackendRole(identity);
      if (!role) throw Object.assign(new Error('Sessão sem perfil autorizado'), { status: 401 });
      if (role !== fileRole) location.href = ROUTES[role];
    } catch (_) {
      location.href = 'index.html';
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-role-logout], .portal-exit, .exit-link').forEach(link => {
      link.addEventListener('click', async event => {
        event.preventDefault();

        if (getPreviewRole()) {
          clearPreview();
          location.href = 'index.html';
          return;
        }

        try {
          await window.PAPApi.auth.logout();
        } catch (_) {}
        location.href = 'index.html';
      });
    });
    checkSession();
  });

  window.PAPAuth = {
    ROUTES,
    normalizeRole,
    getBackendRole,
    redirectForProfile,
    showNotice,
    isLocalHost,
    isBackendConfigured,
    canUsePreview,
    getPreviewRole,
    clearPreview,
    startPreview
  };
})();
