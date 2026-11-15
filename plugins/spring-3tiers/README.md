# spring-3tiers

Plugin Claude Code pour les API Spring Boot + MongoDB en architecture 3-tiers.

| Composant | Type | Rôle |
|---|---|---|
| `nouveau-domaine` | skill | `/spring-3tiers:nouveau-domaine <nom>` crée un domaine complet en imitant le domaine de référence du projet |
| `format.mjs` | hook `PostToolUse` | Normalise chaque fichier que Claude édite (espaces de fin, saut de ligne final) |

Le hook est un script Node.js : `node` doit être installé sur le poste.

## Installation

```bash
claude plugin marketplace add HbDevelop/employee-management-api
claude plugin install spring-3tiers@employee-management
```

## Configuration du projet

Le skill lit `.claude/spring-3tiers.json` à la racine du projet qui l'utilise. Copiez
[`config/spring-3tiers.example.json`](config/spring-3tiers.example.json) et adaptez :

| Clé | Rôle |
|---|---|
| `referenceDomain` | Le dossier du domaine à imiter, par exemple `src/main/java/com/example/employeeapi/employees` |
| `verifyCommand` | La commande qui compile et lance les tests, par exemple `docker compose run --rm verify` |

## Versions

- **0.2.0** — le domaine de référence et la commande de vérification sont lus dans
  `.claude/spring-3tiers.json` (le fichier est désormais requis) ; le hook de formatage rejoint le
  plugin.
- **0.1.0** — le skill `nouveau-domaine`, copié du projet tel quel.
