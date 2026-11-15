# Employee Management API

Une petite API REST écrite en **Java avec Spring Boot**. Elle permet de créer un compte, de se connecter, puis de gérer une liste d'employés. Les données sont enregistrées dans une base **MongoDB**.

## Ce que fait l'application

- **Créer un compte** avec un email et un mot de passe. Le mot de passe n'est jamais enregistré en clair : seule une empreinte impossible à inverser est gardée.
- **Se connecter** pour recevoir un jeton (un « token » JWT). Ce jeton prouve qui vous êtes pendant 1 jour.
- **Gérer des employés** : en ajouter, les lister, les filtrer par département, en lire un, en supprimer un.
- **Protéger les employés** : il faut être connecté pour y accéder, et seul celui qui a créé un employé peut le supprimer.

## Ce qu'il faut installer

Une seule chose : [Docker Desktop](https://www.docker.com/products/docker-desktop/). Il n'y a pas besoin d'installer Java ni MongoDB : Docker s'en occupe.

## Démarrer l'application

**1. Créer le fichier de configuration.** Copiez `.env.example` et nommez la copie `.production.env` :

```bash
cp .env.example .production.env
```

Ouvrez ensuite `.production.env` et remplacez la valeur de `JWT_SECRET` par un texte secret, long d'au moins 32 caractères. Ce fichier reste sur votre machine : il n'est jamais envoyé sur GitHub.

**2. Lancer l'application.** Docker Desktop doit être ouvert.

```bash
docker compose up --build -d
```

Le premier lancement prend quelques minutes. Il construit l'application, lance les tests, puis démarre l'API et la base de données.

**3. Vérifier que ça marche.** Ouvrez <http://localhost:8080/> dans votre navigateur. La page doit afficher `OK`.

### Commandes utiles

| Pour… | Commande |
|---|---|
| Voir ce que fait l'application (les logs) | `docker compose logs -f api` |
| Arrêter l'application (les données sont gardées) | `docker compose down` |
| Arrêter et effacer aussi les données | `docker compose down -v` |
| Relancer après avoir modifié le code | `docker compose up --build -d` |

## Utiliser l'API

L'API répond sur `http://localhost:8080`. Elle reçoit et renvoie du JSON.

### Les routes

| Méthode | Route | Rôle | Connexion requise |
|---|---|---|---|
| `GET` | `/` | Vérifie que le serveur répond | Non |
| `POST` | `/auth/register` | Crée un compte | Non |
| `POST` | `/auth/login` | Connecte et renvoie un jeton | Non |
| `GET` | `/employees` | Liste les employés | Oui |
| `GET` | `/employees?department=IT` | Liste les employés d'un département | Oui |
| `GET` | `/employees/{id}` | Renvoie un employé | Oui |
| `POST` | `/employees` | Crée un employé | Oui |
| `DELETE` | `/employees/{id}` | Supprime un employé (créateur seulement) | Oui |

### Exemple complet

**Créer un compte.** Le mot de passe doit contenir au moins 8 caractères, avec une minuscule, une majuscule, un chiffre et un caractère spécial.

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"Passw0rd!","firstName":"Ada","lastName":"Lovelace"}'
```

**Se connecter.** La réponse contient le jeton : `{"token":"eyJ..."}`.

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"Passw0rd!"}'
```

**Créer un employé.** Remplacez `VOTRE_JETON` par le jeton reçu à la connexion.

```bash
curl -X POST http://localhost:8080/employees \
  -H "Authorization: Bearer VOTRE_JETON" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","department":"IT","level":"J"}'
```

- `department` vaut `IT`, `Marketing` ou `HR`.
- `level` vaut `J` (junior), `M` (medior) ou `S` (senior).

**Lister les employés.**

```bash
curl http://localhost:8080/employees -H "Authorization: Bearer VOTRE_JETON"
```

### Les codes de réponse

| Code | Signification |
|---|---|
| `200`, `201`, `204` | Tout s'est bien passé |
| `400` | Les données envoyées sont incorrectes. La réponse dit quel champ pose problème |
| `401` | Vous n'êtes pas connecté, ou le jeton est faux ou expiré |
| `403` | Vous essayez de supprimer un employé créé par quelqu'un d'autre |
| `404` | L'employé demandé n'existe pas |

