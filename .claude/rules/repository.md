---
paths:
  - "src/main/java/**/*Repository.java"
---

# Couche d'accès aux données

Ces règles ne se chargent que lorsque tu lis un fichier de la couche repository.

- Un repository est une **interface** qui étend `MongoRepository`. Spring Data fournit
  l'implémentation : n'écris jamais de classe `*RepositoryImpl`.
- Une requête s'exprime d'abord par le nom de la méthode (`findByDepartment`). `@Query` ne sert que
  si le nom ne suffit pas.
- **Aucune logique métier ici.** Pas de validation, pas de calcul, pas de règle de gestion : c'est
  le rôle du service. Le repository traduit un appel en requête et rend le résultat.
- Seul un service injecte un repository. Un contrôleur qui en reçoit un est un défaut.
- Les erreurs de la base ne sont pas attrapées ici. Le service les traduit si elles ont un sens
  métier (email déjà utilisé), sinon elles remontent à `GlobalExceptionHandler`.
- Toute méthode de requête ajoutée doit être exercée par un test d'intégration (`*IT`) : c'est le
  seul endroit où la requête générée est réellement exécutée.
