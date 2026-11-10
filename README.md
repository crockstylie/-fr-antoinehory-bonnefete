# 📜 Bonne Fête !

**Bonne Fête !** est une application Android permettant d'afficher le saint du jour sur son écran d'accueil grâce à un widget au style médiéval enluminé et de recevoir une notification quotidienne intelligente lorsqu'un contact de votre répertoire fête son prénom.

---

## ✨ Fonctionnalités principales

- **Widget d'écran d'accueil (Jetpack Glance)** :
    - Affiche le saint du jour avec une typographie médiévale dorée (`medieval_font.otf`) sur un fond enluminé.
    - Texte adaptatif selon les dimensions du widget (`SizeMode.Exact`).
    - **Rafraîchissement automatique garanti à minuit** via `PreferencesGlanceStateDefinition` (`updateAppWidgetState`), `AlarmManager` (`goAsync()`), écoute des changements système de date/heure et cycle `updatePeriodMillis`.
- **Notifications quotidiennes intelligentes** :
    - Heure de rappel personnalisable à la minute près.
    - **Détection exacte dans les contacts** : option permettant de ne recevoir une notification que si le prénom exact du saint du jour figure dans vos contacts (insensible aux accents et à la casse, sans faux positifs de sous-chaînes comme *Clément* / *Clémentine*).
    - Bouton de test immédiat dans les paramètres.
- **Calendrier complet des Saints** :
    - Consultation hors-ligne des 366 jours de l'année (`saints.json` pré-chargé dans Room).

---

## 🛠️ Stack Technique & Architecture

- **Namespace / Application ID** : `fr.antoinehory.bonnefete`
- **SDK** : `minSdk = 26`, `targetSdk = 37`, `compileSdk = 37`
- **Langage** : Kotlin `2.2.10` (`jvmTarget = 11`)
- **Architecture** : Clean MVVM (`ui`, `domain`, `data`, `widget`, `worker`, `di`) — voir [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md)
- **Injection de dépendances** : Dagger Hilt (`2.59.2`) + `HiltWorker`
- **UI & Widget** : Jetpack Compose (Material 3, Edge-to-Edge) & Jetpack Glance (`1.1.1`)
- **Persistance** : Room Database (`AppDatabase`) + Jetpack DataStore Preferences
- **Planification** : `AlarmManager` (Exact Alarms + `BootReceiver`) & `WorkManager`

---

## 🚀 Compilation et Tests

```bash
# Compiler l'APK de debug
./gradlew :app:assembleDebug

# Lancer les tests unitaires JVM
./gradlew :app:testDebugUnitTest

# Générer le bundle AAB de release pour Google Play
./gradlew :app:bundleRelease
```

---

## 🔒 Confidentialité

L'analyse de vos contacts s'effectue **exclusivement en local** sur votre appareil. Aucune donnée personnelle n'est collectée, stockée en ligne ou transmise à des serveurs tiers. Consultez la [Politique de Confidentialité](./POLITIQUE_DE_CONFIDENTIALITE.md) pour plus de détails.

---

## 👤 Auteur

Développé par **Antoine « Crock » Hory**
- 🌐 Site web : [antoinehory.fr](https://antoinehory.fr)
- 📧 Contact : [contact@antoinehory.fr](mailto:contact@antoinehory.fr)
- ☕ Soutenir le projet : [PayPal.me/kuroku](https://paypal.me/kuroku)