## Tester avec Postman

Le dossier `postman` contient une collection prête à l'emploi, avec 29 requêtes qui couvrent tous les cas (réussites et erreurs).

1. Dans Postman, cliquez sur **Import** et choisissez `postman/employee-api.postman_collection.json`.
2. Cliquez sur **Run collection** pour lancer toutes les requêtes dans l'ordre.

L'ordre est important : la connexion enregistre le jeton, qui sert aux requêtes suivantes. Chaque exécution crée deux comptes de test dans la base.

## Lancer les tests automatiques

```bash
docker compose run --rm integration-tests
```

Cette commande lance deux sortes de tests :

- **30 tests unitaires**, qui vérifient chaque partie du code séparément. Ils sont aussi lancés à chaque construction de l'application.
- **17 tests d'intégration**, qui démarrent l'application complète avec une vraie base MongoDB temporaire. Ils ne touchent pas à vos données.

Pour une vérification rapide après une modification (compilation et tests unitaires seulement, sans base de données) :

```bash
docker compose run --rm verify
```

## Parcourir le projet étape par étape

Le dépôt sert aussi de terrain d'entraînement pour **Claude Code**. Un tag marque chaque étape : le projet nu d'abord, puis la configuration Claude Code ajoutée brique par brique.

```bash
git checkout ep01-start     # le projet nu, aucune configuration Claude Code
```

| Tag | État du dépôt |
|---|---|
| `ep01-start` | Projet nu, **aucun `.claude/`** |
| `ep02-start` | + `CLAUDE.md` |
| `ep04-start` | + `.claude/rules/` |
| `ep05-start` | + le domaine `users` complété |
| `ep06-start` | + `.claude/skills/` |
| `ep07-start` | + `.claude/agents/` |
| `ep08-start` | + `.claude/settings.json` et le hook |
| `ep09-start` | + `.mcp.json` |
| `serie-complete` | État final |

Pour voir ce qu'une étape ajoute :

```bash
git diff ep06-start ep07-start
```

Au départ, le domaine `users` est volontairement incomplet (pas de contrôleur, donc pas de route) alors que `employees` est complet. Le compléter en imitant `employees` est l'exercice de l'étape `ep04-start`.

## La configuration

Les réglages se trouvent dans `.production.env` :

| Variable | Rôle | Valeur par défaut |
|---|---|---|
| `PORT` | Port sur lequel l'API écoute | `8080` |
| `DB_URI` | Adresse de la base MongoDB | `mongodb://127.0.0.1:27017/bd_name` |
| `SALT_ROUNDS` | Force de la protection des mots de passe (plus c'est haut, plus c'est sûr et lent) | `10` |
| `JWT_SECRET` | Secret qui signe les jetons (32 caractères minimum, obligatoire) | aucune |
| `JWT_TTL` | Durée de validité d'un jeton | `1d` (1 jour) |

Avec Docker, `DB_URI` est réglé automatiquement dans `docker-compose.yml` pour pointer vers la base lancée par Docker.

> **Important :** ne partagez jamais votre `JWT_SECRET`. Une personne qui le connaît peut fabriquer de faux jetons et se faire passer pour n'importe quel utilisateur.

## Comment le code est organisé

Le code suit une architecture en trois couches : le **contrôleur** reçoit la requête, le **service** applique les règles, le **repository** parle à la base de données.

```
src/main/java/com/example/employeeapi/
├── authentication/   Inscription, connexion et vérification des jetons
├── users/            Les comptes utilisateurs
├── employees/        Les employés
└── core/             Configuration, sécurité, gestion des erreurs, validation
```

Quand on arrête l'application, elle termine d'abord les requêtes en cours avant de fermer la connexion à la base. C'est ce qu'on appelle un arrêt propre (« graceful shutdown »).

## Technologies utilisées

- Java 21 et Spring Boot 3.5 (Web, Security, Data MongoDB, Validation)
- MongoDB 7
- JWT avec la bibliothèque jjwt
- JUnit 5, Mockito et Testcontainers pour les tests
- Docker et Docker Compose
