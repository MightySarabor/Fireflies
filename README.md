#Softwarearchitektur für Enterprises

## Aufgabe 1 „Zaghafte erste Glühwürmchen“ 

In dieser Übung implementieren Sie eine Simulation von synchronisierten Glühwürmchen in einer Programmiersprache Ihrer 
Wahl. Ihre Aufgabe besteht darin, ein einzelnes Programm zu erstellen, das mehrere als Threads implementierte Glühwürmchen 
in einer Torus-Struktur anordnet und simuliert. Der Zustand jedes Glühwürmchen soll durch ein farbiges Rechteck in einem 
Fenster dargestellt werden. Die Simulation soll den Prozess erfassen, bei dem benachbarte Glühwürmchen sich gegenseitig 
beeinflussen und ihre Aufleuchtzyklen gemäß dem Kuramoto-Modell allmählich synchronisieren. Dieses Modell verwendet 
einfache Regeln zur Synchronisation und Nachbarschaftsinteraktion, bei denen jedes Glühwürmchen vom Zustand seiner 
unmittelbaren Nachbarn im Torus beeinflusst wird. Ziel ist es, eine einfache und intuitive Benutzeroberfläche zu erstellen, die die 
Torus-Anordnung und den Synchronisationsprozess visualisiert, wobei Farbe oder Helligkeit verwendet werden, um den 
Aufleuchtzyklus jedes Glühwürmchens darzustellen. 

Die Abgabe den Code der ersten Aufgabe finden Sie im Master-Branch.



### Die Reise des Glühwürmchens

Das Glühwürmchen wird mit festen Parametern erstellt, die für die Synchronisation entscheidend sind. Die Phase wird zufällig initialisiert, um Varianz in das Experiment zu bringen:

```java
this.phase = new Random().nextDouble() * 2 * Math.PI;  // Zufällige Startphase zwischen 0 und 2π
```

Verbindung zum Server
Das Glühwürmchen stellt eine Verbindung zum Server her:

```java
try (TTransport transport = new TSocket("localhost", 9090)) {
    transport.open();
    FireflyService.Client client = new FireflyService.Client(new TBinaryProtocol(transport));
```
Synchronisation und Phasenanpassung
Das Glühwürmchen geht dann in eine Endlosschleife, in der es sich mit der Zeit mit den anderen Glühwürmchen synchronisiert. Zunächst fragt es die Zustände seiner Nachbarn ab:

```java
int[] neighborStates = client.getNeighborStates(gridX, gridY).stream().mapToInt(Integer::intValue).toArray();
```
Die Methode getNeighborStates ist eine Methode der Schnittstelle, die mit Apache Thrift erstellt wurde.

Mit den Informationen über die Nachbarn kann das Glühwürmchen jetzt die Berechnung durchführen, um seine eigene Phase anzupassen.

Phasenfortschritt
```java
currentPhase += (2 * Math.PI / PERIOD) * TIME_STEP;
if (currentPhase >= 2 * Math.PI) {
    currentPhase -= 2 * Math.PI;
}
```
Dieser Abschnitt beschreibt den regelmäßigen Phasenfortschritt:

PERIOD bestimmt die Geschwindigkeit der Phasenänderung. Eine kleinere PERIOD führt zu einer schnelleren Phasenänderung, während eine größere PERIOD zu einer langsameren Phasenänderung führt.

TIME_STEP bestimmt die Granularität der Aktualisierung. Ein kleinerer TIME_STEP führt zu feineren Phasenänderungen, während ein größerer TIME_STEP zu gröberen Phasenänderungen führt.

Sobald die Phase den Wert 2 * π überschreitet, wird sie zurückgesetzt, um im Bereich [0, 2π] zu bleiben.

Anpassung basierend auf Nachbarn
```java
// Synchronisation mit Nachbarn
double phaseAdjustment = 0.0;
for (int state : neighborStates) {
    double neighborPhase = stateToPhase(state);
    phaseAdjustment += Math.sin(neighborPhase - currentPhase);
    //System.out.println("Nachbar Zustand: " + state + ", Phase: " + neighborPhase);
}

double adjustment = ADJUSTMENT_FACTOR * (phaseAdjustment / neighborStates.length);
currentPhase += adjustment;
```
In diesem Abschnitt:

Die Phasenanpassung wird auf 0 initialisiert.

Dann wird über die Nachbarn iteriert, und die Differenzen der Phasen werden berechnet. Diese Differenzen werden aufsummiert.

