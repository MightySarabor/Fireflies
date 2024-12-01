# Softwarearchitektur für Enterprises

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

Die Klasse FireflySimulation bildet das Herzstück des Programms und stellt die Glühwürmchen in einem Grid jeweils als Feld dar, das blinkt oder nicht.
Jedes Feld führt ist ein Glühwürmchen und führt dessen Methoden aus, um festzustellen, ob es aktuell blinken muss oder nicht. Das wird anhand der Phase, einem Wert zwischen 0 und 2 Pi und einem Schwellwert gemacht.
Wenn das Glühwürmchen erstellt wird, startet es in einer zufälligen Phase, damit garantieren wir etwas Varianz, wenn wir das Programm starten. Außerdem hat jedes Glühwürmchen eine Liste mit Nachbarn

In jeder Iteration haben wir einen Phasenfortschritt. 

Phasenfortschritt
```java
currentPhase += (2 * Math.PI / PERIOD) * TIME_STEP;
if (currentPhase >= 2 * Math.PI) {
    currentPhase -= 2 * Math.PI;
}
```
PERIOD bestimmt die Geschwindigkeit der Phasenänderung. Eine kleinere PERIOD führt zu einer schnelleren Phasenänderung, während eine größere PERIOD zu einer langsameren Phasenänderung führt.

TIME_STEP bestimmt die Granularität der Aktualisierung. Ein kleinerer TIME_STEP führt zu feineren Phasenänderungen, während ein größerer TIME_STEP zu gröberen Phasenänderungen führt.

Sobald die Phase den Wert 2 * π überschreitet, wird sie zurückgesetzt, um im Bereich [0, 2π] zu bleiben.

Nach Phasenfortschritt wird die Phase weiterhin leicht angepasst. Der Anpassungsfaktor kann mit ADJUSTMENT_FACTOR gesetzt werden. Je höher, desto schnellere Anpassung.

```java
// Synchronisation mit Nachbarn
double phaseAdjustment = 0.0;
        for (Firefly neighbor : neighbors) {
            phaseAdjustment += Math.sin(neighbor.phase - this.phase);
        }

        phaseAdjustment *= syncRate / neighbors.size();
        this.phase += ADJUSTMENT_FACTOR * phaseAdjustment / neighbors.size();
```
In diesem Abschnitt:

Die Phasenanpassung wird auf 0 initialisiert.

Dann wird über die Nachbarn iteriert, und die Differenzen der Phasen werden berechnet. Diese Differenzen werden aufsummiert.

Die summierte Anpassung wird durch die Anzahl der Nachbarn geteilt, um die durchschnittliche Anpassung zu erhalten.

ADJUSTMENT_FACTOR wird verwendet, um die Stärke der Anpassung zu steuern. Ein höherer Wert führt zu einer stärkeren Anpassung, während ein niedrigerer Wert zu einer schwächeren Anpassung führt.

Die berechnete Anpassung wird dann zur aktuellen Phase addiert.

Damit verändert sich die Phase des Glühwürmchens kontinuierlich in Abhängigkeit von den Phasen der Nachbarn.

Hier checkt das Glühwürmchen, ob es aktuell Blinken soll. Mit flashTimeRemaining leuchtet es ungeachtet der Phase weiter. 

### Beispiel

So sieht das ganze dann aus:

https://github.com/user-attachments/assets/2f827170-6169-4153-8c73-86d2c0d5fbe3

## Aufgabe 2 „Kommunizierende Glühwürmchen“  

In dieser Übung erweitern Sie Ihre Glühwürmchen-Simulation aus Aufgabe 1, indem Sie jedes Glühwürmchen als eigenständiges 
Programm realisieren, das über ein Netzwerk mit den anderen Glühwürmchen kommuniziert. Ziel ist es, die notwendigen 
Zustandsinformationen zwischen den einzelnen Glühwürmchen mithilfe eines Kommunikationsprotokolls wie Apache Thrift 
oder gRPC auszutauschen, um den Synchronisationsprozess zu steuern. Jedes Glühwürmchen fungiert als unabhängiger Prozess 
und tauscht regelmäßig Statusinformationen mit seinen Nachbarn in der Torus-Anordnung aus, um eine Synchronisation zu 
erreichen. Wenn Sie die Programmiersprache Java verwenden, ist alternativ auch Java RMI als Kommunikationsmechanismus 
zulässig. Die Aufgabe besteht darin, die dezentrale Natur der Simulation zu verdeutlichen und sicherzustellen, dass die 
Glühwürmchen auf Basis der empfangenen Informationen den Synchronisationsprozess wie zuvor initiieren können. 

Den Code finden Sie im Brach test

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

Die Grundidee für das Glühwürmchen ist die gleiche. Nur anstatt, dass jedes Feld ein eigenes Glühwürmchen ist und zentral von einer Klasse ausgeführt werden, ist jedes Glühwürmchen nun sein eigener Prozess, der unabhängig von der UI läuft. Das Glühwürmchen meldet sich einmal an und geht dann auf die Reise. Neue Phasen werden jede Iteration an den Server verschickt, der sich die Phase jedes Glühwürmchens merkt. Das Glühwürmchen kann dann seine Nachbarn vom Server abfragen. Was musste grundlegend geändert werden vom Monolithen?

Für die Kommunikation zwischen Server und Client wurden in Apache Thrift zwei zentrale Methoden definiert:

```java
namespace java firefly

service FireflyService {
    void sendState(1: i32 clientId, 2: i32 state),
    list<i32> getNeighborStates(1: i32 gridX, 2: i32 gridY),
}```
Diese Methoden sind notwendig, um den Server über den aktuellen Zustand der Glühwürmchen auf dem Laufenden zu halten und dem Glühwürmchen den Zugriff auf die Zustände seiner Nachbarn zu ermöglichen. Der Server speichert die Zustände der Glühwürmchen in einer ConcurrentHashMap.

Verbindung zum Server
Das Glühwürmchen stellt eine Verbindung zum Server her:

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

Das sieht so aus:

https://github.com/user-attachments/assets/e2795918-62e7-4ca4-a207-decdb732a238

### Überraschendes

Vielleicht nicht überraschend für erfahrene Programmierer, aber überraschend für mich war, dass ab einer bestimmten Anzahl (bei mir ab 7) es zu Problemen mit der Serverconnection kam. Ich vermute Raceconditions bei den Clients. Das Ergebnis ist ein lückenhaftes Grid.


https://github.com/user-attachments/assets/358aaa67-02dd-4f4b-8b06-e4d16446a3b5

Versuche das ganze zu korrigieren mit mehrfachen oder verzögerten Verbindungsversuchen hat dazu geführt, dass kein synchrones Bild mehr entstanden ist, sondern die Würmchen in Wellen geleuchtet haben. Eine kurze Verzögerung bei der Initialisierung scheint dafür zu sorgen. 

Ein Lönsungsansatz dafür wäre die Glühwürmchen erst starten zu lassen, wenn alle Clienten verbunden sind.
