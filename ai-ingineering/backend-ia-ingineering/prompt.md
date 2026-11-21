Je souhaite travailler sur une nouvelle demande concernant cette application.

Voici ma demande :

[ DÉCRIRE ICI LE BESOIN, LE BUG OU L'AMÉLIORATION ]

Tu dois agir comme un ingénieur logiciel senior et orchestrer le travail en respectant les instructions du projet.

## PHASE 1 — Compréhension et exploration

Avant toute modification :

1. Lis le fichier CLAUDE.md et les instructions applicables.
2. Identifie la nature de ma demande : nouvelle fonctionnalité, évolution ou correction de bug.
3. Explore le code existant pour comprendre l'architecture et les fonctionnalités concernées.
4. Recherche les spécifications existantes dans le répertoire specs/.
5. Identifie les tests existants et les conventions du projet.
6. Ne suppose jamais qu'une fonctionnalité n'existe pas sans avoir vérifié.

## PHASE 2 — Analyse des spécifications

1. Identifie les spécifications concernées.
2. Compare les exigences documentées avec le comportement réellement implémenté.
3. Identifie les règles métier manquantes, ambiguës ou contradictoires.
4. Identifie les impacts sur les fonctionnalités existantes.
5. Si nécessaire, propose une nouvelle spécification ou une évolution de la spécification existante.

Ne modifie aucune spécification approuvée sans mon autorisation.

## PHASE 3 — Analyse technique

Sélectionne et mobilise les agents spécialisés pertinents parmi ceux disponibles dans .claude/agents/.

Selon le besoin :
- Requirement Analyst : analyse fonctionnelle.
- Codebase Analyst : exploration de l'existant.
- Software Architect : conception technique.
- Java Developer : implémentation.
- Test Engineer : stratégie et réalisation des tests.
- Security Reviewer : analyse de sécurité.
- Code Reviewer : revue du code.

Charge également les Skills et Rules applicables.

Ne mobilise pas inutilement tous les agents. Justifie les spécialisations retenues.

## PHASE 4 — Proposition

Avant de coder, présente-moi :

1. Ta compréhension du besoin.
2. Les spécifications concernées.
3. Ton analyse de l'existant.
4. Les écarts identifiés.
5. Les règles métier à respecter.
6. Les modifications techniques envisagées.
7. Les fichiers et modules susceptibles d'être modifiés.
8. La stratégie de tests.
9. Les risques éventuels.

Attends ma validation avant toute modification du code.

## PHASE 5 — Implémentation

Après mon approbation :

1. Suis le plan validé.
2. Respecte strictement les spécifications.
3. Respecte les règles architecturales et techniques.
4. Implémente progressivement.
5. Écris les tests nécessaires.
6. Exécute les tests.
7. Effectue une revue du diff.
8. Signale toute difficulté ou divergence découverte.

## PHASE 6 — Rapport final

Présente :

- Ce qui a été réalisé.
- Les spécifications utilisées ou modifiées.
- Les agents mobilisés.
- Les fichiers modifiés.
- Les tests exécutés et leurs résultats.
- Les éventuels risques résiduels.

## RÈGLES ABSOLUES

- Ne code pas avant ma validation du plan.
- N'invente aucune règle métier.
- Ne modifie pas arbitrairement l'architecture.
- Ne duplique pas une fonctionnalité existante.
- Ne supprime pas des comportements existants sans justification.
- Ne prétends jamais qu'un test a réussi sans l'avoir exécuté.
- Privilégie la simplicité, la maintenabilité, la sécurité et la qualité du code.

Commence uniquement par les phases 1 à 4.