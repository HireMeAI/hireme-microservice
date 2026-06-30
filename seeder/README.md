# HireMe — Seeder « plateforme vivante »

Peuple la plateforme avec un jeu de données réaliste et **cohérent entre services** :
recruteurs + offres, candidats + profils + CV (formations, expériences, compétences),
puis candidatures **scorées par l'IA** (ml-engine).

Le seeder pilote l'**API publique via la Gateway** — exactement les mêmes flux que le
frontend. Conséquence : tous les comptes créés sont confirmés (e-mail récupéré depuis
Mailpit) et **utilisables pour se connecter** sur le frontend.

## Pré-requis

La stack doit tourner :

```bash
cd ..              # hireme-microservice/
docker compose up -d
```

Endpoints utilisés : Gateway `:8080`, Mailpit `:8025`. Node ≥ 18 (utilise `fetch` natif).

## Lancement

```bash
cd seeder
node seed.mjs
```

Avec des volumes personnalisés :

```bash
RECRUITERS=8 CANDIDATES=40 node seed.mjs
```

Variables d'environnement disponibles :

| Variable        | Défaut                  | Rôle                                  |
|-----------------|-------------------------|---------------------------------------|
| `RECRUITERS`    | `6`                     | Nombre de recruteurs (3–5 offres / recruteur) |
| `CANDIDATES`    | `20`                    | Nombre de candidats (CV + 3–6 candidatures)   |
| `SEED_PASSWORD` | `Password123!`          | Mot de passe commun à tous les comptes seedés |
| `HIREME_API`    | `http://localhost:8080` | URL de la Gateway                     |
| `MAILPIT_API`   | `http://localhost:8025` | API Mailpit (confirmation d'e-mail)   |

## Comptes générés

Tous avec le mot de passe `SEED_PASSWORD` (`Password123!` par défaut) :

- Recruteurs : `recruteur1@seed.hireme.dev` … `recruteurN@seed.hireme.dev`
- Candidats  : `candidat1@seed.hireme.dev` … `candidatN@seed.hireme.dev`

Les candidats sont répartis sur 5 domaines (Frontend, Backend, Data, DevOps, Mobile) ;
chacun postule surtout à des offres de son domaine (meilleurs scores) et à quelques
offres hors-domaine, ce qui produit une distribution de scores variée et réaliste.

## Idempotence

Le script est re-jouable : un recruteur qui a déjà des offres réutilise les siennes, et
un candidat qui a déjà un CV est ignoré. Relancer ajoute donc surtout les comptes
manquants au lieu de dupliquer les données.

## Réinitialiser

Les données vivent dans les volumes Postgres. Pour repartir de zéro :

```bash
docker compose down -v && docker compose up -d
```
