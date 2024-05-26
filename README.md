## Verteilte Systeme Übung 1
### Ergebnisbericht

Dieses Dokument enthält sowohl eine kurze Anleitung zum Setup und einigen
Implementierungsentscheidungen, als auch die Auswertung und Aufwand der Experimente.

#### Implementierung und Experimentelles Setup

Das Projekt wurde als Maven-Projekt mit Java 17 implementiert und nutzt Google Gson zur Serialisierung/ Aufzeichung
der Spielrunden. Die Klasse `GameMaster` erwartet zwei Kommandozeilen parameter: `PORT` und `DAUER_DER_RUNDE`
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
java -jar Player.jar 192.168.8.112 1234 Player1 3
```
Dabei werden folgender Kommandozeilenparameter übergeben: `IP`, `PORT`, `NAME`, `SPIELER_LATENZ`.

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
Alle Spielteilnehmer befanden ich in meinem Heimnetzwerk. Selbst bei einer relativ geringen Anzahl an
Spielern gestaltete sich die Ausführung  mühsam, da alle Spieler zunächst konfiguriert und
anschließend sequenziell von Hand auf den jeweiligen Rechnern gestartet werden mussten. Somit
ist is also fast unvermeidbar, dass die ersten 1-2 Spielrunden auf Grund der Verzögerung
nicht repräsentativ sind.

Um genügend auswertbare Ergenisse zu erhalten, habe ich die Spieler ca. 10 Runden spielen lassen.
Wie erwartet, waren in den ersten beiden Runden noch nicht alle Spieler angemeldet und daher sind
diese zu vernachlässigen. Dennoch ist in Runden 5 und 8 der Fall aufgetreten, dass durch die Latenz
von Spieler C sein Wurf nicht der entsprechenden Runde zugeordet wurde (es existiert hier
einfach keine Submission für Spieler C). Dementsprechend wurde beispielsweise sein Wurf vom
Server erst in Runde 6 erfasst und als solcher zugeordnet.

### Aufgabe 2c)