Die summierte Anpassung wird durch die Anzahl der Nachbarn geteilt, um die durchschnittliche Anpassung zu erhalten.

ADJUSTMENT_FACTOR wird verwendet, um die Stärke der Anpassung zu steuern. Ein höherer Wert führt zu einer stärkeren Anpassung, während ein niedrigerer Wert zu einer schwächeren Anpassung führt.

Die berechnete Anpassung wird dann zur aktuellen Phase addiert.

Damit verändert sich die Phase des Glühwürmchens kontinuierlich in Abhängigkeit von den Phasen der Nachbarn.

Sicherstellung der Phasengrenzen
```java
if (currentPhase < 0) {
    currentPhase += 2 * Math.PI;
} else if (currentPhase >= 2 * Math.PI) {
    currentPhase -= 2 * Math.PI;
}
```

Zum Schluss wird sichergestellt, dass die Phase des Glühwürmchens innerhalb der Grenzen [0, 2π] bleibt. Dies stellt sicher, dass die Phasenwerte immer im gültigen Bereich liegen.


## Aufgabe 2 „Kommunizierende Glühwürmchen“  

In dieser Übung erweitern Sie Ihre Glühwürmchen-Simulation aus Aufgabe 1, indem Sie jedes Glühwürmchen als eigenständiges 
Programm realisieren, das über ein Netzwerk mit den anderen Glühwürmchen kommuniziert. Ziel ist es, die notwendigen 
Zustandsinformationen zwischen den einzelnen Glühwürmchen mithilfe eines Kommunikationsprotokolls wie Apache Thrift 
oder gRPC auszutauschen, um den Synchronisationsprozess zu steuern. Jedes Glühwürmchen fungiert als unabhängiger Prozess 
und tauscht regelmäßig Statusinformationen mit seinen Nachbarn in der Torus-Anordnung aus, um eine Synchronisation zu 
erreichen. Wenn Sie die Programmiersprache Java verwenden, ist alternativ auch Java RMI als Kommunikationsmechanismus 
zulässig. Die Aufgabe besteht darin, die dezentrale Natur der Simulation zu verdeutlichen und sicherzustellen, dass die 
Glühwürmchen auf Basis der empfangenen Informationen den Synchronisationsprozess wie zuvor initiieren können. 

Für die verteilte Struktur habe ich mir folgdenes überlegt:

Ich habe einen zentralen Server 'ServerUI'. Dann habe ich Die Glühwürmchen, ein Glühwürmchen ist ein 'FireflyClient'. Dann habe ich den 'ClientHandler', dieser startet lediglich die FireflyClients. Und zu guter letzt habe ich mit Apache Thrift einen FireflyService erstellt, der ein Framework für die verteilte Struktur liefert.

Als UML Diagramm sehen die Klassen wie folgt aus:
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

Für die Kommunikation zwischen Server und Client wurden in Apache Thrift zwei zentrale Methoden definiert:

```java
namespace java firefly

service FireflyService {
    void sendState(1: i32 clientId, 2: i32 state),
    list<i32> getNeighborStates(1: i32 gridX, 2: i32 gridY),
}```
Diese Methoden sind notwendig, um den Server über den aktuellen Zustand der Glühwürmchen auf dem Laufenden zu halten und dem Glühwürmchen den Zugriff auf die Zustände seiner Nachbarn zu ermöglichen. Der Server speichert die Zustände der Glühwürmchen in einer ConcurrentHashMap.

```java
private final ConcurrentHashMap<Integer, Integer> clientStates = new ConcurrentHashMap<>();
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
In dieser Methode werden die Positionen der Nachbarn des Glühwürmchens basierend auf gridX und gridY berechnet, einschließlich diagonaler Nachbarn. Diese Positionen werden dann verwendet, um die IDs der Nachbarn zu ermitteln.

Nach jeder Phasenberechnung aktualisiert das Glühwürmchen seine Werte mithilfe der Methode sendState, um die Zustände aktuell zu halten.

```java
for (int[] pos : positions) {
    int neighborId = pos[0] * GRID_SIZE + pos[1];
    neighbors.add(clientStates.getOrDefault(neighborId, 0));
}
return neighbors;
}
```
Im letzten Schritt der Methode getNeighborStates wird die Liste neighbors mit den Zuständen der Nachbarn gefüllt. Diese Zustände werden vom Glühwürmchen mit getNeighborStates abgefragt, um die eigene Phase basierend auf den Zuständen der Nachbarn anzupassen.
