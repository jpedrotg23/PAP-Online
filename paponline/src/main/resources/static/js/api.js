(function () {
  const baseUrl = (window.PAP_CONFIG && window.PAP_CONFIG.API_BASE_URL || '').replace(/\/$/, '');

  async function request(path, options = {}) {
    if (!baseUrl) throw new Error('BACKEND_NOT_CONFIGURED');
    const response = await fetch(baseUrl + path, { credentials: 'include', headers: { Accept: 'application/json', ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }), ...(options.headers || {}) }, ...options });
    if (!response.ok) {
      let message = 'Não foi possível concluir a operação.';
      try { const body = await response.json(); message = body.message || message; } catch (_) {}
      const error = new Error(message);
      error.status = response.status;
      throw error;
    }
    if (response.status === 204) return null;
    return response.json();
  }

  window.PAPApi = {
    request,
    auth: {
      login: (credentials) => request('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
      session: () => request('/auth/session'),
      logout: () => request('/auth/logout', { method: 'POST', body: '{}' })
    },
    users: { me: () => request('/me'), update: (data) => request('/me', { method: 'PATCH', body: JSON.stringify(data) }) },
    appointments: { list: () => request('/appointments'), create: (data) => request('/appointments', { method: 'POST', body: JSON.stringify(data) }) },
    adaptations: { list: () => request('/adaptations'), create: (data) => request('/adaptations', { method: 'POST', body: JSON.stringify(data) }) },
    messages: { list: () => request('/messages'), send: (data) => request('/messages', { method: 'POST', body: JSON.stringify(data) }) },
    notifications: { list: () => request('/notifications') },
    reports: { list: () => request('/reports') },
    admin: { professionals: () => request('/admin/professionals'), students: () => request('/admin/students') }
  };
})();
