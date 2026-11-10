# DECISIONS.md — Journal des Décisions Techniques (ADR)

> Ce fichier consigne les choix d'architecture et les contraintes techniques clés du projet **Bonne Fête** pour éviter de remettre en question des décisions déjà arbitrées.

---

## ADR-001 : Stockage Hybride `assets/saints.json` + Base Locale Room (`AppDatabase`)
- **Date** : 2026-04-24
- **Contexte** : L'application doit fonctionner 100 % hors-ligne et accéder instantanément au saint du jour (depuis le widget, le worker de notification et l'interface principale) ainsi qu'à la liste complète des 366 jours de l'année.
- **Décision** :
    1. Conserver le calendrier de référence dans `app/src/main/assets/saints.json`.
    2. Au premier accès (`SaintRepository.populateDatabaseIfNeeded()`), désérialiser le JSON et insérer les entrées dans la table Room `saints` (`SaintEntity`).
    3. Exposer les lectures via des `Flow` Room (`SaintDao.getSaintForDate(month, day)` et `getAllSaints()`).
- **Conséquences** :
    - Lectures SQL indexées ultra-rapides pour le widget et les workers en arrière-plan, sans re-parser le fichier JSON à chaque réveil.

## ADR-002 : Rendu de la Police Médiévale par Bitmap dans Jetpack Glance (`SaintWidget`)
- **Date** : 2026-04-24
- **Contexte** : `RemoteViews` et Jetpack Glance ne permettent pas d'utiliser directement une police personnalisée `.otf` (`R.font.medieval_font`) dans un composant `androidx.glance.text.Text` sur tous les launchers Android.
- **Décision** :
    - Dessiner les textes du widget (« Bonne Fête ! » et le prénom du saint) sur un `Canvas` Android avec `ResourcesCompat.getFont(context, R.font.medieval_font)` et la couleur `R.color.antique_gold`, en adaptant la taille en `dp` proportionnellement à `LocalSize.current` (`SizeMode.Exact`).
- **Conséquences** :
    - Rendu visuel fidèle à 100 % sur tous les téléphones et launchers Android, quelle que soit la taille du widget.

## ADR-003 : Documentation Modulaire Orientée IA & Catalogue de Skills (Source de Vérité)
- **Date** : 2026-11-10
- **Contexte** : Aligner **Bonne Fête** sur l'organisation documentaire éprouvée dans **Mon Simple Journal** afin d'offrir à l'agent IA une mémoire structurée du projet sans saturer sa fenêtre de contexte.
- **Décision** :
    - Créer et maintenir `AGENTS.md`, `docs/ARCHITECTURE.md`, `docs/CODING_STANDARDS.md`, `docs/ROADMAP.md`, `docs/DECISIONS.md` et `PLAY_STORE.md`.
- **Conséquences** :
    - Maintenance rapide, traçabilité complète des correctifs et cohérence architecturale entre les projets du développeur.

## ADR-004 : Mise à niveau vers `compileSdk = 37` et `targetSdk = 37` (Conformité Google Play Console & Android 17)
- **Date** : 2026-11-10
- **Contexte** : La Google Play Console exige que les mises à jour d'applications ciblent un niveau d'API récent pour autoriser le déploiement d'une nouvelle version. De plus, cibler Android 17 (`targetSdk = 37`) impose un plafond strict sur la mémoire des Bitmaps transmis via `RemoteViews` dans les widgets.
- **Décision** :
    1. Passer `compileSdk = 37` et `targetSdk = 37` dans `app/build.gradle.kts` (`versionCode = 3`, `versionName = "1.2.0"`).
    2. Dans `SaintWidget.provideGlance`, décoder et sous-échantillonner (`Bitmap.Config.RGB_565`) l'image `R.drawable.widget_background` **une seule fois avant `provideContent`** en vérifiant le plafond `calculateWidgetMemoryCapBytes(context)`.
    3. Ajouter `onCompositionError` dans `SaintWidget` et la règle ProGuard pour `ActionCallback` dans `app/proguard-rules.pro`.
- **Conséquences** :
    - Déblocage immédiat des mises à jour sur la Google Play Console et protection contre tout crash `RemoteViews` lié à la mémoire Bitmap sur Android 17.

