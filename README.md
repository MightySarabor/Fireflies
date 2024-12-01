# Softwarearchitektur für Enterprises

## Aufgabe 1 „Zaghafte erste Glühwürmchen“ 

Die Abgabe den Code der ersten Aufgabe finden Sie im Master-Branch.

### UML-Diagramm
```
---------------------------------
|            Torus              |
---------------------------------
| - grid: Firefly[][]           |
| - size: int                   |
---------------------------------
| + Torus(size: int)            |
| + update()                    |
| + getGrid(): Firefly[][]      |
| - initializeNeighbors()       |
---------------------------------
                ^
                |
---------------------------------
|     FireflySimulation         |
---------------------------------
| + start(primaryStage: Stage)  |
| + main(args: String[])        |
| - update(rectangles:          |
|   Rectangle[][])              |
---------------------------------
                ^
                |
---------------------------------
|           Firefly             |
---------------------------------
| - phase: double               |
| - neighbors: List<Firefly>    |
| - isFlashing: boolean         |
| - flashTimeRemaining: double  |
| + Firefly()                   |
| + addNeighbor(neighbor:       |
|   Firefly)                    |
| + updatePhase()               |
| + isFlashing(): boolean       |
| + getPhase(): double          |
---------------------------------
```
### Die Reise des Glühwürmchens

Die Klasse `FireflySimulation` bildet das **Herzstück des Programms**. Sie stellt die Glühwürmchen in einem **Grid** dar, wobei jedes Feld ein Glühwürmchen repräsentiert. Jedes Glühwürmchen besitzt Methoden, um zu bestimmen, ob es aktuell blinkt oder nicht. 

### Phasenbasierte Logik
- **Phase**: Ein Wert zwischen `0` und `2π`, der den Zustand des Glühwürmchens beschreibt.
- **Schwellwert**: Entscheidet, ob das Glühwürmchen blinkt.
- **Initialisierung**: Jedes Glühwürmchen startet mit einer zufällig generierten Phase. Dadurch wird beim Programmstart eine gewisse Varianz gewährleistet.
- **Nachbarn**: Jedes Glühwürmchen verfügt über eine Liste von Nachbarn, mit denen es interagieren kann.

### Phasenfortschritt
In jeder Iteration wird die Phase des Glühwürmchens aktualisiert. Der Fortschritt wird durch folgende Berechnung gesteuert: 

```java
currentPhase += (2 * Math.PI / PERIOD) * TIME_STEP;
if (currentPhase >= 2 * Math.PI) {
    currentPhase -= 2 * Math.PI;
}
```
- **PERIOD**: Bestimmt die Geschwindigkeit der Phasenänderung. 
  - Eine kleinere `PERIOD` führt zu einer schnelleren Phasenänderung.
  - Eine größere `PERIOD` resultiert in einer langsameren Phasenänderung.

- **TIME_STEP**: Legt die Granularität der Aktualisierung fest.
  - Ein kleinerer `TIME_STEP` ermöglicht feinere Phasenänderungen.
  - Ein größerer `TIME_STEP` bewirkt gröbere Phasenänderungen.

- **Phasenbegrenzung**: Sobald die Phase den Wert `2π` überschreitet, wird sie zurückgesetzt, um im Bereich `[0, 2π]` zu bleiben.

- **Anpassungsfaktor**: Nach dem Fortschritt wird die Phase leicht angepasst, um Synchronisation zu ermöglichen. 
  - Der **Anpassungsfaktor** (`ADJUSTMENT_FACTOR`) steuert die Stärke dieser Anpassung.
  - Ein höherer Wert bewirkt eine schnellere Anpassung, ein niedrigerer Wert sorgt für langsamere Anpassung.


```java
// Synchronisation mit Nachbarn
double phaseAdjustment = 0.0;
        for (Firefly neighbor : neighbors) {
            phaseAdjustment += Math.sin(neighbor.phase - this.phase);
        }

        phaseAdjustment *= syncRate / neighbors.size();
        this.phase += ADJUSTMENT_FACTOR * phaseAdjustment / neighbors.size();
```
Die Phasenanpassung wird auf `0` initialisiert. Anschließend wird wie folgt vorgegangen:

