# Explications

Ce fichier a pour but d'expliquer mes choix et les hypothèses prises.

## Contraintes prises pour l'url
- Elle doit commencer par http ou https (choix arbitraire de ne gérer que ces 2 protocoles pour l'instant)  
- Sa taille doit être comprise entre 15 (choisi arbitrairement) et 2048 (valeur généralement prise en charge par les navigateurs)
- Vu que selon moi l'intérêt de l'exercice n'est pas ici, je ne vérifie pas le format de l'url (hormis le début de celle-ci)

## Algorithme

### Résumé
Pour obtenir une url raccourcie à partir d'une url complète, j'ai fait les choix suivants :

- L'api est publique et ne nécessite pas d'authentification
- L'url est hashee. Cela permet d'obtenir une sortie de longueur égale peu importe la taille de l'url d'entrée et de limiter au maximum le risque de collision.  
- Une sous partie du hash est retourné pour construire l'url raccourcie
- L'url courte retournée commence toujours par le même préfixe puis le hash raccourci est ajouté.
- L'url d'origine est encodée avant d'être enregistrée dans la BDD

### Details

- Pour l'algorithme de hashage, j'ai choisi du SHA256 pour avoir un hash suffisamment long et ainsi pouvoir stocker un très grand nombre de valeurs (même si du md5 aurait très probablement été suffisant). Néanmoins suivant les envies de performance et / ou autres, un autre algorithme de hashage pourrait être choisi.  
- Données sauvegardées en BDD :
  - url d'origine (encodée)
  - hash
  - hash court
- Calculi hash court:
  - celui-ci est créé en ne gardant que les 10 premiers caractères du hash complet
  - si le hash court est déjà présent en BDD pour un autre enregistrement, alors un nouveau hash court est calculé (décalage des index pour création du hash court)
  - s'il n'est pas possible d'obtenir un hash court non déjà utilise, alors une erreur est retournée.

### Erreurs

#### POST

400 :
- le body est manquant
- le body ne contient pas le paramètre url
- le paramètre url ne respecte pas les contraintes (format, taille)

500 :
- Il est impossible de générer un sous hash unique pour une url

#### GET

400 :
- le paramètre shortenedUrl est manquant
- le paramètre shortenedUrl ne respecte pas les contraintes (pattern)

404 :
- L'url raccourcie fournie n'est pas connue


## Stack technique 
Comme demande dans le sujet, le projet utilise JAVA avec le framework SpringBoot.  
L'utilisation de la version 21 de java permet notamment d'utiliser les `String template` ainsi que les `Unnamed Variables`.  

Pour la base de données j'ai fait le choix d'utiliser une `H2` pour permettre des tests plus aises (pas besoin de docker sur son poste). Il est évident que pour un vrai projet une `MySQL/MariaDB/PostgreSQL' serait utilisée.

J'ai hésité à utiliser une base `Redis` a la place d'une BDD relationnelle, mais celle-ci ne correspondait pas totalement à mes contraintes. Néanmoins celle-ci pourrait être utilisée en supplément (gestion de cache par exemple).

Sinon les librairies utilisées sont les librairies que j'utilise de manière régulière pour un projet `JAVA + SpringBoot`.

## Améliorations / Evolutions possibles
- Tests de performances, il est probable que la structure de la base de données (1 seule table) entraine des limitations avec beaucoup d'enregistrements.    
- Limiter les caractères possibles pour l'url, des vérifications seraient à mettre en place pour améliorer la robustesse.  
- Ajouter une information sur la date de création de l'url courte en BDD  
- Purger les url générées au bout d'une certaine période  
- Pour la documentation l'ajout d'un diagramme de sequence avec mermaid par exemple
