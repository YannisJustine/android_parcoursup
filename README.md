# Application Parcoursup Android

Application Android moderne pour consulter les formations Parcoursup avec statistiques 2024 et carte interactive.

## Fonctionnalites

### 1. Liste des Formations
- Affichage de toutes les formations avec filtrage par filiere
- Barre de recherche pour trouver rapidement une formation
- Statistiques agregees (nombre d'etablissements, capacite moyenne, taux d'acces, moyenne au bac)
- Ajout aux favoris directement depuis la liste

### 2. Favoris
- Sauvegarde locale des formations favorites (Room Database)
- Acces rapide aux formations sauvegardees
- Suppression facile des favoris

### 3. Carte Globale
- Visualisation de tous les etablissements sur une carte interactive MapLibre
- Clic sur un marqueur pour acceder aux details de la formation
- Navigation fluide vers les pages de detail

### 4. Details d'une Formation
- Informations completes sur l'etablissement et la formation
- Statistiques 2024 :
  - Capacite, nombre de voeux, admis
  - Taux d'acces
  - Repartition par genre (pourcentage et nombre)
  - Repartition par bac d'origine (General, Tech, Pro)
  - Mentions au bac des admis
- Carte individuelle avec localisation precise de l'etablissement
- Bouton favori pour sauvegarder la formation

## Architecture

L'application suit les bonnes pratiques Android modernes inspirees du projet [Now in Android](https://github.com/android/nowinandroid).

### Architecture MVVM (Model-View-ViewModel)
```
app/src/main/java/com/ensiie/justine/parcoursup/
├── data/
│   ├── model/          # Data classes (Formation, ApiResponse, etc.)
│   ├── network/        # Retrofit API services
│   ├── local/          # Room Database (favoris)
│   └── repository/     # Repository pattern
├── di/                 # Dependency Injection (Koin)
├── ui/
│   ├── formations/     # Liste des formations
│   ├── favorites/      # Ecran des favoris
│   ├── filters/        # Filtres de recherche
│   ├── map/            # Carte globale
│   ├── detail/         # Details d'une formation
│   └── theme/          # Theme Material 3
├── navigation/         # Navigation 3
└── utils/              # Utilitaires
```

### Technologies utilisees

| Categorie | Technologie | Version |
|-----------|-------------|---------|
| UI | Jetpack Compose + Material Design 3 | BOM 2025.12.00 |
| Navigation | Navigation 3 | 1.0.0 |
| Dependency Injection | Koin | 4.1.1 |
| Networking | Retrofit + OkHttp + Moshi | 3.0.0 / 5.3.2 / 1.15.2 |
| Database | Room | 2.8.4 |
| Async | Kotlin Coroutines + Flow | - |
| Maps | MapLibre Compose | 0.12.1 |
| Serialization | Kotlinx Serialization | 1.9.0 |
| Architecture | MVVM + Repository Pattern | - |
| State Management | StateFlow + Unidirectional Data Flow | - |

## APIs utilisees

### API Parcoursup
- Base URL : `https://data.enseignementsup-recherche.gouv.fr/api/records/1.0/`
- Datasets :
  - `fr-esr-parcoursup` : Donnees des formations
  - Annee 2024 (session = 2024)

### Endpoints principaux
- Recuperation des filieres
- Formations par filiere avec filtres
- Recherche de formations
- Donnees geolocalisees pour la carte

## Lancement de l'application

### Prerequis
- Android Studio Ladybug (2024.2.1) ou superieur
- JDK 11
- Android SDK (min: 24, target: 36)

### Instructions

1. Cloner le projet

2. Ouvrir dans Android Studio
   ```bash
   # Ouvrir le dossier android/ dans Android Studio
   ```

3. Synchroniser Gradle
   - Android Studio va automatiquement telecharger les dependances

4. Lancer l'application
   - Connecter un appareil Android ou utiliser un emulateur
   - Cliquer sur Run

### Configuration Gradle

L'application utilise :
- Kotlin 2.2.21
- Compose BOM 2025.12.00
- Koin 4.1.1
- Retrofit 3.0.0
- OkHttp 5.3.2
- Room 2.8.4
- Navigation 3 1.0.0
- MapLibre Compose 0.12.1
- KSP 2.3.0

## Interface utilisateur

### Navigation Bottom Bar
Trois onglets principaux :
1. Formations - Liste et recherche
2. Favoris - Formations sauvegardees
3. Carte - Vue geographique

### Theme
- Material Design 3
- Support du mode sombre
- Couleurs adaptatives

## Donnees et Statistiques

L'application affiche les statistiques 2024 :
- Capacite des formations
- Taux d'acces
- Repartition hommes/femmes
- Types de bac (General, Technologique, Professionnel)
- Mentions au bac des admis
- Moyenne des moyennes au bac (calculee)

## Cartes MapLibre

### Carte Globale
- Affiche tous les etablissements avec coordonnees GPS
- Zoom et navigation
- Popup avec nom de l'etablissement et bouton d'action

### Carte Individuelle
- Zoom sur l'etablissement selectionne
- Marqueur avec popup
- Integration native via MapLibre Compose

## Gestion des donnees

### Local (Room)
- Stockage des formations favorites
- Acces rapide et offline
- Synchronisation automatique

### Remote (Retrofit)
- Appels API asynchrones avec Coroutines
- Gestion des erreurs
- Cache en memoire avec StateFlow

## Tests

Structure pour les tests :
- `test/` : Tests unitaires (ViewModels, Repository)
- `androidTest/` : Tests d'instrumentation (UI, Database)

## Notes techniques

### Bonnes pratiques appliquees
- Single Activity Architecture
- Unidirectional Data Flow (UDF)
- Separation of Concerns
- Dependency Injection avec Koin
- Repository Pattern
- StateFlow pour la gestion d'etat
- Coroutines pour l'asynchrone
- Type-safe Navigation avec Kotlinx Serialization
- Material Design 3

### Ameliorations possibles
- Pagination pour la liste des formations
- Filtres avances (departement, capacite, taux d'acces)
- Graphiques pour les statistiques
- Mode hors ligne complet
- Notifications pour les formations favorites
- Partage de formations
- Comparateur de formations

## Licence

Projet educatif - ENSIIE

## Auteur

Application developpee selon les specifications du projet Parcoursup avec les bonnes pratiques Android modernes.
