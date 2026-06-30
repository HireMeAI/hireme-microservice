#!/usr/bin/env node
/**
 * HireMe — Seeder « plateforme vivante ».
 *
 * Pilote l'API publique via la Gateway (les mêmes flux que le frontend) pour créer un
 * jeu de données cohérent et réaliste : recruteurs + offres, candidats + profils + CV,
 * puis candidatures (scorées par l'IA). Les comptes sont confirmés en récupérant le
 * jeton depuis Mailpit, donc tous les comptes seedés sont utilisables pour se connecter.
 *
 * Pré-requis : la stack docker-compose tournante (gateway :8080, Mailpit :8025).
 * Lancement   : node seed.mjs        (ou: RECRUITERS=8 CANDIDATES=30 node seed.mjs)
 *
 * Tous les comptes seedés partagent le mot de passe défini ci-dessous (SEED_PASSWORD).
 */

import { writeFileSync } from 'node:fs';

const BASE = process.env.HIREME_API || 'http://localhost:8080';
const MAILPIT = process.env.MAILPIT_API || 'http://localhost:8025';
const SEED_PASSWORD = process.env.SEED_PASSWORD || 'Password123!';
const N_RECRUITERS = parseInt(process.env.RECRUITERS || '6', 10);
const N_CANDIDATES = parseInt(process.env.CANDIDATES || '20', 10);
// Fichier de vérité terrain pour l'évaluation du modèle ML (cf. ../hireme-ml-engine/evaluate.py).
const GOLDEN_OUT = process.env.GOLDEN_OUT || 'golden_set.csv';

/* ------------------------------------------------------------------ utils */
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const rand = (arr) => arr[Math.floor(Math.random() * arr.length)];
const randInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const sample = (arr, n) => [...arr].sort(() => Math.random() - 0.5).slice(0, n);
const slug = (s) => s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');

let okCount = 0, failCount = 0;

const randomIp = () => `51.${randInt(0, 255)}.${randInt(0, 255)}.${randInt(1, 254)}`;

async function http(method, path, { token, body, ip } = {}) {
  const headers = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  // La Gateway limite /api/auth/login à 5 essais/min par IP (anti-brute-force).
  // Un seeder local légitime envoie une IP synthétique unique pour ne pas se bloquer.
  if (ip) headers['X-Forwarded-For'] = ip;
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    // /api/auth/confirm répond par une redirection 302 vers le frontend (:5173).
    // 'manual' évite que fetch la suive (le frontend n'est pas forcément démarré).
    redirect: 'manual',
  });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  return { ok: res.ok, status: res.status, data };
}

/* --------------------------------------------------------------- mailpit */
async function fetchConfirmToken(email) {
  // Laisse le temps à l'e-mail d'arriver.
  for (let i = 0; i < 10; i++) {
    const res = await fetch(`${MAILPIT}/api/v1/search?query=${encodeURIComponent('to:' + email)}`);
    if (res.ok) {
      const msgs = (await res.json()).messages || [];
      if (msgs.length) {
        const full = await (await fetch(`${MAILPIT}/api/v1/message/${msgs[0].ID}`)).json();
        const txt = (full.HTML || '') + ' ' + (full.Text || '');
        const m = txt.match(/token=([A-Za-z0-9\-_.]+)/);
        if (m) return m[1];
      }
    }
    await sleep(400);
  }
  return null;
}

/** Inscrit (si besoin), confirme l'e-mail via Mailpit, puis se connecte. */
async function createUser({ firstName, lastName, email, recruiter }) {
  const endpoint = recruiter ? '/api/auth/recruiter/register' : '/api/auth/register';
  const reg = await http('POST', endpoint, { body: { firstName, lastName, email, password: SEED_PASSWORD } });
  // 409/400 = compte déjà présent (re-run) → on continue vers confirm/login.
  const alreadyExists = reg.status === 409 || reg.status === 400;
  if (!reg.ok && !alreadyExists) {
    throw new Error(`register échoué ${email}: ${reg.status} ${JSON.stringify(reg.data)}`);
  }

  // Confirme (idempotent : sans effet si déjà vérifié).
  const token = await fetchConfirmToken(email);
  if (token) await http('GET', `/api/auth/confirm?token=${encodeURIComponent(token)}`);

  const login = await http('POST', '/api/auth/login', { ip: randomIp(), body: { email, password: SEED_PASSWORD } });
  if (!login.ok || !login.data?.accessToken) {
    throw new Error(`login échoué ${email}: ${login.status} ${JSON.stringify(login.data)}`);
  }
  return { token: login.data.accessToken, user: login.data.user, email };
}

