document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-toggle]').forEach(button => button.addEventListener('click', () => {
    const input = document.getElementById(button.dataset.toggle);
    if (input) input.type = input.type === 'password' ? 'text' : 'password';
  }));

  const filterButtons = document.querySelectorAll('[data-filter]');
  filterButtons.forEach(button => button.addEventListener('click', () => {
    filterButtons.forEach(item => item.classList.remove('active'));
    button.classList.add('active');
    document.querySelectorAll('.request-card').forEach(card => {
      card.hidden = button.dataset.filter !== 'all' && card.dataset.status !== button.dataset.filter;
    });
  }));

  document.querySelectorAll('[data-decision]').forEach(button => button.addEventListener('click', () => {
    const card = button.closest('.request-card');
    if (card) {
      let feedback = card.querySelector('[role="status"]');
      if (!feedback) {
        feedback = document.createElement('p');
        feedback.setAttribute('role', 'status');
        card.append(feedback);
      }
      feedback.textContent = 'Esta decisão depende da integração com o backend. Nenhuma alteração foi salva.';
    }
  }));

  const profile = document.getElementById('profileForm');
  if (profile) profile.addEventListener('submit', event => {
    event.preventDefault();
    const notice = profile.querySelector('[role="status"]');
    if (notice) notice.textContent = 'As alterações não foram enviadas; a persistência depende da integração com o backend.';
  });

  document.querySelectorAll('[data-demo-form]').forEach(form => form.addEventListener('submit', event => {
    event.preventDefault();
    let notice = form.querySelector('[role="status"]');
    if (!notice) {
      notice = document.createElement('p');
      notice.setAttribute('role', 'status');
      form.append(notice);
    }
    notice.textContent = 'Os dados não foram enviados. Esta operação depende da integração com o backend.';
  }));

  document.querySelectorAll('[data-pending-backend]').forEach(button => button.addEventListener('click', event => {
    event.preventDefault();
    let notice = button.closest('form')?.querySelector('[role="status"]') || document.querySelector('.pro-action-feedback');
    if (!notice) {
      notice = document.createElement('p');
      notice.className = 'pro-action-feedback';
      notice.setAttribute('role', 'status');
      const content = document.querySelector('.pro-content') || document.querySelector('.pro-main');
      if (content) content.prepend(notice);
      else button.insertAdjacentElement('afterend', notice);
    }
    notice.textContent = button.dataset.pendingBackend || 'Esta operação depende da integração com o backend.';
  }));
});
