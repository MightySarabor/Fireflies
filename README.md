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
| ServerUI         |<>----------| + sendState()   |
|------------------|            +-----------------+
| + clientStates   |            
| + grid           |            
|------------------|            
| + start()        |            
| + startThriftServer() |       
| + updateGrid()   |            
+------------------+

+------------------+
| FireflyService   |
|------------------|
| + sendState()    |
| + getNeighborStates()|
+------------------+


## Die Reise des Glühwürmchens
Um den Code zu verstehen, betrachten wir den Programmablauf aus der Sicht eines Glühwürmchens. Der FireflyClient startet das Glühwürmchen. Dabei erhält das Glühwürmchen eine eindeutige ID und eine Position im Grid. Diese Position wird später verwendet, um die Nachbarn des Glühwürmchens zu berechnen.

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


### Kommunikation mit dem Server
