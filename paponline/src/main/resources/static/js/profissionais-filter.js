(function (root) {
  'use strict';

  function normalizarTexto(valor) {
    return String(valor || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/\s+/g, ' ')
      .trim()
      .toUpperCase();
  }

  function especialidadesProfissional(profissional) {
    const cargo = normalizarTexto(profissional && profissional.cargo);
    const especialidades = [];

    if (/(^|[^A-Z])PSICOPEDAGOG(I|IA|ICO|ICA|ICOS|ICAS|O|A)?/.test(cargo)
      || cargo.includes('PSICOPEDAGOG')) especialidades.push('PSICOPEDAGOGIA');
    if (/(^|[^A-Z])PSICOLOG(IA|O|A|OS|AS)?/.test(cargo)
      || cargo.includes('PSICOLOG')) especialidades.push('PSICOLOGIA');

    return especialidades;
  }

  function profissionalAtendeEspecialidade(profissional, especialidade) {
    if (profissional && profissional.ativo === false) return false;

    const especialidadeNormalizada = normalizarTexto(especialidade);
    if (especialidadeNormalizada !== 'PSICOLOGIA'
      && especialidadeNormalizada !== 'PSICOPEDAGOGIA') return false;

    return especialidadesProfissional(profissional).includes(especialidadeNormalizada);
  }

  function filtrarProfissionais(profissionais, especialidade) {
    if (!Array.isArray(profissionais)) return [];
    return profissionais.filter(profissional =>
      profissionalAtendeEspecialidade(profissional, especialidade));
  }

  const api = {
    normalizarTexto,
    especialidadesProfissional,
    profissionalAtendeEspecialidade,
    filtrarProfissionais
  };

  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  root.PAPProfissionaisFiltro = api;
})(typeof window !== 'undefined' ? window : globalThis);
