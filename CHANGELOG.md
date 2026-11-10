# 📋 Changelog - Bonne Fête !

Toutes les modifications notables apportées à ce projet sont documentées dans ce fichier.

---

## [1.2.0] - 2026-11-10

### ✨ Ajouté
- **Documentation Source de Vérité** : Ajout de `AGENTS.md`, `docs/ARCHITECTURE.md`, `docs/CODING_STANDARDS.md`, `docs/ROADMAP.md`, `docs/DECISIONS.md` et `PLAY_STORE.md`.
- **Compatibilité Android 17 (API 37)** : Mise à niveau de `compileSdk` et `targetSdk` vers `37` pour répondre aux exigences de la Google Play Console, avec sous-échantillonnage Bitmap conforme au plafond mémoire `RemoteViews` d'Android 17 et fallback `onCompositionError`.

### 🐛 Corrigé
- **Rafraîchissement automatique du Widget à minuit (Samsung Galaxy A56 & tous appareils)** :
    - Intégration de `PreferencesGlanceStateDefinition` (`updateAppWidgetState` + `widget.update`) via `SaintWidget.pushWidgetUpdate()` pour forcer la recomposition du widget à minuit sans avoir besoin de le redimensionner manuellement.
    - Ajout de `goAsync()` sur `Dispatchers.IO` et de la reprogrammation automatique de l'alarme du lendemain (`workScheduler.scheduleMidnightRefresh()`) dans `WidgetRefreshReceiver`.
    - Écoute des événements système `ACTION_DATE_CHANGED`, `ACTION_TIME_SET` et `ACTION_TIMEZONE_CHANGED` dans `SaintWidgetReceiver` et activation d'un intervalle de secours `updatePeriodMillis="1800000"` dans `saint_widget_info.xml`.
- **Précision de la détection des contacts (`Clément` ≠ `Clémentine`)** :
    - Remplacement de la recherche par sous-chaîne (`.contains()`) par une comparaison lexicale stricte par **mot entier (token exact)** dans `ContactService`. Un saint comme *« Clément »* ne déclenche plus de notification erronée pour un contact nommé *« Clémentine »*, tout en gérant les prénoms composés (*Jean-Baptiste*) et les épithètes historiques (*François d'Assise*).

---

## [1.1.0] - 2026-05-03

### Ajouté
- **Gestion de la résilience** : Ajout d'un `BootReceiver` pour reprogrammer automatiquement les notifications et le rafraîchissement du widget après un redémarrage du téléphone.
- **Rafraîchissement automatique du Widget** : Alarme quotidienne pour actualiser le prénom du jour.
- **Internationalisation** : Extraction des chaînes de caractères vers `strings.xml`.
- **Documentation technique** : Ajout de KDoc pour les classes et méthodes principales.

### Corrigé
- **Précision des Notifications** : Migration vers `AlarmManager` (Exact Alarms) pour assurer un déclenchement à la seconde près.
- **Permission Système** : Utilisation de `USE_EXACT_ALARM` pour Android 14+.

### Modifié
- **Épuration visuelle du Widget** : Suppression du préfixe "Saint" ou "Sainte" devant le prénom pour un design plus épuré.

---

## [1.0.0] - 2026-04-24
- Lancement initial de l'application sur le Google Play Store.
- Widget médiéval avec police personnalisée.
- Système de notifications basé sur le répertoire de contacts.
- Base de données locale Room contenant le calendrier des saints français.