/* ------------------------------------------------------------ data pools */
const FIRST = ['Lucas', 'Emma', 'Hugo', 'Léa', 'Adam', 'Chloé', 'Nathan', 'Manon', 'Gabriel', 'Camille',
  'Louis', 'Sarah', 'Jules', 'Inès', 'Raphaël', 'Jade', 'Arthur', 'Louise', 'Maël', 'Alice',
  'Yanis', 'Nina', 'Théo', 'Lina', 'Ethan', 'Zoé', 'Noah', 'Eva', 'Sami', 'Maya'];
const LAST = ['Martin', 'Bernard', 'Dubois', 'Thomas', 'Robert', 'Petit', 'Durand', 'Leroy', 'Moreau', 'Simon',
  'Laurent', 'Lefebvre', 'Michel', 'Garcia', 'David', 'Bertrand', 'Roux', 'Vincent', 'Fournier', 'Morel',
  'Girard', 'André', 'Mercier', 'Blanc', 'Guerin', 'Boyer', 'Garnier', 'Chevalier', 'Francois', 'Legrand'];
const CITIES = ['Paris', 'Lyon', 'Marseille', 'Toulouse', 'Bordeaux', 'Lille', 'Nantes', 'Nice', 'Rennes', 'Strasbourg'];
const COMPANIES = ['Datalix', 'Novacode', 'Cloudwave', 'Pixelis', 'Quantora', 'Finovo', 'Greenstack', 'Mobiq',
  'Healthia', 'Retailo', 'Aerospike Labs', 'Brightside', 'Cortex AI', 'Nimbus', 'Voltaic'];
const CONTRACTS = ['FULL_TIME', 'PART_TIME', 'FIXED_TERM', 'FREELANCE', 'INTERNSHIP'];
const REMOTE = ['ON_SITE', 'REMOTE', 'HYBRID'];
const AVAIL = ['IMMEDIATE', 'OPEN_TO_TALK', 'NOT_AVAILABLE'];

const DOMAINS = {
  Frontend: {
    title: 'Développeur Frontend',
    skills: ['React', 'TypeScript', 'JavaScript', 'CSS', 'Next.js', 'Redux', 'Vue.js', 'HTML', 'Tailwind'],
    jobs: ['Développeur Frontend React', 'Intégrateur Web', 'Lead Frontend', 'Développeur Vue.js'],
  },
  Backend: {
    title: 'Développeur Backend',
    skills: ['Java', 'Spring Boot', 'Node.js', 'PostgreSQL', 'Kafka', 'Docker', 'REST', 'Microservices'],
    jobs: ['Développeur Backend Java', 'Ingénieur Logiciel', 'Architecte Microservices', 'Développeur Node.js'],
  },
  Data: {
    title: 'Data Scientist',
    skills: ['Python', 'SQL', 'Pandas', 'TensorFlow', 'Spark', 'Machine Learning', 'scikit-learn'],
    jobs: ['Data Scientist', 'Data Engineer', 'Ingénieur Machine Learning', 'Analyste Data'],
  },
  DevOps: {
    title: 'Ingénieur DevOps',
    skills: ['Docker', 'Kubernetes', 'AWS', 'Terraform', 'CI/CD', 'Linux', 'Ansible'],
    jobs: ['Ingénieur DevOps', 'SRE', 'Cloud Engineer', 'Platform Engineer'],
  },
  Mobile: {
    title: 'Développeur Mobile',
    skills: ['Kotlin', 'Swift', 'Flutter', 'React Native', 'Android', 'iOS'],
    jobs: ['Développeur Mobile Flutter', 'Développeur Android', 'Développeur iOS', 'Lead Mobile'],
  },
};
const DOMAIN_KEYS = Object.keys(DOMAINS);

