(function () {
  document.addEventListener('DOMContentLoaded', () => {
    function getFeedback(button) {
      const form = button.closest('form');
      let feedback = form?.querySelector('[role="status"]') || document.querySelector('.portal-action-feedback');
      if (!feedback) {
        feedback = document.createElement('p');
        feedback.className = 'portal-info-box portal-action-feedback';
        feedback.setAttribute('role', 'status');
        const content = document.querySelector('.portal-content');
        if (content) content.prepend(feedback);
        else button.insertAdjacentElement('afterend', feedback);
      }
      return feedback;
    }

    document.querySelectorAll('[data-search]').forEach(input => input.addEventListener('input', () => {
      const term = input.value.trim().toLocaleLowerCase('pt-BR');
      document.querySelectorAll(input.dataset.search).forEach(item => {
        item.hidden = !item.textContent.toLocaleLowerCase('pt-BR').includes(term);
      });
    }));

    document.querySelectorAll('[data-filter-select]').forEach(select => select.addEventListener('change', () => {
      const target = select.closest('.portal-panel');
      if (!target) return;
      target.querySelectorAll('[data-status]').forEach(item => {
        item.hidden = select.value !== 'all' && item.dataset.status !== select.value;
      });
    }));

    document.querySelectorAll('[data-demo], [data-pending-backend]').forEach(button => button.addEventListener('click', event => {
      event.preventDefault();
      const feedback = getFeedback(button);
      feedback.textContent = button.dataset.pendingBackend || button.dataset.demo || 'Esta operação depende da integração com o backend.';
    }));

    document.querySelectorAll('[data-demo-form]').forEach(form => form.addEventListener('submit', event => {
      event.preventDefault();
      let feedback = form.querySelector('[role="status"]');
      if (!feedback) {
        feedback = document.createElement('p');
        feedback.className = 'portal-form-feedback';
        feedback.setAttribute('role', 'status');
        form.append(feedback);
      }
      feedback.textContent = 'Os dados não foram enviados. Esta operação depende da integração com o backend.';
    }));

    document.querySelectorAll('[data-profile-edit]').forEach(button => button.addEventListener('click', () => {
      const form = button.closest('form');
      if (!form) return;
      const fields = form.querySelectorAll('[data-profile-field]');
      const editing = button.dataset.editing === 'true';
      fields.forEach(field => field.readOnly = editing);
      button.dataset.editing = editing ? 'false' : 'true';
      button.textContent = editing ? 'Editar' : 'Salvar';
      if (editing) {
        const feedback = form.querySelector('.portal-form-feedback');
        if (feedback) feedback.textContent = 'Alterações não foram enviadas; a persistência depende do backend.';
      }
    }));

    document.querySelectorAll('[data-profile-cancel]').forEach(button => button.addEventListener('click', () => {
      const form = button.closest('form');
      if (!form) return;
      form.querySelectorAll('[data-profile-field]').forEach(field => field.readOnly = true);
      const edit = form.querySelector('[data-profile-edit]');
      if (edit) {
        edit.dataset.editing = 'false';
        edit.textContent = 'Editar';
      }
      const feedback = form.querySelector('.portal-form-feedback');
      if (feedback) feedback.textContent = 'Edição cancelada.';
    }));
  });
})();
