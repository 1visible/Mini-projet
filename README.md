# **Moteur d'évaluation de requête en étoile**

[![badge_java](https://img.shields.io/badge/Fait%20avec-Java-orange)](https://www.java.com/fr/)  [![badge_um](https://img.shields.io/badge/Projet%20HAI914I%20-Universit%C3%A9%20de%20Montpellier-ff69b4)](https://sciences.edu.umontpellier.fr/)




## Objectifs du projet
- Implémenter l’approche hexastore pour l’interrogation des données RDF, ainsi que
les procédures nécessaires à l’évaluation de requêtes en étoile.  
- Évaluer et analyser les performances de notre système en le comparant à Jena à l’aide
du benchmark WatDiv.


## Utilisation
Notre programme possède 3 points d'entrée :
* *Filtrage des requêtes*

Il permet de diviser toutes nos requêtes en plusieurs jeux de requêtes afin d'être le plus pertinent pour notre benchmark.
Ces derniers seront écrits dans le dossier [filtered](/filtered).

````shell
java -jar queriesfilter.jar -queries [path to file or folder] -data [path to file] -verbose true
````

* *Traitement des requêtes avec notre système*

````shell
java -jar rdfengine.jar -queries [path to file or folder] -data [path to file] -output [path to file] -verbose true
````

* *Traitement des requêtes avec Jena*

````shell
java -jar jena.jar -queries [path to file or folder] -data [path to file] -output [path to file] -verbose true
````

**Les arguments :**
* queries : chemin vers un fichier ou un dossier de requête (.queryset)
* data : chemin vers un fichier de donnée (.nt)
* output : chemin vers un fichier csv où seront écrit les métadonnées d'exécution
* verbose : true ou false, pour activer ou désactiver les logs console

Les résultats du benchmark de notre système et de celui de Jena sont disponibles [ici](/benchmark_results.xlsx) et notre rapport final [ici](/Rapport_final_-_DUCHON_LAURET.pdf).

## Auteurs

* **Damien Duchon** _alias_ [@damdcn](https://github.com/damdcn)
* **Nicolas Lauret** _alias_ [@1visible](https://github.com/1visible)