// Déduit le domaine d'une offre existante à partir de ses compétences/titre (domaines
// à compétences disjointes). Évite d'attribuer un domaine aléatoire aux offres réutilisées,
// ce qui fausserait la vérité terrain (golden_set.csv). Fallback : 1er domaine.
function inferDomain(job) {
  const hay = `${job.title || ''} ${(job.requiredSkills || []).join(' ')}`.toLowerCase();
  let best = DOMAIN_KEYS[0], bestScore = -1;
  for (const key of DOMAIN_KEYS) {
    const score = DOMAINS[key].skills.filter((s) => hay.includes(s.toLowerCase())).length;
    if (score > bestScore) { bestScore = score; best = key; }
  }
  return best;
}

const DEGREES = ['Master Informatique', 'Diplôme d\'Ingénieur', 'Licence Pro Développement', 'Master Data Science', 'BUT Informatique'];
const SCHOOLS = ['Université Paris-Saclay', 'EPITECH', 'INSA Lyon', 'École 42', 'CentraleSupélec', 'IMT Atlantique'];

/* ------------------------------------------------------------ seed steps */
async function ensureSkills(token, titles) {
  // Récupère le pool global puis crée les manquants. Retourne map title -> id.
  const list = await http('GET', '/api/skills', { token });
  const map = {};
  (Array.isArray(list.data) ? list.data : []).forEach((s) => { if (s.title) map[s.title] = s.id; });
  for (const t of titles) {
    if (map[t]) continue;
    const c = await http('POST', '/api/skills', { token, body: { title: t } });
    if (c.ok && c.data?.id) map[t] = c.data.id;
  }
  return map;
}

async function seedRecruitersAndJobs() {
  const jobs = [];
  for (let i = 0; i < N_RECRUITERS; i++) {
    const firstName = rand(FIRST), lastName = rand(LAST);
    const email = `recruteur${i + 1}@seed.hireme.dev`;
    let rec;
    try { rec = await createUser({ firstName, lastName, email, recruiter: true }); }
    catch (e) { console.log(`  ⚠️  recruteur ${email}: ${e.message}`); failCount++; continue; }

    // Idempotence : si ce recruteur a déjà des offres (run précédent), on les réutilise.
    const existing = await http('GET', `/api/jobs/recruiter/${rec.user.id}`, { token: rec.token });
    if (Array.isArray(existing.data) && existing.data.length > 0) {
      existing.data.forEach((j) => jobs.push({ ...j, domain: inferDomain(j) }));
      console.log(`  ↺ recruteur ${email} → ${existing.data.length} offres existantes réutilisées`);
      continue;
    }

    const company = rand(COMPANIES);
    const nJobs = randInt(3, 5);
    for (let j = 0; j < nJobs; j++) {
      const domain = rand(DOMAIN_KEYS);
      const d = DOMAINS[domain];
      const title = rand(d.jobs);
      const skills = sample(d.skills, randInt(3, 5));
      const min = randInt(32, 55) * 1000;
      const body = {
        recruiterId: rec.user.id,
        title,
        description: `${company} recrute un(e) ${title}. Stack : ${skills.join(', ')}. Rejoignez une équipe ${domain} ambitieuse sur des projets à fort impact.`,
        company,
        location: rand(CITIES),
        salaryMin: min,
        salaryMax: min + randInt(8, 20) * 1000,
        contractType: rand(CONTRACTS),
        remotePolicy: rand(REMOTE),
        status: 'OPEN',
        requiredSkills: skills,
      };
      const r = await http('POST', '/api/jobs', { token: rec.token, body });
      if (r.ok && r.data?.id) { jobs.push({ ...r.data, domain }); okCount++; }
      else { failCount++; }
    }
    console.log(`  ✅ recruteur ${email} (${company}) → ${nJobs} offres`);
  }
  return jobs;
}

