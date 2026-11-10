# AGENTS.md — Source de Vérité Principale (Bonne Fête)

> **RÈGLE CRITIQUE POUR L'AGENT IA :**
> Ce fichier est ton point d'entrée obligatoire. Pour préserver ta fenêtre de contexte et garantir un code cohérent, **ne lis pas l'intégralité du codebase à l'aveugle**. Consulte d'abord cet index et uniquement le fichier de documentation ou le Skill correspondant à ta tâche.

---

## 1. Index des Sources de Vérité

| Fichier | Rôle | Quand le lire / mettre à jour ? |
| :--- | :--- | :--- |
| [`AGENTS.md`](./AGENTS.md) | Règles globales, stack technique, skills, garde-fous | Au début de chaque tâche |
| [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) | Structure des packages, flux de données, index des fichiers | Avant de créer/modifier une classe ou l'architecture |
| [`docs/CODING_STANDARDS.md`](./docs/CODING_STANDARDS.md) | Standards Kotlin/Compose/Glance, bonnes pratiques Room/DataStore/Alarms | Avant d'écrire ou refactoriser du code |
| [`docs/ROADMAP.md`](./docs/ROADMAP.md) | État d'avancement, tâches faites / en cours / à venir | Au début et à la fin de chaque fonctionnalité |
| [`docs/DECISIONS.md`](./docs/DECISIONS.md) | Décisions techniques (ADR), widget minuit, correspondance contacts, SDK | Lors d'un choix technique ou d'un arbitrage |
| [`README.md`](./README.md) | Présentation générale du projet, fonctionnalités et commandes Gradle | Lors d'un changement majeur de fonctionnalité ou de build |
| [`CHANGELOG.md`](./CHANGELOG.md) | Historique des versions et nouveautés | À chaque livraison de version |
| [`PLAY_STORE.md`](./PLAY_STORE.md) | Textes Google Play Console prêts à l'emploi | Avant publication ou mise à jour sur le Play Store |
| [`POLITIQUE_DE_CONFIDENTIALITE.md`](./POLITIQUE_DE_CONFIDENTIALITE.md) | Politique de confidentialité officielle (traitement local des contacts) | En cas d'évolution touchant aux données ou permissions |

---

## 2. Protocole d'Économie de Contexte

1. **Ciblage chirurgical** : Utilise la cartographie dans [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) pour identifier directement le fichier concerné sans scanner tout le projet.
2. **Lectures partielles** : Utilise les outlines de fichiers (`view_file_outline`) ou des plages de lignes (`startLine`/`endLine`) sur les fichiers volumineux (notamment `saints.json`).
3. **Mise à jour continue** :
    - Dès qu'un nouveau fichier Kotlin est créé ou déplacé, mets à jour la cartographie dans [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md).
    - Dès qu'une étape est validée, coche-la dans [`docs/ROADMAP.md`](./docs/ROADMAP.md).
    - Dès qu'une décision structurante est prise, ajoute une entrée concise dans [`docs/DECISIONS.md`](./docs/DECISIONS.md).

---

## 3. Skills Agentiques à Activer selon la Tâche

Avant d'implémenter l'un des sujets ci-dessous, **lis le fichier `SKILL.md` correspondant** via `read_file` pour appliquer les patterns officiels Android Studio :