1. **Iteration über Nachbarn**: Die Differenzen zwischen den Phasen des aktuellen Glühwürmchens und seiner Nachbarn werden berechnet und aufsummiert.
2. **Berechnung der durchschnittlichen Anpassung**: Die summierte Anpassung wird durch die Anzahl der Nachbarn geteilt.
3. **Steuerung der Anpassungsstärke**: Der `ADJUSTMENT_FACTOR` reguliert die Stärke der Phasenanpassung. Ein höherer Wert bewirkt eine stärkere Anpassung, ein niedrigerer eine schwächere.
4. **Aktualisierung der Phase**: Die berechnete Anpassung wird zur aktuellen Phase hinzugefügt.

Diese Schritte sorgen dafür, dass die Phase des Glühwürmchens kontinuierlich angepasst wird, basierend auf den Phasen seiner Nachbarn.

Zusätzlich überprüft das Glühwürmchen, ob es aktuell blinken soll. Dabei sorgt `flashTimeRemaining` dafür, dass das Glühwürmchen unabhängig von der Phase weiterleuchtet, solange dieser Wert positiv ist.


### Beispiel

So sieht das ganze dann aus:

https://github.com/user-attachments/assets/2f827170-6169-4153-8c73-86d2c0d5fbe3

## Aufgabe 2 „Kommunizierende Glühwürmchen“  

Den Code finden Sie im Brach distrFireflies

Für die verteilte Struktur habe ich folgendes Konzept entwickelt:

1. **Zentraler Server**: Der zentrale Server (`ServerUI`) übernimmt die Verwaltung der Glühwürmchen und ihrer Zustände.
2. **Glühwürmchen-Clients**: Jedes Glühwürmchen wird als eigenständiger Prozess (`FireflyClient`) ausgeführt.
3. **ClientHandler**: Diese Komponente startet lediglich die `FireflyClient`-Instanzen.
4. **Apache Thrift**: Mit Apache Thrift wurde ein `FireflyService` erstellt, der die Kommunikation zwischen Server und Clients ermöglicht.

Das UML-Diagramm der Klassenstruktur sieht wie folgt aus:


```
+------------------+            +-----------------+
| ClientHandler    |            | FireflyClient   |
|------------------|            |-----------------|
| + clients        |<>----------| + id            |
|                  |            | + gridX         |
|                  |            | + gridY         |
|------------------|            | + phase         |
| + startClients() |            |-----------------|
| + main()         |            | + run()         |
+------------------+            | + updatePhase() |
                                | + phaseToState()|
+------------------+            | + stateToPhase()|
| ServerUI         |<>----------+-----------------+
|------------------|            
| + clientStates   |            
| + grid           |            
|------------------|            
| + start()        |            
| + startThriftServer() |       
| + updateGrid()   |
| + sendState()    |
| + getNeighborStates()|            
+------------------+
      |
      |Interface
      |
      V
+------------------+
| FireflyService   |
|------------------|
| + sendState()    |
| + getNeighborStates()|
+------------------+

```


### Kommunikation mit dem Server

Die Grundidee des Glühwürmchens bleibt unverändert. Der Unterschied besteht darin, dass:

- **Jedes Glühwürmchen nun ein eigener Prozess ist**, der unabhängig von der UI läuft.
- **Zentrale Verwaltung entfällt**: Statt zentral von einer Klasse ausgeführt zu werden, meldet sich jedes Glühwürmchen einmalig beim Server an.
- **Server übernimmt Verwaltung**: Der Server speichert die Phasen aller Glühwürmchen.
- **Nachbarn über Server abrufbar**: Jedes Glühwürmchen kann seine Nachbarn vom Server abfragen.
- **Phase wird iterativ gesendet**: In jeder Iteration aktualisiert das Glühwürmchen seine Phase und sendet diese an den Server.

#### Änderungen gegenüber dem Monolithen

