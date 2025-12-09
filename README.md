# Application Parcoursup Android

Application Android moderne pour consulter les formations Parcoursup avec statistiques 2024 et carte interactive.

## 🎯 Fonctionnalités

### 1. **Liste des Formations** 📚
- Affichage de toutes les formations avec filtrage par filière
- Barre de recherche pour trouver rapidement une formation
- Statistiques agrégées (nombre d'établissements, capacité moyenne, taux d'accès, moyenne au bac)
- Ajout aux favoris directement depuis la liste

### 2. **Favoris** ⭐
- Sauvegarde locale des formations favorites (Room Database)
- Accès rapide aux formations sauvegardées
- Suppression facile des favoris

### 3. **Carte Globale** 🗺️
- Visualisation de tous les établissements sur une carte interactive Leaflet
- Clic sur un marqueur pour accéder aux détails de la formation
- Navigation fluide vers les pages de détail

### 4. **Détails d'une Formation** 📊
- Informations complètes sur l'établissement et la formation
- **Statistiques 2024** :
  - Capacité, nombre de vœux, admis
  - Taux d'accès
  - Répartition par genre (pourcentage et nombre)
  - Répartition par bac d'origine (Général, Tech, Pro)
  - Mentions au bac des admis
- **Carte individuelle** avec localisation précise de l'établissement
- Bouton favori pour sauvegarder la formation

## 🏗️ Architecture

L'application suit les bonnes pratiques Android modernes inspirées du projet [Now in Android](https://github.com/android/nowinandroid) :

### Architecture MVVM (Model-View-ViewModel)
```
app/
├── data/
│   ├── model/          # Data classes (Formation, ApiResponse, etc.)
│   ├── network/        # Retrofit API services
│   ├── local/          # Room Database (favoris)
│   └── repository/     # Repository pattern
├── di/                 # Dependency Injection (Hilt)
├── ui/
│   ├── formations/     # Liste des formations
│   ├── favorites/      # Écran des favoris
│   ├── map/           # Carte globale
│   └── detail/        # Détails d'une formation
└── navigation/         # Navigation Compose
```

### Technologies utilisées

- **UI** : Jetpack Compose + Material Design 3
- **Navigation** : Navigation Compose avec type-safe routes
- **Dependency Injection** : Hilt
- **Networking** : Retrofit + OkHttp + Moshi
- **Database** : Room
- **Async** : Kotlin Coroutines + Flow
- **Maps** : Leaflet (WebView) - pas de Google SDK
- **Architecture** : MVVM + Repository Pattern
- **State Management** : StateFlow + Unidirectional Data Flow

## 📡 APIs utilisées

### API Parcoursup
- **Base URL** : `https://data.enseignementsup-recherche.gouv.fr/api/records/1.0/`
- **Datasets** :
  - `fr-esr-parcoursup` : Données des formations
  - Année 2024 (session = 2024)

### Endpoints principaux
- Récupération des filières
- Formations par filière avec filtres
- Recherche de formations
- Données géolocalisées pour la carte

## 🚀 Lancement de l'application

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 11
- Android SDK (min: 24, target: 36)

### Instructions

1. **Cloner le projet** (déjà fait ici)

2. **Ouvrir dans Android Studio**
   ```bash
   # Ouvrir le dossier android/ dans Android Studio
   ```

3. **Synchroniser Gradle**
   - Android Studio va automatiquement télécharger les dépendances

4. **Lancer l'application**
   - Connecter un appareil Android ou utiliser un émulateur
   - Cliquer sur Run (▶️)

### Build Gradle

L'application utilise :
- Kotlin 2.0.21
- Compose BOM 2024.09.00
- Hilt 2.51.1
- Retrofit 2.11.0
- Room 2.6.1
- Navigation Compose 2.8.5

## 🎨 Interface utilisateur

### Navigation Bottom Bar
Trois onglets principaux :
1. 🔍 **Formations** - Liste et recherche
2. ⭐ **Favoris** - Formations sauvegardées
3. 🗺️ **Carte** - Vue géographique

### Thème
- Material Design 3
- Support du mode sombre
- Couleurs adaptatives

## 📊 Données et Statistiques

L'application affiche les statistiques 2024 :
- Capacité des formations
- Taux d'accès
- Répartition hommes/femmes
- Types de bac (Général, Technologique, Professionnel)
- Mentions au bac des admis
- Moyenne des moyennes au bac (calculée)

## 🗺️ Cartes Leaflet

### Carte Globale
- Affiche tous les établissements avec coordonnées GPS
- Zoom et navigation
- Popup avec nom de l'établissement et bouton d'action

### Carte Individuelle
- Zoom sur l'établissement sélectionné
- Marqueur avec popup
- Intégration via WebView

## 🔄 Gestion des données

### Local (Room)
- Stockage des formations favorites
- Accès rapide et offline
- Synchronisation automatique

### Remote (Retrofit)
- Appels API asynchrones avec Coroutines
- Gestion des erreurs
- Cache en mémoire avec StateFlow

## 🧪 Tests

Structure pour les tests :
- `test/` : Tests unitaires (ViewModels, Repository)
- `androidTest/` : Tests d'instrumentation (UI, Database)

## 📝 Notes techniques

### Bonnes pratiques appliquées
✅ Single Activity Architecture
✅ Unidirectional Data Flow (UDF)
✅ Separation of Concerns
✅ Dependency Injection
✅ Repository Pattern
✅ StateFlow pour la gestion d'état
✅ Coroutines pour l'asynchrone
✅ Type-safe Navigation
✅ Material Design 3

### Améliorations possibles
- Pagination pour la liste des formations
- Filtres avancés (département, capacité, taux d'accès)
- Graphiques pour les statistiques
- Mode hors ligne complet
- Notifications pour les formations favorites
- Partage de formations
- Comparateur de formations

## 📄 Licence

Projet éducatif - BUT S4 WEB

## 👥 Auteur

Application développée selon les spécifications du projet Parcoursup avec les bonnes pratiques Android modernes.
