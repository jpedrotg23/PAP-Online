const assert = require('assert');
const filtro = require('./profissionais-filter.js');
const fs = require('fs');
const path = require('path');

const apenasPsicologia = {
  id: 1,
  ativo: true,
  cargo: 'Psicóloga clínica'
};
const apenasPsicopedagogia = {
  id: 2,
  ativo: true,
  cargo: 'Psicopedagoga escolar'
};
const ambas = {
  id: 3,
  ativo: true,
  cargo: 'Psicóloga e Psicopedagoga'
};
const psicologiaMasculina = {
  id: 4,
  ativo: true,
  cargo: 'PSICOLOGO'
};
const inativo = {
  id: 5,
  ativo: false,
  cargo: 'Psicóloga'
};

assert.deepStrictEqual(
  filtro.filtrarProfissionais([apenasPsicologia, apenasPsicopedagogia, ambas, psicologiaMasculina, inativo], 'Psicologia')
    .map(p => p.id),
  [1, 3, 4]
);
assert.deepStrictEqual(
  filtro.filtrarProfissionais([apenasPsicologia, apenasPsicopedagogia, ambas, psicologiaMasculina, inativo], 'Psicopedagogia')
    .map(p => p.id),
  [2, 3]
);
assert.strictEqual(
  filtro.profissionalAtendeEspecialidade({ ativo: false, cargo: 'Psicóloga' }, 'Psicologia'),
  false
);

const htmlPath = path.join(__dirname, '..', 'aluno', 'aluno-agendar.html');
const html = fs.readFileSync(htmlPath, 'utf8');
assert.match(html, /fetch\('\/areas', \{ credentials: 'include' \}\)/);
assert.match(html, /id="areaCards"/);
assert.match(html, /data-area-id="\${a\.id}"/);
assert.match(html, /async function selecionarArea\(areaId\)/);
assert.match(html, /state\.areaSelecionada = state\.areas\.find\(a => a\.id === areaId\)/);
assert.match(html, /await carregarProfissionais\(\)/);
assert.match(html, /async function selecionarProfissional\(profId\)/);
assert.match(html, /await carregarHorarios\(\)/);
assert.match(html, /async function confirmarAgendamento\(\)/);
assert.doesNotMatch(html, /areas-atendimento/);

console.log('Filtro de profissionais: testes locais aprovados.');
