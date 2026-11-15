---
description: >
  Crée un nouveau domaine métier complet en architecture 3-tiers (document, repository, service,
  contrôleur, corps de requête et tests), en suivant le modèle du domaine de référence du projet.
  À utiliser quand l'utilisateur demande d'ajouter une nouvelle entité ou un nouveau domaine à
  l'API. Passer le nom du domaine au singulier et en minuscules, par exemple "project" ou "invoice".
argument-hint: [nom-du-domaine-au-singulier]
---

## Configuration du projet

!`cat .claude/spring-3tiers.json 2>/dev/null || echo "CONFIGURATION ABSENTE"`

Si la ligne ci-dessus affiche `CONFIGURATION ABSENTE`, arrête-toi et demande à l'utilisateur de
créer `.claude/spring-3tiers.json` à partir de l'exemple livré avec le plugin :
`${CLAUDE_PLUGIN_ROOT}/config/spring-3tiers.example.json`.

Sinon, le **domaine de référence** est le dossier indiqué par `referenceDomain`, et
`verifyCommand` est la commande qui compile et lance les tests.

## La tâche

Crée le domaine `$0` dans un package `$0s`, voisin du domaine de référence, en suivant
**exactement** son modèle.

Si `$0` est vide, demande à l'utilisateur le nom du domaine et arrête-toi là.

## Ce qu'il existe déjà — à lire avant d'écrire

Liste le dossier du domaine de référence et lis chacun de ses fichiers, ainsi que ses tests sous
`src/test/java/` : ce sont les modèles à imiter, fichier par fichier.

## Fichiers à produire

Dans le package `$0s`, où `Xxx` est `$0` avec une majuscule :

| Fichier | Contenu |
|---|---|
| `Xxx.java` | Le document MongoDB (`@Document`), identifiant `String id` exposé en JSON sous `_id` |
| `XxxRepository.java` | Une interface qui étend `MongoRepository<Xxx, String>`, sans implémentation |
| `CreateXxxRequest.java` | Un `record` avec ses règles de validation et une méthode `empty()` |
| `XxxService.java` | La logique métier — **reçoit le repository par son constructeur**, ne connaît pas HTTP |
| `XxxController.java` | Les routes — reçoit le service et `RequestValidator` par son constructeur |

Dans le même package, sous `src/test/java/` :

| Fichier | Contenu |
|---|---|
| `XxxServiceTest.java` | Mockito, repository mocké |
| `XxxControllerTest.java` | MockMvc en `standaloneSetup`, service mocké |

Puis, si le domaine doit être protégé, ajoute `"/$0s"` à `JwtAuthFilter.PROTECTED_PREFIXES`.

## Contraintes

- **Injection par constructeur.** Aucune couche n'instancie ses dépendances, aucun `@Autowired` sur
  un champ.
- Une couche ne parle qu'à celle immédiatement en dessous.
- Aucune dépendance de test à ajouter : JUnit 5, Mockito et AssertJ suffisent.
- Un identifiant reçu dans l'URL est validé avec `ObjectId.isValid` avant tout appel au service, et
  renvoie 404 s'il est invalide.
- Un corps de requête se valide avec `RequestValidator`, pas avec `@Valid`.
- Ne jamais exposer de champ sensible (mot de passe, empreinte) dans une réponse.
- Chaque classe porte un commentaire d'une phrase, en français simple, qui dit ce qu'elle fait.

## Pour finir

Lance la commande `verifyCommand` et corrige jusqu'à ce que tous les tests passent. Ne rends pas
la main sur une suite rouge.