## ADR-005 : Rafraîchissement Garanti du Widget à Minuit sans Redimensionnement (Fix Samsung Galaxy A56)
- **Date** : 2026-11-10
- **Contexte** : Sur Samsung Galaxy A56 (One UI), le widget ne se mettait pas à jour tout seul à minuit : il fallait redimensionner manuellement le widget pour voir apparaître le nouveau saint du jour. L'analyse du code a révélé quatre causes combinées :
    1. `SaintWidget` n'utilisait pas `PreferencesGlanceStateDefinition` et ne lisait aucun état dans `provideContent` : seul un changement de `LocalSize.current` (lors d'un redimensionnement) forçait la recomposition de `provideContent`.
    2. `WidgetRefreshReceiver.onReceive()` lançait une coroutine sur `Dispatchers.Main` sans appeler `goAsync()`, permettant à One UI de geler/tuer le récepteur avant la fin de la lecture Room et de l'envoi IPC au widget.
    3. `WidgetRefreshReceiver` ne rappelait jamais `workScheduler.scheduleMidnightRefresh()`, si bien que l'alarme `setExactAndAllowWhileIdle` (à coup unique) ne se reprogrammait pas d'une nuit sur l'autre.
    4. `saint_widget_info.xml` avait `updatePeriodMillis="0"` et `SaintWidgetReceiver` n'écoutait pas `ACTION_DATE_CHANGED` / `ACTION_TIME_CHANGED`.
- **Décision** :
    1. Configurer `stateDefinition = PreferencesGlanceStateDefinition` dans `SaintWidget` et créer `SaintWidget.pushWidgetUpdate(context, saintRepository)` qui écrit `KEY_DATE_KEY`, `KEY_SAINT_NAME` et incrémente `KEY_REVISION` via `updateAppWidgetState(context, glanceId)` avant d'appeler `widget.update(context, glanceId)`.
    2. Lire `currentState<Preferences>()` à l'intérieur de `provideContent` pour forcer la recomposition à chaque mutation d'état.
    3. Utiliser `goAsync()` + `Dispatchers.IO` dans `WidgetRefreshReceiver`, `SaintWidgetReceiver` et `BootReceiver`, et reprogrammer systématiquement `workScheduler.scheduleMidnightRefresh()` après chaque exécution.
    4. Définir `android:updatePeriodMillis="1800000"` (30 min) dans `saint_widget_info.xml` et écouter `DATE_CHANGED`, `TIME_SET`, `TIMEZONE_CHANGED` dans `SaintWidgetReceiver`.
- **Conséquences** :
    - Le widget se met à jour automatiquement à minuit (`00:00:05`) et au réveil du téléphone sur Samsung Galaxy A56 et tous les appareils Android, sans jamais avoir besoin d'être redimensionné.

## ADR-006 : Correspondance des Contacts par Token de Prénom Exact (`Clément` ≠ `Clémentine`)
- **Date** : 2026-11-10
- **Contexte** : L'ancienne implémentation de `ContactService` utilisait `contactName.normalize().contains(normalizedSaint)`. Ainsi, lorsque le saint du jour était *"Clément"*, l'application envoyait à tort une notification pour les contacts nommés *"Clémentine"* (car `"clementine".contains("clement")` vaut `true`), et de même pour *"Jean"* ➔ *"Jeanne"*, *"Paul"* ➔ *"Pauline"*, *"Luc"* ➔ *"Lucie"*, etc.
- **Décision** :
    1. Dans `ContactService`, extraire le prénom principal du saint (suppression des préfixes `"Saint "` / `"Sainte "`, exclusion des fêtes non-prénoms comme *"Jour de l'An"*, et retrait des épithètes historiques comme *"d'Assise"* ou *"de Padoue"*).
    2. Découper le nom du contact et celui du saint en tokens de mots complets via l'expression régulière `[a-z0-9]+(?:-[a-z0-9]+)*`.
    3. Exiger une égalité stricte de séquence de tokens (`containsTokenSequence`), avec prise en charge de la variante séparée par un espace uniquement pour les prénoms composés avec trait d'union (ex. `"Jean-Baptiste"` matche `"Jean-Baptiste"` et `"Jean Baptiste"`, mais `"Clément"` ne matche que le mot entier `"clement"`).
- **Conséquences** :
    - Élimination totale des faux positifs de sous-chaînes (`"Clément"` ne déclenche plus jamais de notification pour `"Clémentine"`), tout en conservant l'insensibilité aux accents et à la casse.
