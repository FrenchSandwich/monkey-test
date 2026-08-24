# Tractor Application

## Packages
L'app et le code sont découpés en 3 couches : web, service et configuration. 

## Multi-tracteurs et collisions
Un `TractorFleet` gère plusieurs `TractorService`, chacun avec son propre thread worker. Le seul état
partagé entre threads est l'`OccupancyGrid` : toute décision de déplacement (case libre, dans la grille)
passe par un unique verrou (`ReentrantLock`), ce qui garantit que deux tracteurs ne peuvent jamais
occuper la même case même en cas de mouvements concurrents. Voir `OccupancyGridTest` pour un test qui
reproduit la course entre threads et vérifie l'invariant, et `requests-collisions.http` pour un scénario
manuel.

## IA
J'ai utilisé l'IA pour : 
* générer les requêtes .http à la racine du projet
* m'aider à utiliser les bons Objets pour la gestion des threads dans le `TractorService`
* générer l'exception handler de la couche web.
* générer le parser d'instructions et les regex qui viennent de la requête http.
* concevoir le passage à plusieurs tracteurs (`TractorFleet`, `OccupancyGrid`) et le test de course associé.