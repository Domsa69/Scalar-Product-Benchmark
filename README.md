# Produs scalar cu Producer–Consumer (Java)

Laborator: calculul produsului scalar a doi vectori folosind două thread-uri,
un **producer** și un **consumer**, legate printr-o coadă de dimensiune configurabilă.

## Cum funcționează

- **Producer**: parcurge cei doi vectori, calculează `a[i] * b[i]` și pune rezultatul în coadă.
- **Consumer**: scoate produsele din coadă și le adună într-o sumă.
- **Coada (`BoundedQueue`)**: are o capacitate maximă și este protejată de un `ReentrantLock` (mutex)
  și două `Condition` (variabile de condiție):
  - `notFull` – producerul așteaptă aici când coada este plină;
  - `notEmpty` – consumerul așteaptă aici când coada este goală.
- Consumerul poate folosi un produs imediat ce a fost pus în coadă.
- Producerul este pus să aștepte atunci când coada atinge dimensiunea maximă.

### Structura proiectului

| Clasă | Rol |
|---|---|
| `BoundedQueue` | coada cu `put` / `take`, sincronizată cu lock + 2 condition variables |
| `Producer` | calculează produsele `a[i] * b[i]` și le pune în coadă |
| `Consumer` | scoate produsele din coadă și le adună (`getSum()` returnează suma) |
| `Main` | rulare simplă, cu verificarea sumei față de un calcul secvențial |
| `ScalarBenchmark` | benchmark JMH, variază dimensiunea cozii |

La final, suma calculată de consumer este comparată cu produsul scalar calculat secvențial,
pentru a verifica corectitudinea.

## Măsurători

Benchmark realizat cu **JMH** (Java Microbenchmark Harness).

| Setare | Valoare |
|---|---|
| Mod | `AverageTime` (timp mediu per rulare) |
| Încălzire (warm-up) | 5 iterații × 1 s |
| Măsurători | 10 iterații × 1 s |
| Fork-uri | 1 |
| Lungime vectori (`n`) | 100 000 |
| Valori vectori | numere aleatoare între 1 și 10 |
| Parametru variat | dimensiunea cozii: 1, 2, 4, 8, 16, 32, 64, 128 |

Mașina de test: Mașina de test: Intel Core i9-13900HX (24 de nuclee: 8 performanță + 16 eficiență, 32 de thread-uri), 16 GB RAM (2×8 GB), JDK 23

### Rezultate

Timpul este timpul mediu pentru o rulare completă (un produs scalar pe `n` elemente).
Valorile brute JMH sunt în nanosecunde; mai jos sunt convertite în milisecunde.

| Dimensiune coadă | Timp mediu (ms) | Eroare (± ms) | Accelerare față de dimensiunea 1 |
|---:|---:|---:|---:|
| 1 | 523,43 | 131,41 | 1,0× |
| 2 | 248,25 | 12,79 | 2,1× |
| 4 | 141,98 | 9,53 | 3,7× |
| 8 | 148,31 | 42,07 | 3,5× |
| 16 | 45,17 | 6,48 | 11,6× |
| 32 | 22,91 | 1,97 | 22,8× |
| 64 | 15,74 | 0,99 | 33,2× |
| 128 | 8,18 | 0,18 | 64,0× |

Valorile brute (ns/op) din JMH:

```
Benchmark                      (queueSize)  Mode  Cnt          Score           Error  Units
ScalarBenchmark.scalarProduct            1  avgt   10  523425751.667 ± 131405471.325  ns/op
ScalarBenchmark.scalarProduct            2  avgt   10  248246581.000 ±  12786671.665  ns/op
ScalarBenchmark.scalarProduct            4  avgt   10  141980103.393 ±   9534125.190  ns/op
ScalarBenchmark.scalarProduct            8  avgt   10  148313076.778 ±  42069731.192  ns/op
ScalarBenchmark.scalarProduct           16  avgt   10   45172672.731 ±   6476248.177  ns/op
ScalarBenchmark.scalarProduct           32  avgt   10   22907871.284 ±   1971055.489  ns/op
ScalarBenchmark.scalarProduct           64  avgt   10   15743181.172 ±    985597.244  ns/op
ScalarBenchmark.scalarProduct          128  avgt   10    8177899.164 ±   184612.850  ns/op
```

## Observații

- **Dimensiunea cozii contează mult.** De la dimensiunea 1 la 128, timpul scade de la ~523 ms la ~8 ms,
  adică de aproximativ 64 de ori mai rapid.
- **Coada de dimensiune 1 este cea mai lentă.** Producerul și consumerul trebuie să se aștepte și să se
  trezească unul pe altul la aproape fiecare element. Este și cea mai instabilă măsurătoare
  (eroare de ±131 ms).
- **Câștigul se menține la cozi mari.** Timpul continuă să scadă și între 64 și 128, deci în intervalul
  testat nu s-a atins încă un punct de saturare.
- **Dimensiunea 8 iese din tendință.** Rezultatul (148 ms) nu este mai bun decât la dimensiunea 4 (142 ms)
  și are o eroare mare (±42 ms). Diferența dintre 4 și 8 se încadrează în marja de eroare, deci nu se
  poate trage concluzia că 8 este mai slab decât 4.
- **Explicația probabilă:** cu o coadă mică, cele două thread-uri își predau controlul foarte des
  (`await` / `signal`), iar fiecare predare costă timp. O coadă mai mare permite fiecărui thread să
  lucreze mai mulți pași la rând înainte de a fi nevoit să aștepte. Această explicație nu a fost
  verificată separat prin măsurători.

## Rulare

Benchmark-ul se rulează din `main`-ul clasei `ScalarBenchmark`.

Dependențe Gradle:

```groovy
dependencies {
    implementation 'org.openjdk.jmh:jmh-core:1.37'
    annotationProcessor 'org.openjdk.jmh:jmh-generator-annprocess:1.37'
}
```

În IntelliJ trebuie activat *Enable annotation processing*
(Settings → Build, Execution, Deployment → Compiler → Annotation Processors).
