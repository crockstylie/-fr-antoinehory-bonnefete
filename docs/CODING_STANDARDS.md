# CODING_STANDARDS.md — Standards de Code & Best Practices

> Ce document définit les conventions de code, les idiomes Kotlin/Compose/Glance et les bonnes pratiques obligatoires pour garantir un code propre, performant, testable et maintenable sur **Bonne Fête**.

---

## 1. Conventions Kotlin & Style

- **Langue du code** :
    - Identifiants (classes, fonctions, propriétés, paramètres, tables SQL) en **anglais** clair et explicite (`SaintEntity`, `ContactService`, `pushWidgetUpdate`, `doesContactMatchSaint`).
    - Documentation KDoc et commentaires explicatifs en **français** ou anglais technique cohérent.
- **Immuabilité par défaut** :
    - Toujours privilégier `val` à `var`.
    - Utiliser des collections immuables (`List`, `Set`) dans les états exposés à l'UI (`MainUiState`).
- **Null-safety & Validation** :
    - Proscrire l'opérateur `!!` (non-null assertion). Utiliser `?:`, `let`, ou des retours anticipés (`?: continue`, `?: return`).

---

## 2. Coroutines, BroadcastReceivers & Threading

- **Interdiction des Coroutines orphelines dans `BroadcastReceiver`** :
    - Dans tout `BroadcastReceiver` (`WidgetRefreshReceiver`, `SaintWidgetReceiver`, `BootReceiver`) exécutant une opération asynchrone (accès Room, DataStore ou mise à jour Glance), il est **obligatoire** d'appeler `val pendingResult = goAsync()` au début de `onReceive()` et d'appeler `pendingResult.finish()` dans un bloc `finally`.
    - Sans `goAsync()`, Android (et particulièrement le gestionnaire de batterie *Freecess* de Samsung One UI) gèle ou détruit le processus dès le retour de `onReceive()`, avant que le widget ait pu se mettre à jour.
- **Isolation du Main Thread (Zéro ANR)** :
    - Toutes les opérations de lecture `assets/saints.json`, requêtes Room (`SaintDao`), lectures du `ContentResolver` (`ContactsContract`) et décodages de `Bitmap` doivent s'exécuter sur `Dispatchers.IO`.

---

## 3. Jetpack Glance (Widget) & Limites Mémoire Android 17 (SDK 37)

- **Mise à jour d'état explicite (`updateAppWidgetState`)** :
    - Ne jamais se contenter d'un appel nu à `SaintWidget().updateAll(context)` pour rafraîchir le saint du jour : si une session Glance est active ou si aucun état observé dans `provideContent` n'a changé, l'UI du widget reste figée jusqu'à un redimensionnement manuel.
    - Toujours appeler `SaintWidget.pushWidgetUpdate(context, saintRepository)` qui mute `PreferencesGlanceStateDefinition` (`KEY_DATE_KEY`, `KEY_SAINT_NAME`, `KEY_REVISION`) via `updateAppWidgetState(context, glanceId)` puis appelle immédiatement `widget.update(context, glanceId)`.
- **Sous-échantillonnage des Bitmaps hors de `provideContent`** :
    - Avec `SizeMode.Exact`, `provideContent` est invoqué plusieurs fois par mise à jour (orientations portrait/paysage).
    - Toujours décoder et sous-échantillonner (`Bitmap.Config.RGB_565`) l'image de fond `R.drawable.widget_background` **une seule fois dans `provideGlance` avant `provideContent`** afin que `RemoteViews` déduplique le `Bitmap` et respecte strictement le plafond mémoire d'Android 17 (`6 * width * height` octets).
- **Fallback d'erreur sans Bitmap** :
    - Toujours implémenter `onCompositionError` dans `SaintWidget` avec un `RemoteViews` léger de repli (`R.layout.glance_default_layout`).

---

## 4. Correspondance des Contacts (`ContactService`)

- **Comparaison par Token de Mot Entier obligatoire** :
    - Ne **jamais** utiliser `String.contains()` pour vérifier si le nom d'un contact contient le prénom du saint du jour.
    - Toujours normaliser les chaînes en `Normalizer.Form.NFD` (suppression des diacritiques `\p{InCombiningDiacriticalMarks}+`, ligatures `œ`/`æ`, minuscules) et découper en tokens lexicaux (`[a-z0-9]+(?:-[a-z0-9]+)*`).
    - Vérifier par des tests unitaires systématiques dans `ContactServiceTest` les cas de préfixes féminins/dérivés (`"Clément"` vs `"Clémentine"`, `"Jean"` vs `"Jeanne"` / `"Jean-Baptiste"`, `"Paul"` vs `"Pauline"`).
