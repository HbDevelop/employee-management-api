---
paths:
  - "src/test/java/**/*.java"
---

# Tests

- JUnit 5, Mockito et AssertJ uniquement : ils sont fournis par `spring-boot-starter-test`.
  **Ne propose jamais** d'ajouter une autre bibliothèque de test.
- Un test unitaire (`*Test`) ne démarre ni Spring ni base de données : services avec
  `@ExtendWith(MockitoExtension.class)`, contrôleurs avec `MockMvcBuilders.standaloneSetup`.
- Un test d'intégration (`*IT`) hérite de `AbstractIntegrationTest`, qui démarre l'application et
  un MongoDB temporaire. Il a besoin de Docker.
- Une classe de test par classe testée, dans le même package (`EmployeeServiceTest`).
- Tester le comportement observable de la couche, pas l'implémentation de ses dépendances.
- Après toute modification : `docker compose run --rm verify`.