async function seedCandidatesAndApplications(jobs, skillMap) {
  const samples = [];
  const golden = []; // { domain, text } de chaque CV créé, pour la vérité terrain.
  for (let i = 0; i < N_CANDIDATES; i++) {
    const firstName = rand(FIRST), lastName = rand(LAST);
    const email = `candidat${i + 1}@seed.hireme.dev`;
    let cand;
    try { cand = await createUser({ firstName, lastName, email }); }
    catch (e) { console.log(`  ⚠️  candidat ${email}: ${e.message}`); failCount++; continue; }

    const token = cand.token;
    const userId = cand.user.id;

    // Idempotence : candidat déjà seedé (a un CV) → on saute.
    const existingCv = await http('GET', `/api/resumes?userId=${userId}`, { token });
    if (Array.isArray(existingCv.data) && existingCv.data.length > 0) {
      console.log(`  ↺ candidat ${email} déjà seedé, ignoré`);
      if (samples.length < 3) samples.push(email);
      continue;
    }

    const domain = DOMAIN_KEYS[i % DOMAIN_KEYS.length];
    const d = DOMAINS[domain];
    const mySkills = sample(d.skills, randInt(4, 6));

    // Profil candidat
    await http('POST', '/api/candidate/updateProfile', {
      token,
      body: {
        firstName, lastName,
        bio: `${d.title} passionné(e), ${randInt(2, 9)} ans d'expérience. Spécialiste ${mySkills.slice(0, 3).join(', ')}.`,
        desiredJobTitle: d.title,
        availability: rand(AVAIL),
        openToRelocate: Math.random() > 0.5,
        autoApplyEnabled: Math.random() > 0.7,
        contractPreferences: sample(CONTRACTS, randInt(1, 2)),
      },
    });

    // Contact
    const contact = await http('POST', '/api/contacts', {
      token,
      body: {
        phone: `06${randInt(10000000, 99999999)}`,
        email,
        city: rand(CITIES),
        postalCode: `${randInt(10, 95)}000`,
        linkedin: `https://linkedin.com/in/${slug(firstName + '-' + lastName)}`,
      },
    });
    const contactId = contact.ok ? contact.data?.id : undefined;

    // CV
    const resumeTitle = `CV ${d.title}`;
    const summary = `${d.title} avec une solide maîtrise de ${mySkills.join(', ')}. À la recherche de défis techniques stimulants.`;
    const resume = await http('POST', '/api/resumes', {
      token,
      body: {
        userId,
        contactId,
        title: resumeTitle,
        portfolioSlug: `${slug(firstName + '-' + lastName)}-${i + 1}`,
        summary,
        visibility: 'PUBLIC',
      },
    });
    if (!resume.ok || !resume.data?.id) { console.log(`  ⚠️  CV ${email}: ${resume.status}`); failCount++; continue; }
    const resumeId = resume.data.id;
    okCount++;

    // Formation
    const gradYear = randInt(2014, 2023);
    await http('POST', `/api/resumes/${resumeId}/educations`, {
      token,
      body: {
        degree: rand(DEGREES),
        institution: rand(SCHOOLS),
        startDate: `${gradYear - 2}-09-01`,
        endDate: `${gradYear}-06-30`,
        description: `Spécialisation ${domain}.`,
      },
    });

    // Expériences
    const expTitles = [];
    const nExp = randInt(1, 3);
    let yearCursor = 2024;
    for (let e = 0; e < nExp; e++) {
      const pos = `${d.title}${e === 0 ? ' Senior' : ''}`;
      expTitles.push(pos);
      const start = yearCursor - randInt(1, 3);
      await http('POST', `/api/resumes/${resumeId}/experiences`, {
        token,
        body: {
          position: pos,
          company: rand(COMPANIES),
          description: `Développement et maintenance de solutions ${domain} (${sample(mySkills, 3).join(', ')}).`,
          startDate: `${start}-01-01`,
          endDate: e === 0 ? null : `${yearCursor}-12-31`,
        },
      });
      yearCursor = start;
    }

    // Compétences (liées au CV)
    for (const s of mySkills) {
      const sid = skillMap[s];
      if (sid) await http('POST', `/api/resumes/${resumeId}/skills/${sid}`, { token });
    }

    // Candidatures : surtout dans le domaine du candidat (bons scores) + quelques hors-domaine.
    const inDomain = jobs.filter((j) => j.domain === domain);
    const offDomain = jobs.filter((j) => j.domain !== domain);
    const targets = [...sample(inDomain, Math.min(randInt(2, 4), inDomain.length)),
                     ...sample(offDomain, Math.min(randInt(1, 2), offDomain.length))];
    const resumeText = `${resumeTitle}. ${summary}. Compétences: ${mySkills.join(', ')}. Expériences: ${expTitles.join(', ')}.`;
    let applied = 0;
    for (const job of targets) {
      const jobText = `${job.title}. ${job.description}. Compétences requises: ${(job.requiredSkills || []).join(', ')}.`;
      const a = await http('POST', '/api/matching/applications', {
        token,
        body: {
          candidateId: userId,
          resumeId,
          jobOfferId: job.id,
          resumeText,
          jobText,
          knownPii: [firstName, lastName, email],
          source: 'SEED',
        },
      });
      if (a.ok) { applied++; okCount++; } else { failCount++; }
    }
    golden.push({ domain, text: resumeText });
    console.log(`  ✅ candidat ${email} (${domain}) → CV + ${applied} candidatures`);
    if (samples.length < 3) samples.push(email);
  }
  return { samples, golden };
}

