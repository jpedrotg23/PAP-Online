(function () {
  'use strict';

  const STORAGE_KEY = 'pap-appearance';
  const palettes = {
    default: { label: 'Padrão', protanopia: false, deuteranopia: false, tritanopia: false },
    protanopia: { label: 'Protanopia', protanopia: true, deuteranopia: false, tritanopia: false },
    deuteranopia: { label: 'Deuteranopia', protanopia: false, deuteranopia: true, tritanopia: false },
    tritanopia: { label: 'Tritanopia', protanopia: false, deuteranopia: false, tritanopia: true }
  };

  function readPreferences() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
    } catch (_) {
      return {};
    }
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
    '⚙': '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm8 4 2 1-2 3-2-1a8 8 0 0 1-2 1v2h-4v-2a8 8 0 0 1-2-1l-2 1-2-3 2-1a8 8 0 0 1 0-2L4 9l2-3 2 1a8 8 0 0 1 2-1V4h4v2a8 8 0 0 1 2 1l2-1 2 3-2 1a8 8 0 0 1 0 2Z"/></svg>'
  };

  function updatePasswordButton(button, input) {
    const visible = input.type === 'text';
    button.setAttribute('aria-label', visible ? 'Ocultar senha' : 'Mostrar senha');
    button.setAttribute('aria-pressed', String(visible));
    button.innerHTML = visible
      ? '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.9 5.2A10.7 10.7 0 0 0 3 12s3 6 9 6a9.7 9.7 0 0 0 4.1-.9M14.1 5.2A10.7 10.7 0 0 1 21 12s-3 6-9 6"/></svg>'
      : '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 5 12 5s9.5 7 9.5 7-3.5 7-9.5 7-9.5-7-9.5-7Zm9.5-3a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z"/></svg>';
  }

  function setupAppearance() {
    const wrapper = document.createElement('div');
    wrapper.className = 'appearance-controls';
    wrapper.setAttribute('aria-label', 'Preferências de aparência');
    wrapper.innerHTML = '<button type="button" class="appearance-theme" aria-label="Alternar modo noturno"></button>' +
      '<label>Paleta <select class="appearance-palette" aria-label="Paleta de cores">' +
      Object.entries(palettes).map(([value, item]) => `<option value="${value}">${item.label}</option>`).join('') +
      '</select></label>';
    document.body.append(wrapper);
    const preferences = readPreferences();
    const themeButton = wrapper.querySelector('.appearance-theme');
    const paletteSelect = wrapper.querySelector('.appearance-palette');
    paletteSelect.value = palettes[preferences.palette] ? preferences.palette : 'default';
    const refresh = () => {
      const current = readPreferences();
      applyPreferences(current);
      themeButton.textContent = document.documentElement.dataset.theme === 'dark' ? 'Modo claro' : 'Modo noturno';
      themeButton.setAttribute('aria-pressed', String(document.documentElement.dataset.theme === 'dark'));
      paletteSelect.value = document.documentElement.dataset.palette;
    };
    themeButton.addEventListener('click', () => {
      const current = readPreferences();
      current.theme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
      localStorage.setItem(STORAGE_KEY, JSON.stringify(current));
      refresh();
    });
    paletteSelect.addEventListener('change', () => {
      const current = readPreferences();
      current.palette = paletteSelect.value;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(current));
      refresh();
    });
    refresh();
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
    document.querySelectorAll('.portal-nav-icon, .nav-ico').forEach(element => {
      const icon = icons[element.textContent.trim()];
      if (icon) element.innerHTML = icon;
      element.setAttribute('aria-hidden', 'true');
    });
    setupAppearance();
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
  else initialize();

  window.PAPUI = { applyPreferences, updatePasswordButton };
})();
