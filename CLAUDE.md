# API Employés — Spring Boot + MongoDB

API REST en architecture 3-tiers : inscription, connexion par JWT et gestion des employés.

## Commandes

Java et Maven ne sont pas installés sur le poste : tout passe par Docker.

```bash
docker compose run --rm verify              # compilation + tests unitaires — LA commande à lancer après toute modification
docker compose run --rm integration-tests   # tests unitaires + tests d'intégration (MongoDB temporaire)
docker compose up --build -d                # construit et démarre l'API sur http://localhost:8080
```

Les tests unitaires sont mockés et **ne nécessitent aucune base de données**.

## Architecture

Un package par domaine sous `src/main/java/com/example/employeeapi/`, et dans chaque domaine une
classe par couche :

| Fichier | Rôle |
|---|---|
| `*Controller.java` | Déclare les routes, lit la requête, appelle le service, écrit la réponse |
| `*Service.java` | Logique métier, ne connaît ni HTTP ni Spring MVC |
| `*Repository.java` | Accès aux données (interface Spring Data), ne connaît pas le métier |
| `<Domaine>.java` | Document MongoDB |
| `*Request.java` | Corps de requête attendu, avec ses règles de validation (`record`) |

`core/` contient la configuration, la sécurité, la gestion des erreurs et la validation.
`authentication/` contient l'inscription, la connexion et le filtre JWT.

## Conventions

- **Injection par constructeur**, jamais de `@Autowired` sur un champ : c'est ce qui permet de
  construire une couche avec des mocks, sans démarrer Spring.
- Une route protégée se déclare en ajoutant son préfixe à `JwtAuthFilter.PROTECTED_PREFIXES`.
- Une réponse ne contient jamais l'entité `User` : on renvoie un `PublicUser`, sans mot de passe.
- Une couche ne parle qu'à la couche immédiatement inférieure. Un contrôleur n'appelle jamais un
  repository.
- Un corps de requête se valide avec `RequestValidator` (`core/validation/`), pas avec `@Valid` :
  c'est lui qui produit le format d'erreur de l'API (`[{"field": ..., "error": ...}]`).
- Les erreurs deviennent des réponses HTTP dans `GlobalExceptionHandler` (`core/error/`), pas dans
  les contrôleurs.
- Les tests sont **dans le même package** que le code testé, sous `src/test/java/` :
  `EmployeeServiceTest` pour `EmployeeService`.
- Tests unitaires avec JUnit 5, Mockito et AssertJ ; contrôleurs testés avec MockMvc en mode
  `standaloneSetup`. Les tests d'intégration (`*IT`) héritent de `AbstractIntegrationTest`.
- Le domaine de référence est `employees` : tout nouveau domaine l'imite fichier par fichier.

## Périmètre

- Ne lance pas l'application ni les tests d'intégration sans qu'on te le demande : ils ont besoin
  de Docker et prennent du temps. La boucle de vérification, c'est `docker compose run --rm verify`.
- Les fichiers `.env` contiennent le secret JWT. Ne les lis pas et ne les commite jamais : le
  `.gitignore` couvre `*.env`. Le modèle versionné est `.env.example`.