| Domaine d'intervention | Skill à activer | Chemin du Skill |
| :--- | :--- | :--- |
| **Widget Glance, Mises à jour DataStore & Mémoire Bitmap (SDK 37)** | `glance-appwidget-updates-and-memory` | `~/.android-studio/skills/glance-appwidget-updates-and-memory/SKILL.md` |
| **Insets & Affichage plein écran** (Barre d'état/navigation, `enableEdgeToEdge`) | `edge-to-edge` | `~/.android-studio/skills/edge-to-edge/SKILL.md` |
| **Performance UI Compose** (Stabilité, recompositions dans la liste des saints) | `jetpack-compose-performance` | `~/.android-studio/skills/jetpack-compose-performance/SKILL.md` |
| **Audit ANR & Threading** (`goAsync()` dans les `BroadcastReceiver`, requêtes `ContactsContract`, Room) | `anr-audit-and-fix` | `~/.android-studio/skills/anr-audit-and-fix/SKILL.md` |
| **Permissions & Sécurité IPC** (`READ_CONTACTS`, `POST_NOTIFICATIONS`, `BroadcastReceiver` exportés) | `android-permissions-security` | `~/.android-studio/skills/android-permissions-security/SKILL.md` |
| **Optimisation R8 / ProGuard** (`ActionCallback`, Hilt, Serialization) | `r8-analyzer` | `~/.android-studio/skills/r8-analyzer/SKILL.md` |

---

## 4. Stack Technique de Référence

- **Nom de l'application** : `Bonne Fête`
- **Package racine / Namespace** : `fr.antoinehory.bonnefete`
- **SDK** : `minSdk = 26`, `targetSdk = 37`, `compileSdk = 37`
- **Langage & Build** : Kotlin `2.2.10`, AGP `9.2.0`, KSP `2.2.10-2.0.2`, Java 11, Version Catalog (`gradle/libs.versions.toml`)
- **Injection de Dépendances** : Dagger Hilt (`2.59.2`) + `androidx.hilt:hilt-work` (`1.2.0`) + `hilt-navigation-compose`
- **UI & Thème** :
    - Jetpack Compose (BOM `2026.02.01`), Material 3, Edge-to-Edge activé (`BonneFeteTheme`).
    - Thème médiéval enluminure (`MedievalBlue`, `AntiqueGold`, `RubyRed`, `Parchment`) et police personnalisée (`R.font.medieval_font`).
    - Splash Screen Android 12+ (`androidx.core:core-splashscreen`).
- **Widget d'écran d'accueil** : Jetpack Glance (`androidx.glance:glance-appwidget:1.1.1` & `glance-material3`) avec `PreferencesGlanceStateDefinition` (`updateAppWidgetState`) et sous-échantillonnage Bitmap conforme aux limites mémoire Android 17 (API 37).
- **Persistance Locale (Calendrier des Saints)** : AndroidX Room (`2.8.4`) — pré-remplie au premier lancement depuis `assets/saints.json` via `kotlinx-serialization-json`.
- **Préférences Utilisateur** : AndroidX DataStore Preferences (`1.2.1`) — heure/minute de notification et filtre « Uniquement si dans les contacts ».
- **Planification & Tâches de fond** : `AlarmManager` (`setExactAndAllowWhileIdle` avec repli `setAndAllowWhileIdle`) + `WorkManager` (`DailyUpdateWorker`) + `BroadcastReceiver` (`WidgetRefreshReceiver`, `SaintWidgetReceiver`, `BootReceiver`).

---

## 5. Garde-Fous Critiques (Résumé)

> Pour le détail complet des règles de code, consulter [`docs/CODING_STANDARDS.md`](./docs/CODING_STANDARDS.md).

1. **Correspondance stricte des prénoms dans les contacts (`ContactService`)** :
    - Ne **jamais** utiliser `.contains()` en sous-chaîne brute pour comparer le saint du jour aux contacts (ex. `"Clément"` ne doit **jamais** matcher `"Clémentine"` ou `"Jean-Clément"`).
    - Toujours comparer par **token de mot entier** après normalisation NFD (sans accents, en minuscules), en gérant proprement les prénoms composés (`Jean-Baptiste`) et les épithètes historiques (`François d'Assise` ➔ `François`).
2. **Mise à jour garantie du Widget à minuit sans redimensionnement (`SaintWidget`)** :
    - Un simple `SaintWidget().updateAll(context)` sans mutation d'état Glance peut être ignoré par la session Glance active.
    - Toujours passer par `SaintWidget.pushWidgetUpdate(context, saintRepository)` qui mute `PreferencesGlanceStateDefinition` (`KEY_DATE_KEY`, `KEY_SAINT_NAME`, `KEY_REVISION`) via `updateAppWidgetState(context, glanceId)` avant d'appeler `widget.update(context, glanceId)`.
    - Toujours utiliser `goAsync()` dans `WidgetRefreshReceiver`, `SaintWidgetReceiver` et `BootReceiver`, et reprogrammer systématiquement l'alarme du lendemain (`workScheduler.scheduleMidnightRefresh()`) après chaque exécution à minuit.
3. **Respect du plafond mémoire Bitmap des Widgets (Android 17 / SDK 37)** :
    - Décoder et sous-échantillonner (`RGB_565`) l'image de fond `R.drawable.widget_background` **une seule fois dans `provideGlance` avant `provideContent`**, et fournir un `onCompositionError` de repli sans bitmap.
4. **Confidentialité 100 % locale** :
    - La lecture du répertoire (`READ_CONTACTS`) s'effectue exclusivement en mémoire locale pour filtrer la notification quotidienne. Aucune donnée de contact ne doit jamais être persistée ni transmise.
