import http from 'k6/http';
import { check, sleep, group } from 'k6';

// ─────────────────────────────────────────────────────────────────────────────
// Teste de capacidade / ponto de ruptura — k6
//
// Objetivo: descobrir quantos usuários simultâneos o sistema aguenta mantendo
// tempo de resposta p(95) <= P95_MS (padrão 2000ms), navegando de forma
// autenticada e realista.
//
// Uso: roda em um nível FIXO de VUs por execução (ver loadtest/CAPACIDADE.md
// para a metodologia de busca binária/exponencial entre execuções):
//   k6 run -e VUS=100 -e P95_MS=1000 -e BASE_URL=http://localhost:8080 loadtest/capacidade.js
// ─────────────────────────────────────────────────────────────────────────────

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = Number(__ENV.VUS || 50);
const P95_MS = Number(__ENV.P95_MS || 2000);
const ADMIN_USER = __ENV.ADMIN_USER || 'admin';
const ADMIN_PASS = __ENV.ADMIN_PASS || 'admin123';

export const options = {
  scenarios: {
    navegacao: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: VUS },  // sobe rápido pro nível alvo
        { duration: '25s', target: VUS },  // sustenta o nível alvo
        { duration: '5s', target: 0 },     // desaquece
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: [`p(95)<${P95_MS}`],   // meta deste teste, parametrizável
  },
};

let autenticado = false;
let sessionIdSalvo = null;

function extrairValor(html, nomeCampo) {
  const regexValue = new RegExp(`name="${nomeCampo}"\\s+value="([^"]+)"`);
  const regexContent = new RegExp(`name="${nomeCampo}"\\s+content="([^"]+)"`);
  const m = html.match(regexValue) || html.match(regexContent);
  return m ? m[1] : null;
}

function jsessionid() {
  const cookies = http.cookieJar().cookiesForURL(BASE + '/');
  return cookies['JSESSIONID'] ? cookies['JSESSIONID'][0] : null;
}

function fazerLogin() {
  const paginaLogin = http.get(`${BASE}/login`);
  const csrfToken = extrairValor(paginaLogin.body, '_csrf');
  const resp = http.post(
    `${BASE}/login`,
    { username: ADMIN_USER, password: ADMIN_PASS, _csrf: csrfToken },
    { redirects: 0 }
  );
  check(resp, { 'login: redireciona (302/303)': (r) => r.status === 302 || r.status === 303 });
  sessionIdSalvo = jsessionid();
  autenticado = true;
}

// O k6 reseta o cookie jar a cada iteração; restauramos a sessão manualmente
// para simular um mesmo usuário navegando por várias páginas (ver carga.js).
function garantirSessao() {
  if (!autenticado) {
    group('login', fazerLogin);
  } else if (sessionIdSalvo) {
    http.cookieJar().set(BASE, 'JSESSIONID', sessionIdSalvo);
  }
}

export default function () {
  garantirSessao();

  const naoFoiPraLogin = (r) => !r.url.includes('/login');

  group('navegacao autenticada', () => {
    const home = http.get(`${BASE}/`);
    check(home, { 'home: 200 e autenticado': (r) => r.status === 200 && naoFoiPraLogin(r) });

    const listaAtivos = http.get(`${BASE}/ativos?pagina=0`);
    check(listaAtivos, { 'lista ativos: 200 e autenticado': (r) => r.status === 200 && naoFoiPraLogin(r) });

    const buscaAtivos = http.get(`${BASE}/ativos?busca=note&pagina=0`);
    check(buscaAtivos, { 'busca ativos: 200 e autenticado': (r) => r.status === 200 && naoFoiPraLogin(r) });

    const listaChamados = http.get(`${BASE}/chamados?pagina=0`);
    check(listaChamados, { 'lista chamados: 200 e autenticado': (r) => r.status === 200 && naoFoiPraLogin(r) });
  });

  sleep(1);
}
