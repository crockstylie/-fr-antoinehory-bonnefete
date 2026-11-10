# ROADMAP.md — Suivi d'Avancement du Projet

> Ce fichier trace l'état exact du développement de **Bonne Fête**. L'agent IA doit le consulter pour savoir où en est le projet et le mettre à jour après chaque étape terminée.

Légende : `[x]` Terminé | `[/]` En cours | `[ ]` À faire

---

## Phase 1 : Socle, Base de Données des Saints, Widget Médiéval & Notifications (`v1.0.0`)
- [x] Initialisation du projet Android `fr.antoinehory.bonnefete` (Kotlin, Jetpack Compose, Material 3, Hilt)
- [x] Intégration du calendrier français des saints (`assets/saints.json`) et persistance locale Room (`SaintEntity`, `SaintDao`, `AppDatabase`, `SaintRepository`)
- [x] Création du thème médiéval enluminé (`BonneFeteTheme`, palette `MedievalBlue` / `AntiqueGold` et police personnalisée `medieval_font.otf`)
- [x] Création du widget d'écran d'accueil (`SaintWidget` avec Jetpack Glance)
- [x] Création du système de notifications quotidiennes (`DailyUpdateWorker`, `NotificationService`) et filtrage par contacts (`ContactService`)
- [x] Création des écrans Compose : `HomeScreen`, `MainScreen` (Paramètres), `SaintsListScreen` (Calendrier annuel) et `InfoScreen` (À propos)

## Phase 2 : Alarmes Exactes, BootReceiver & Épuration du Widget (`v1.1.0`)
- [x] Migration de la planification vers `AlarmManager` (`WorkScheduler` avec `USE_EXACT_ALARM`)
- [x] Ajout de `BootReceiver` pour reprogrammer les alarmes au redémarrage du téléphone
- [x] Suppression du préfixe « Saint / Sainte » sur le widget pour un affichage épuré centré sur le prénom
- [x] Extraction des chaînes de caractères dans `res/values/strings.xml`

## Phase 3 : Documentation Source de Vérité, Migration SDK 37, Correctif Widget Minuit (Samsung A56) & Précision Contacts (`v1.2.0`)
- [x] Mise en place de la documentation modulaire orientée IA sur le modèle de *Mon Simple Journal* :
    - `AGENTS.md`
    - `docs/ARCHITECTURE.md`
    - `docs/CODING_STANDARDS.md`
    - `docs/ROADMAP.md`
    - `docs/DECISIONS.md`
    - `PLAY_STORE.md`
- [x] Mise à niveau du SDK pour conformité Google Play Console :
    - Passage à `compileSdk = 37` et `targetSdk = 37` dans `app/build.gradle.kts` (`versionCode = 3`, `versionName = "1.2.0"`)
    - Nettoyage des dépendances dupliquées et ajout des règles R8 Glance (`ActionCallback`) dans `app/proguard-rules.pro`
    - Sous-échantillonnage de l'image de fond du widget en amont de `provideContent` et ajout de `onCompositionError` (conformité Android 17 / API 37)
- [x] Correction du bug de non-rafraîchissement automatique du widget à minuit (notamment sur Samsung Galaxy A56) :
    - Intégration de `PreferencesGlanceStateDefinition` et `SaintWidget.pushWidgetUpdate()` utilisant `updateAppWidgetState` (`KEY_DATE_KEY`, `KEY_SAINT_NAME`, `KEY_REVISION`) suivi de `widget.update()` pour forcer la recomposition sans avoir besoin de redimensionner le widget
    - Utilisation de `goAsync()` sur `Dispatchers.IO` dans `WidgetRefreshReceiver` + reprogrammation automatique de l'alarme du minuit suivant (`workScheduler.scheduleMidnightRefresh()`)
    - Activation de `updatePeriodMillis="1800000"` dans `saint_widget_info.xml` et écoute de `ACTION_DATE_CHANGED`, `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED` dans `SaintWidgetReceiver`
    - Initialisation automatique au démarrage de l'application dans `BonneFeteApp` et lors des mises à jour d'application (`ACTION_MY_PACKAGE_REPLACED` dans `BootReceiver`)
- [x] Correction du bug de correspondance des contacts (`"Clément"` ne doit plus notifier pour `"Clémentine"`) :
    - Remplacement du `.contains()` en sous-chaîne par une tokenisation lexicale stricte par mot entier (`doesContactMatchSaint`) dans `ContactService`
    - Gestion des prénoms composés (`Jean-Baptiste`), des épithètes historiques (`François d'Assise`, `Antoine de Padoue`) et exclusion des fêtes non-prénoms (`Jour de l'An`, `Fête Nationale`, etc.)
- [x] Mise à jour et validation des tests unitaires JVM (`ContactServiceTest`, `MainViewModelTest`, `SaintRepositoryTest`)
