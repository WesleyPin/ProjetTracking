# ProjetTracking
Projet de tracking - Développement Mobile

1 - Installation GIT : https://git-scm.com/download/win, installer GIT sur sa machine.

2 - Dans Android Studio : 
- File -> Settings -> Version Control -> Git
- Paramétrer le path vers l'executable git dans le dossier créer via l'installation dans l'étape 1.(Faire un test pour savoir si le path est correct).
- Appliquer les modifications et appuyer sur "Ok".
- Une fois ceci fait, aller sur VCS -> Enable Version Control Integration -> Dans la liste déroulante sélectionner GIT et valider.
- Pour récupérer le dépôt git, aller dans VCS -> GIT -> Clone -> Pour l'url cliquer sur le bouton Clone/Download dans github et copier l'url afficher -> Valider.

Wiki Git : (A Compléter/Corriger/etc)

  - Quelques requêtes : 
  
  -  git status, pour voir si on a des changements par rapport à votre branche.
  -  git pull --rebase, pour récupérer les changements sur votre branche.
  -  git commit, pour sauvegarder des modifications apportées au code.
  -  git push, pour pousser vos modifications vers votre branche.
  
  - Quelques conseils : 
  
  -  Dans le cas où le pull --rebase ne fonctionne pas, vous avez surement des modifications en local qui bloque la                              récupération des modifications sur la branche dans ce cas il faut commit vos modifications puis pull --rebase                              et enfin push ou undo votre dernier commit.
  -  Lorsque vous voulez push vos modifications vers la branche il faut vérifier que votre code compile bien pour que                            cela n'impact pas le travail des autres qui vont récupérer vos modifications.
                          
Informations/tutoriel sur l'API LoL :
https://aawebapps.com/tutorials/tutorial1.html

URL pour appel à l'API, récupérer les informations d'un jouer (LoL) : 
https://euw1.api.riotgames.com/lol/summoner/v4/summoners/by-name/PSEUDO?api_key=key

Selon Mme Keller : Voir pour intégrer un widget qui s'actualise toutes les X minutes sur l'écran d'accueil du smartphone.

Selon ce qu'on veut implémenter (sachant qu'on va en avoir besoin à 100% ne serait-ce que pour les icônes de joueur), il va falloir ajouter "Data Dragon" à notre projet. https://developer.riotgames.com/static-data.html
En gros, c'est une sorte de base de données statique du jeu (où sont stockés tous les splash art, icônes de joueur, de spell, ... etc.)
