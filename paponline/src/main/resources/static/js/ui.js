(function () {
  'use strict';

  if (window.__PAP_UI_INITIALIZED__) return;
  window.__PAP_UI_INITIALIZED__ = true;

  const STORAGE_PREFIX = 'pap-appearance';
  const LEGACY_STORAGE_KEY = 'pap-appearance';
  const palettes = {
    default: { label: 'Padrão', protanopia: false, deuteranopia: false, tritanopia: false },
    protanopia: { label: 'Protanopia', protanopia: true, deuteranopia: false, tritanopia: false },
    deuteranopia: { label: 'Deuteranopia', protanopia: false, deuteranopia: true, tritanopia: false },
    tritanopia: { label: 'Tritanopia', protanopia: false, deuteranopia: false, tritanopia: true }
  };

  function getRole() {
    if (location.pathname.includes('/aluno/')) return 'aluno';
    if (location.pathname.includes('/profissional/')) return 'profissional';
    if (location.pathname.includes('/admin/')) return 'admin';
    try {
      return sessionStorage.getItem('PAP_PREVIEW_ROLE') || 'public';
    } catch (_) {
      return 'public';
    }
  }

  let preferenceIdentity = null;
  let preferenceScope = getRole();

  function storageKey() {
    return `${STORAGE_PREFIX}:${preferenceScope}:${preferenceIdentity || 'tipo-acesso'}`;
  }

  function readPreferences() {
    try {
      const scoped = JSON.parse(localStorage.getItem(storageKey())) || null;
      if (scoped) return scoped;
      return JSON.parse(localStorage.getItem(LEGACY_STORAGE_KEY)) || {};
    } catch (_) {
      return {};
    }
  }

  function savePreferences(preferences) {
    try {
      localStorage.setItem(storageKey(), JSON.stringify(preferences));
    } catch (_) {}
  }

  function applyPreferences(preferences) {
    const root = document.documentElement;
    root.dataset.theme = preferences.theme === 'dark' ? 'dark' : 'light';
    root.dataset.palette = palettes[preferences.palette] ? preferences.palette : 'default';
  }

  applyPreferences(readPreferences());

  const icons = {
    '⌂': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z"/></svg>',
    '✉': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 5h18v14H3zM3 6l9 7 9-7"/></svg>',
    '↪': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 7V4l8 8-8 8v-3H4v-10zM20 5v14"/></svg>',
    '⚙': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm8 4 2 1-2 3-2-1a8 8 0 0 1-2 1v2h-4v-2a8 8 0 0 1-2-1l-2 1-2-3 2-1a8 8 0 0 1 0-2L4 9l2-3 2 1a8 8 0 0 1 2-1V4h4v2a8 8 0 0 1 2 1l2-1 2 3-2 1a8 8 0 0 1 0 2Z"/></svg>',
    '♙': '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3"/><path d="M5 20c.5-4 3-6 7-6s6.5 2 7 6"/></svg>',
    '▦': '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18"/></svg>',
    '▣': '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18"/></svg>',
    '▤': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 3h9l4 4v14H6zM15 3v5h5M9 12h7M9 16h7"/></svg>',
    '▢': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 4h16v12H8l-4 4z"/></svg>',
    '♢': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 16H6l1.5-2V10a4.5 4.5 0 0 1 9 0v4zM10 19h4M12 3v2"/></svg>'
  };

  function updatePasswordButton(button, input) {
    const visible = input.type === 'text';
    button.setAttribute('aria-label', visible ? 'Ocultar senha' : 'Mostrar senha');
    button.setAttribute('aria-pressed', String(visible));
    button.innerHTML = visible
      ? '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 5 12 5s9.5 7 9.5 7-3.5 7-9.5 7-9.5-7-9.5-7Zm9.5-3a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z"/></svg>'
      : '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.9 5.2A10.7 10.7 0 0 0 3 12s3 6 9 6a9.7 9.7 0 0 0 4.1-.9M14.1 5.2A10.7 10.7 0 0 1 21 12s-3 6-9 6"/></svg>';
  }

  function createAppearanceControls() {
    const container = document.querySelector('[data-appearance-section]');
    if (!container) return;
    let wrapper = container.querySelector('.appearance-controls');
    if (!wrapper) {
      wrapper = document.createElement('div');
      wrapper.className = 'appearance-controls';
      wrapper.setAttribute('role', 'group');
      wrapper.setAttribute('aria-label', 'Preferências de aparência');
      wrapper.innerHTML =
        '<button type="button" class="appearance-theme" aria-label="Alternar modo noturno"></button>' +
        '<label for="appearance-palette">Paleta de cores</label>' +
        '<select id="appearance-palette" class="appearance-palette" aria-label="Paleta de cores">' +
        Object.entries(palettes).map(([value, item]) => `<option value="${value}">${item.label}</option>`).join('') +
        '</select>';
      container.append(wrapper);
    }
    return wrapper;
  }

  function refreshAppearanceControls(wrapper) {
    if (!wrapper) return;
    const preferences = readPreferences();
    const themeButton = wrapper.querySelector('.appearance-theme');
    const paletteSelect = wrapper.querySelector('.appearance-palette');
    applyPreferences(preferences);
    themeButton.textContent = document.documentElement.dataset.theme === 'dark' ? 'Modo claro' : 'Modo noturno';
    themeButton.setAttribute('aria-pressed', String(document.documentElement.dataset.theme === 'dark'));
    paletteSelect.value = document.documentElement.dataset.palette;
  }

  function bindAppearanceListeners(wrapper) {
    if (!wrapper) return;
    const themeButton = wrapper.querySelector('.appearance-theme');
    const paletteSelect = wrapper.querySelector('.appearance-palette');
    if (wrapper.dataset.listenersBound === 'true') return;
    wrapper.dataset.listenersBound = 'true';

    themeButton.addEventListener('click', () => {
      const current = readPreferences();
      current.theme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
      savePreferences(current);
      refreshAppearanceControls(wrapper);
    });
    paletteSelect.addEventListener('change', () => {
      const current = readPreferences();
      current.palette = paletteSelect.value;
      savePreferences(current);
      refreshAppearanceControls(wrapper);
    });
  }

  function setupAppearance() {
    const wrapper = createAppearanceControls();
    if (!wrapper) return;
    bindAppearanceListeners(wrapper);
    refreshAppearanceControls(wrapper);
    return wrapper;
  }

  function initialize() {
    document.querySelectorAll('[data-toggle]').forEach(button => {
      const input = document.getElementById(button.dataset.toggle);
      if (!input) return;
      updatePasswordButton(button, input);
      button.addEventListener('click', () => {
        input.type = input.type === 'password' ? 'text' : 'password';
        updatePasswordButton(button, input);
      });
    });
    const navIcons = {
      home: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z"/></svg>',
      calendar: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18"/></svg>',
      document: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 3h9l4 4v14H6zM15 3v5h5M9 12h7M9 16h7"/></svg>',
      message: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 4h16v12H8l-4 4z"/></svg>',
      history: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>',
      user: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3"/><path d="M5 20c.5-4 3-6 7-6s6.5 2 7 6"/></svg>',
      logout: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 4H4v16h6M14 8l4 4-4 4M18 12H8"/></svg>',
      bell: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 16H6l1.5-2V10a4.5 4.5 0 0 1 9 0v4zM10 19h4M12 3v2"/></svg>',
      settings: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm8 4 2 1-2 3-2-1a8 8 0 0 1-2 1v2h-4v-2a8 8 0 0 1-2-1l-2 1-2-3 2-1a8 8 0 0 1 0-2L4 9l2-3 2 1a8 8 0 0 1 2-1V4h4v2a8 8 0 0 1 2 1l2-1 2 3-2 1a8 8 0 0 1 0 2Z"/></svg>'
    };

    document.querySelectorAll('.portal-nav-link, .side-link, .portal-exit, .exit-link').forEach(link => {
      const explicitIcon = link.querySelector('[data-icon]');
      let type = explicitIcon?.dataset.icon;
      if (!type) {
        const href = String(link.getAttribute('href') || '').toLowerCase();
        const label = String(link.textContent || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
        if (link.matches('.portal-exit, .exit-link') || label.includes('sair')) type = 'logout';
        else if (href.includes('inicial')) type = 'home';
        else if (href.includes('agenda') || href.includes('agendar')) type = 'calendar';
        else if (href.includes('adapt') || href.includes('solicit')) type = 'document';
        else if (href.includes('mensagem')) type = 'message';
        else if (href.includes('histor')) type = 'history';
        else if (href.includes('perfil')) type = 'user';
        else if (href.includes('relatorio')) type = 'history';
        else if (href.includes('profissionais') || href.includes('alunos') || href.includes('notific')) type = 'document';
      }
      const iconElement = explicitIcon || link.querySelector('.portal-nav-icon, .nav-ico');
      if (iconElement && type && navIcons[type]) {
        iconElement.innerHTML = navIcons[type];
        iconElement.setAttribute('aria-hidden', 'true');
      }
    });
    document.querySelectorAll('.portal-notification, .bell').forEach(button => {
      const badge = button.querySelector('i');
      button.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 16H6l1.5-2V10a4.5 4.5 0 0 1 9 0v4zM10 19h4M12 3v2"/></svg>';
      if (badge) button.appendChild(badge);
    });
    setupAppearance();
    resolveIdentity();
  }

  async function resolveIdentity() {
    if (window.PAPApi && window.PAPApi.auth) {
      try {
        const identity = await window.PAPApi.auth.session();
        const stableId = identity?.id;
        if (stableId) preferenceIdentity = String(stableId);
      } catch (_) {}
    }
    applyPreferences(readPreferences());
    const wrapper = document.querySelector('[data-appearance-section] .appearance-controls');
    refreshAppearanceControls(wrapper);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
  else initialize();

  window.PAPUI = { applyPreferences, updatePasswordButton };
})();
