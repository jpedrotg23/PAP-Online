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
    return ['localhost', '127.0.0.1', '::1'].includes(location.hostname);
  }

  function isBackendConfigured() {
    return Boolean(window.PAP_CONFIG && window.PAP_CONFIG.API_BASE_URL);
  }

  function getPreviewRole() {
    try {
      if (!isLocalHost() || isBackendConfigured() || sessionStorage.getItem(PREVIEW_MODE_KEY) !== 'true') return null;
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
    if (!isLocalHost() || isBackendConfigured()) return false;
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

  async function checkSession() {
    if (!isLocalHost() || isBackendConfigured()) clearPreview();
    const previewRole = getPreviewRole();

    if (previewRole) {
      if (!fileRole) location.href = ROUTES[previewRole];
      else if (previewRole !== fileRole) location.href = ROUTES[previewRole];
      return;
    }

    if (!fileRole || !window.PAPApi) return;

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
    getPreviewRole,
    clearPreview,
    startPreview
  };
})();
