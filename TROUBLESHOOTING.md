# 🐛 Guide de Débogage & Troubleshooting

## Problèmes Potentiels et Solutions

### 1. Erreurs de Compilation Gradle

#### Erreur : "Unresolved reference: hilt"
**Solution** :
```bash
# Dans le terminal Android Studio :
./gradlew clean
./gradlew build
```
Puis : `File > Invalidate Caches and Restart`

#### Erreur : "compileSdk version"
**Solution** :
Vérifier dans `build.gradle.kts` :
```kotlin
compileSdk = 36  // ou version installée
```

---

### 2. Erreurs Runtime

#### Crash au lancement : "Hilt not found"
**Solution** :
Vérifier dans `AndroidManifest.xml` :
```xml
<application
    android:name=".ParcoursupApplication"
    ...>
```

#### Crash : "No interface method"
**Solution** :
Ajouter dans `app/proguard-rules.pro` :
```proguard
-keep class com.ensiie.justine.parcoursup.data.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
```

---

### 3. Problèmes Réseau

#### API ne répond pas
**Solutions** :
1. Vérifier les permissions dans `AndroidManifest.xml` :
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

2. Ajouter `usesCleartextTraffic` pour HTTPS :
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

3. Vérifier la connexion Internet de l'émulateur

#### Timeout
**Solution** dans `AppModule.kt` :
```kotlin
.connectTimeout(60, TimeUnit.SECONDS)
.readTimeout(60, TimeUnit.SECONDS)
```

---

### 4. Problèmes avec Leaflet/WebView

#### Carte ne s'affiche pas
**Solutions** :

1. Activer JavaScript :
```kotlin
webView.settings.javaScriptEnabled = true
webView.settings.domStorageEnabled = true
```

2. Vérifier les logs WebView :
```kotlin
webView.webChromeClient = object : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        Log.d("WebView", consoleMessage.message())
        return true
    }
}
```

3. Tester le HTML dans un navigateur d'abord

---

### 5. Room Database

#### Crash : "Cannot create database"
**Solution** :
```kotlin
// Ajouter fallbackToDestructiveMigration en dev
Room.databaseBuilder(context, ParcoursupDatabase::class.java, "parcoursup_db")
    .fallbackToDestructiveMigration()
    .build()
```

#### Erreur : "Entity must have at least one primary key"
**Solution** :
Vérifier `@PrimaryKey` sur l'entity :
```kotlin
@Entity(tableName = "favorite_formations")
data class FavoriteFormation(
    @PrimaryKey val recordId: String,
    ...
)
```

---

### 6. Navigation

#### Retour arrière ne fonctionne pas
**Solution** :
Ajouter dans MainActivity :
```kotlin
override fun onBackPressed() {
    if (!navController.popBackStack()) {
        super.onBackPressed()
    }
}
```

#### Bottom bar reste visible sur détails
**Solution** :
Déjà implémenté avec :
```kotlin
val showBottomBar = currentDestination?.hierarchy?.any { ... }
```

---

### 7. Performances

#### Liste lente avec beaucoup d'éléments
**Solutions** :

1. Ajouter `key` dans `LazyColumn` :
```kotlin
items(formations, key = { it.recordId }) { formation ->
    // ...
}
```

2. Implémenter pagination :
```kotlin
@Query("SELECT * FROM formations LIMIT :limit OFFSET :offset")
suspend fun getFormationsPaginated(limit: Int, offset: Int): List<Formation>
```

3. Utiliser `derivedStateOf` pour les calculs :
```kotlin
val filteredFormations by remember {
    derivedStateOf { 
        formations.filter { it.filiere == selectedFiliere }
    }
}
```

---

### 8. Logs & Debugging

#### Activer les logs détaillés

**Retrofit** :
```kotlin
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

**Room** :
```kotlin
Room.databaseBuilder(...)
    .setQueryCallback({ sqlQuery, bindArgs ->
        Log.d("RoomQuery", "SQL: $sqlQuery")
    }, Executors.newSingleThreadExecutor())
    .build()
```

**ViewModels** :
```kotlin
init {
    Log.d("FormationsViewModel", "Initialized")
}
```

---

### 9. Tests

#### Tests unitaires ViewModels

```kotlin
@Test
fun `test loadFormations success`() = runTest {
    val repository = FakeFormationRepository()
    val viewModel = FormationsViewModel(repository)
    
    viewModel.loadFormations()
    
    val state = viewModel.uiState.value
    assertTrue(state is FormationsUiState.Success)
}
```

#### Tests UI Compose

```kotlin
@Test
fun testFormationCard() {
    composeTestRule.setContent {
        FormationCard(
            formation = testFormation,
            onClick = {},
            onFavoriteClick = {},
            isFavorite = false
        )
    }
    
    composeTestRule
        .onNodeWithText("Établissement Test")
        .assertIsDisplayed()
}
```

---

### 10. Optimisations

#### Réduire la taille de l'APK

Dans `build.gradle.kts` :
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

#### Activer R8

```kotlin
android {
    buildFeatures {
        buildConfig = true
    }
}
```

---

## 🔍 Commandes Utiles

### Nettoyer le projet
```bash
./gradlew clean
```

### Build debug
```bash
./gradlew assembleDebug
```

### Installer sur appareil
```bash
./gradlew installDebug
```

### Voir les logs
```bash
adb logcat | grep "Parcoursup"
```

### Inspecter la base de données
```bash
adb shell
run-as com.ensiie.justine.parcoursup
cd databases
sqlite3 parcoursup_database
.tables
SELECT * FROM favorite_formations;
```

---

## 📊 Monitoring

### Performance
```kotlin
// Dans MainActivity
override fun onCreate(savedInstanceState: Bundle?) {
    Trace.beginSection("MainActivity.onCreate")
    super.onCreate(savedInstanceState)
    // ...
    Trace.endSection()
}
```

### Mémoire
Utiliser Android Studio Profiler pour :
- Memory leaks
- CPU usage
- Network traffic

---

## 🆘 Ressources

- [Android Developers](https://developer.android.com/)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Guide](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Guide](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Leaflet JS](https://leafletjs.com/)
- [Now in Android](https://github.com/android/nowinandroid)

---

## 💡 Tips

### 1. Hot Reload Compose
Appuyer sur `Ctrl + R` (Windows) ou `Cmd + R` (Mac) pour recharger sans rebuild complet

### 2. Preview Compose
```kotlin
@Preview(showBackground = true)
@Composable
fun FormationCardPreview() {
    ParcoursupTheme {
        FormationCard(...)
    }
}
```

### 3. Inspect Compose Layout
Activer le Layout Inspector dans Android Studio

### 4. Debug Network
Utiliser [Charles Proxy](https://www.charlesproxy.com/) ou Postman pour tester les endpoints

---

## ✅ Checklist Avant Production

- [ ] Tests unitaires passent
- [ ] Tests UI passent
- [ ] Pas de warnings Lint critiques
- [ ] ProGuard/R8 configuré
- [ ] Permissions minimales
- [ ] Gestion des erreurs réseau
- [ ] Offline mode ou messages d'erreur clairs
- [ ] Pas de clés API en dur
- [ ] Icônes et assets optimisés
- [ ] Support multi-langues (optionnel)
- [ ] Tests sur différentes tailles d'écran