/**
 * Écrit golden_set.csv : chaque CV créé est croisé avec CHAQUE offre.
 * pertinent = 1 si même domaine, sinon 0. C'est la vérité terrain consommée par
 * evaluate.py (--from-csv) pour mesurer le modèle sur les textes réels de la pipeline.
 */
function writeGoldenSet(goldenCandidates, jobs) {
  if (goldenCandidates.length === 0) {
    console.log('  ⚠️  Aucun CV neuf créé (DB déjà seedée ?) — golden_set non écrit.');
    return;
  }
  const cell = (s) => `"${String(s ?? '').replace(/"/g, '""')}"`;
  const rows = ['cv_domain,job_domain,relevant,cv_text,job_text'];
  for (const c of goldenCandidates) {
    for (const job of jobs) {
      const jobText = `${job.title}. ${job.description}. Compétences requises: ${(job.requiredSkills || []).join(', ')}.`;
      const relevant = job.domain === c.domain ? 1 : 0;
      rows.push([cell(c.domain), cell(job.domain), relevant, cell(c.text), cell(jobText)].join(','));
    }
  }
  writeFileSync(GOLDEN_OUT, rows.join('\n') + '\n', 'utf8');
  console.log(`  📄 ${GOLDEN_OUT} écrit : ${goldenCandidates.length} CV × ${jobs.length} offres = ${rows.length - 1} couples étiquetés.`);
}

/* ----------------------------------------------------------------- main */
(async () => {
  console.log(`\n🌱 HireMe seeder → ${BASE}`);
  console.log(`   ${N_RECRUITERS} recruteurs · ${N_CANDIDATES} candidats · mot de passe commun: ${SEED_PASSWORD}\n`);

  // Sanity check
  const ping = await fetch(`${BASE}/actuator/gateway/routes`).then((r) => r.ok).catch(() => false);
  if (!ping) { console.error(`❌ Gateway injoignable sur ${BASE}. La stack docker-compose est-elle démarrée ?`); process.exit(1); }

  const allSkills = [...new Set(Object.values(DOMAINS).flatMap((d) => d.skills))];

  console.log('👔 Recruteurs & offres…');
  const jobs = await seedRecruitersAndJobs();
  if (jobs.length === 0) { console.error('❌ Aucune offre créée, arrêt.'); process.exit(1); }

  // Un token quelconque pour gérer le pool de skills global.
  const seederAcc = await createUser({ firstName: 'Skill', lastName: 'Seeder', email: 'skillpool@seed.hireme.dev' });
  console.log('\n🏷️  Pool de compétences…');
  const skillMap = await ensureSkills(seederAcc.token, allSkills);
  console.log(`   ${Object.keys(skillMap).length} compétences disponibles`);

  console.log('\n👤 Candidats, CV & candidatures…');
  const { samples, golden } = await seedCandidatesAndApplications(jobs, skillMap);

  console.log('\n🎯 Vérité terrain (évaluation ML)…');
  writeGoldenSet(golden, jobs);

  console.log(`\n✨ Terminé. ${okCount} entités créées, ${failCount} échecs.`);
  console.log(`   Offres ouvertes : ${jobs.length}`);
  console.log('\n🔑 Connexion (frontend) — exemples :');
  console.log(`   Recruteur : recruteur1@seed.hireme.dev / ${SEED_PASSWORD}`);
  samples.forEach((e) => console.log(`   Candidat  : ${e} / ${SEED_PASSWORD}`));
  console.log('');
})();