Die zentrale Struktur wurde aufgelöst. Die Kommunikation zwischen Server und Client erfolgt nun über Apache Thrift. Dabei wurden zwei zentrale Methoden definiert:

```java
namespace java firefly

service FireflyService {
    void sendState(1: i32 clientId, 2: i32 state),
    list<i32> getNeighborStates(1: i32 gridX, 2: i32 gridY),
}
```
Diese Methoden sind notwendig, um:

1. **Den Server auf dem Laufenden zu halten**: `sendState` informiert den Server über den aktuellen Zustand der Glühwürmchen.
2. **Zustände der Nachbarn bereitzustellen**: `getNeighborStates` ermöglicht es dem Glühwürmchen, die Zustände seiner Nachbarn abzufragen.

Der Server speichert die Zustände der Glühwürmchen in einer `ConcurrentHashMap`, um gleichzeitige Zugriffe sicher zu handhaben.

```java
try (TTransport transport = new TSocket("localhost", 9090)) {
    transport.open();
    FireflyService.Client client = new FireflyService.Client(new TBinaryProtocol(transport));
```

Die Methode getNeighborStates, die vom Glühwürmchen aufgerufen wird, ist im FireflyHandler definiert:

```java
@Override
public List<Integer> getNeighborStates(int gridX, int gridY) {
    List<Integer> neighbors = new ArrayList<>();
    int[][] positions = {
        {gridX, (gridY + 1) % GRID_SIZE},  // right
        {gridX, (gridY - 1 + GRID_SIZE) % GRID_SIZE},  // left
        {(gridX + 1) % GRID_SIZE, gridY},  // down
        {(gridX - 1 + GRID_SIZE) % GRID_SIZE, gridY},  // up
        {(gridX + 1) % GRID_SIZE, (gridY + 1) % GRID_SIZE},  // down-right
        {(gridX + 1) % GRID_SIZE, (gridY - 1 + GRID_SIZE) % GRID_SIZE},  // down-left
        {(gridX - 1 + GRID_SIZE) % GRID_SIZE, (gridY + 1) % GRID_SIZE},  // up-right
        {(gridX - 1 + GRID_SIZE) % GRID_SIZE, (gridY - 1 + GRID_SIZE) % GRID_SIZE}  // up-left
    };
```
In dieser Methode werden die Positionen der Nachbarn des Glühwürmchens auf Basis von `gridX` und `gridY` berechnet. Dabei werden auch die **diagonalen Nachbarn** berücksichtigt. Die berechneten Positionen dienen anschließend zur Ermittlung der Nachbar-IDs.

```java
for (int[] pos : positions) {
    int neighborId = pos[0] * GRID_SIZE + pos[1];
    neighbors.add(clientStates.getOrDefault(neighborId, 0));
}
return neighbors;
}
```
Im letzten Schritt der Methode `getNeighborStates` wird die Liste `neighbors` mit den Zuständen der Nachbarn gefüllt. Die Zustände werden direkt aus der `ConcurrentHashMap` des Servers abgerufen:

### Beispiel
Am Ende sieht das so aus:

https://github.com/user-attachments/assets/e2795918-62e7-4ca4-a207-decdb732a238

### Überraschendes

Vielleicht nicht überraschend für erfahrene Programmierer, aber überraschend für mich war, dass ab einer bestimmten Anzahl (bei mir ab 7) es zu Problemen mit der Serverconnection kam. Ich vermute Raceconditions bei den Clients. Das Ergebnis ist ein lückenhaftes Grid.


https://github.com/user-attachments/assets/358aaa67-02dd-4f4b-8b06-e4d16446a3b5

Versuche das ganze zu korrigieren mit mehrfachen oder verzögerten Verbindungsversuchen hat dazu geführt, dass kein synchrones Bild mehr entstanden ist, sondern die Würmchen in Wellen geleuchtet haben. Eine kurze Verzögerung bei der Initialisierung scheint dafür zu sorgen. 

Ein Lönsungsansatz dafür wäre die Glühwürmchen erst starten zu lassen, wenn alle Clienten verbunden sind.
