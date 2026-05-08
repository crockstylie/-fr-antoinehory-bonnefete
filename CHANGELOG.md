# Changelog - Bonne Fête !

Toutes les modifications notables apportées à ce projet seront documentées dans ce fichier.

## [1.1.0] - 2026-05-03

### Ajouté
- **Gestion de la résilience** : Ajout d'un `BootReceiver` pour reprogrammer automatiquement les notifications et le rafraîchissement du widget après un redémarrage du téléphone.
- **Rafraîchissement automatique du Widget** : Nouvelle alarme quotidienne à 00h01 pour garantir que le prénom du jour se met à jour sans intervention manuelle.
- **Internationalisation** : Extraction de toutes les chaînes de caractères vers `strings.xml`.
- **Documentation technique** : Ajout de KDoc pour les classes et méthodes principales.

### Corrigé
- **Bug du Widget** : Correction du problème nécessitant le redimensionnement du widget pour actualiser le prénom.
- **Précision des Notifications** : Migration complète vers `AlarmManager` (Exact Alarms) pour assurer un déclenchement à la seconde près.
- **Permission Système** : Utilisation de `USE_EXACT_ALARM` pour une compatibilité totale avec Android 14+.

### Modifié
- **Épuration visuelle du Widget** : Suppression du préfixe "Saint" ou "Sainte" devant le prénom pour un design plus épuré.
- **Optimisation Performance** : Amélioration de la gestion des Bitmaps pour le rendu de la police personnalisée.
- **README** : Réécriture complète pour une présentation plus professionnelle.

---

## [1.0.0] - 2026-04-24
- Lancement initial de l'application sur le Google Play Store.
- Widget médiéval avec police personnalisée.
- Système de notifications basé sur le répertoire de contacts.
- Base de données locale Room contenant le calendrier des saints français.
