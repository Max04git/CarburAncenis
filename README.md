# Carbur'Ancenis
Application Android (Kotlin Jetpack Compose)

L'application permet de comparer les prix des carburants entre plusieurs stations autour d'une ville choisie.

## Fonctionnalités
- **Recherche de ville** (suggestions Nominatim)
- **Favoris** : ajout de ville en favoris
- **Filtrage** : type de carburant, rayon (en km), date de mise à jour des prix
- **Affichage** : liste des stations avec prix, adresse, distance et date de mise à jour

## Stack technique
- **Kotlin**
- **Jetpack Compose (Material 3)**
- **Architecture** : MVVM (ViewModel + StateFlow)
- **Réseau** : Retrofit + OkHttp
- **Sérialisation** : kotlinx.serialization
- **Persistance** : DataStore Preferences

## APIs
- Prix des carburants : http://api.prix-carburants.2aaz.fr/
- Géocodage (coordonnées des villes) : https://nominatim.openstreetmap.org/

## Architecture (vue d'ensemble)

### UI (Compose)
- HomePage.kt : écran principal (drawer recherche + liste stations + filtres)
- L'UI observe les StateFlow exposés par le ViewModel via collectAsStateWithLifecycle().

### ViewModel (MVVM)
- HomeViewModel.kt
  - Centralise l'état : ville courante, suggestions, favoris, filtres, liste des stations.
  - Déclenche les mises à jour quand les paramètres changent.

### Data layer
- PrixCarburantsRepository : récupère les stations/prix depuis l'API.
- GeocodingRepository : suggestions de villes (Nominatim).
- UserPreferences : DataStore.

### Modèles
- data/model : modèles partagés (FavoriteCity, GeocodedCity).
- ui : modèles d'affichage (StationUi).

## Flux de données (data flow)
1. L'utilisateur choisit une ville (via la recherche ou les favoris).
2. La ville est persistée dans UserPreferences.
3. HomeViewModel observe les préférences (ville + filtres) et relance un chargement.
4. PrixCarburantsRepository appelle l'API.
5. L'UI se met à jour automatiquement via les StateFlow.

## Persistance
Les préférences utilisateur sont stockées dans **DataStore Preferences** :
- Ville courante
- Liste des villes favorites
- Carburant, distance, date de mise à jour max

## Limitations
- Nominatim est un service public : éviter les appels trop fréquents (rate limit). La recherche utilise des suggestions à partir de 2 caractères.

