 Simulation de l’algorithme d’exclusion mutuelle de Lamport avec détection de pannes
Ce projet simule l’algorithme d’exclusion mutuelle de Lamport dans un environnement distribué où des processus peuvent tomber en panne.
L’application est développée en Java et utilise :

* 10 processus répartis sur des ports différents, jouant le rôle de peers (client & serveur à la fois)
* Un 11ᵉ processus (Watchdog) chargé de surveiller et détecter les processus défaillants
* Des communications via sockets TCP
* Des threads pour la gestion parallèle des requêtes et des délais simulés
* Une horloge logique de Lamport pour le respect de l’ordre causal
* Une section critique simulée avec attente aléatoire
* Une file de requêtes locale à chaque processus gérée via des PriorityQueue

La section critique est protégée à l’aide de requêtes REQUEST, ACK et RELEASE respectant la logique de Lamport avec estampilles.
Le watchdog surveille les processus qui ne répondent plus et peut déclencher une alerte ou une relance.

✨ Une interface graphique (non incluse ici) est prévue pour visualiser en temps réel :

 * L’état des processus
 * Les files d’attente
 * Les estampilles Lamport

📁 Structure du code :

- Processus.java : logique d’un peer Lamport
- LamportClock.java : horloge logique
- RequestHandler.java : gestion des sockets entrants
- Watchdog.java : surveillance des pannes
- Main.java : lancement de la simulation

⚙️ Technologies utilisées : Java, Threads, Sockets, Programmation concurrente
