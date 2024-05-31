## Verteilte Systeme Übung 1
### Ergebnisbericht

Dieses Dokument enthält sowohl eine kurze Anleitung zum Setup und einigen
Implementierungsentscheidungen, als auch die Auswertung und Aufwand der Experimente.

#### Implementierung und Experimentelles Setup

Das Projekt wurde als Maven-Projekt mit Java 17 implementiert und nutzt Google Gson zur Serialisierung/ Aufzeichung
der Spielrunden. Die Klasse `GameMaster` erwartet zwei Kommandozeilenparameter: `PORT` und `DAUER_DER_RUNDE`
(in Sekunden). Sollte nichts angegeben werden, wird per default Port 12345 und 10s verwendet.

Das Projekt wurde so implementiert, dass  der Befehl
```console
mvn package
```
eine `JAR` Datei mit der Klasse `Player` als Main-Klasse erzeugt.
Dementsrechend habe ich bei den Experimenten auf dem Host die Klasse `GameMaster` aus der IDE heraus
ausgeführt und die `Player.jar` von verschiedenen Systemen aus mit dem Host kommunizieren lassen.
Um `Player.jar` auf dem jeweiligen System auszuführen, kann folgender Befehl genutzt werden.
```console
java -jar Player.jar 192.168.8.112 12345 Player1 3
```
Dabei werden folgende Kommandozeilenparameter übergeben: `IP`, `PORT`, `NAME`, `SPIELER_LATENZ`.
Für die Ausführung von Aufgabe `2a` bespielsweise sollte der `GameMaster` in package `task2a` 
gestartet werden und dementsprechend im maven-shade-plugin die Klasse `task2a.Player` als 
Main-Klasse gesetzt werden, um die korrekte Version der `Player.jar` passend zur Aufgabe zu erzeugen.

__Wichtig:__ Um Probleme zu vermeiden, sollte der `GameMaster` zuerst gestartet werden.

### Aufgabe 2a)

Der Code zu jeweils `GameMaster` und `Player` unter Verwendung der lokalen Uhren befindet sich in
package `task2a`. Im package `resources.task2a` befindet sich eine Datei `game_rounds.json`, 
in der die Spielergebnisse für mehrere Runden geloggt sind. Für ein experimentelles Setup habe 
ich folgende Parameter verwendet:
- Anzahl der Spieler: 3
- Dauer einer Runde: 10 Sekunden
- Maximale Latenz Spieler A: 5 Sekunden
- Maximale Latenz Spieler B: 10 Sekunden
- Maximale Latenz Spieler C: 15 Sekunden

Die Parameter wurden so gewählt, dass prinzipiell falsche Zuordnungen von Wurf und Runde auftreten können.
Alle Spielteilnehmer befanden sich in meinem Heimnetzwerk. Selbst bei einer relativ geringen Anzahl an
Spielern gestaltete sich die Ausführung  mühsam, da alle Spieler zunächst konfiguriert und
anschließend sequenziell von Hand auf den jeweiligen Rechnern gestartet werden mussten. Somit
ist es also fast unvermeidbar, dass die ersten 1-2 Spielrunden auf Grund der Verzögerung
nicht repräsentativ sind.

Um genügend auswertbare Ergenisse zu erhalten, habe ich die Spieler ca. 10 Runden spielen lassen.
Wie erwartet waren in den ersten beiden Runden noch nicht alle Spieler angemeldet und daher sind
diese zu vernachlässigen. Dennoch ist in Runden 5 und 8 der Fall aufgetreten, dass durch die Latenz
von Spieler C sein Wurf nicht der entsprechenden Runde zugeordet wurde (es existiert hier
einfach keine Submission für Spieler C). Dementsprechend wurde beispielsweise sein Wurf vom
Server erst in Runde 6 erfasst und als solcher zugeordnet. Somit entsteht ein fortlaufender Fehler
auf Seite des Clients, da er bei hoher Latenz das Startsignal des Servers verpasst und somit 
insgesamt weniger Submissions abgibt als insgesamt Runden gespielt wurden.

### Aufgabe 2b)

Ähnlich wie bei Aufgabe `2a` dauert es auch hier 2 Runden bis alle Spieler registriert waren.
Ab Runde 3 war dann zu beobachten, dass auf Grund der Lamport-Zeit klar zu erkennen war, in welcher
Reihenfolge die jeweiligen Submissions beim Server eintrafen. Auch hier trat in Runden 5 und 8
der Fall auf, dass die Submission von Spieler C nicht rechtzeitig zum Rundenende beim Sever angekommen
war (höchstwahrscheinlich durch Pseudo-Randomness auf Client-Seite). Da durch ein `receiveEvent` seitens des Servers eine Maximums-Bildung gemäß der
Lamport-Clock erfolgt, ist im Zeitlog selbt nicht mehr nachvollziehbar, zu welcher
Runde die zurückgebliebene Nachricht gehört. Um das zu beheben müsste man zusätzlich
Server-intern aufzeichnen, zu welchem Intervall der Lamport-Zeit eine jeweilige Runde
stattgefunden hat und vor der Bildung des Maximums die Nachricht der jeweiligen Runde
zuordnen.

### Aufgabe 2c)

Für diesen Aufgabenteil hatte ich geplant, 50 Spieler-Instanzen über `localhost` spielen
zu lassen. Allerdings ist hier bereits beim Start des 23ten Spielers die JVM auf meinem
lokalen System an ihre Grenzen gestoßen (The paging file is too small for this operation to complete).
Aber selbst mit nur 22 Spielern hat es gut 15 Spielrunden gedauert (150s), bis alle Spieler
händisch über das CLI gestartet wurden. Für eine grobe Übersicht der Sielergebnisse befindet
sich im package `resources.task2c` das ensprechende Log (es wurden hier ca. 50 Runden gespielt).
Bereits bei diesem experimentellen Setup wird schnell klar, dass sich das Setup von größeren
Systemen von Hand schnell als sehr aufwending und fehleranfällig gestaltet. Hinzu kommt noch,
dass es sich hierbei um lokale Spieler-Instanzen handelt. Sollte dieses Experiment im größeren
Ausmaß auf physisch verschiedenen Rechnern ausgeführt werden, würde sich sowohl der Aufwand als
auch die Fehleranfälligkeit um ein Vielfaches erhöhen.


