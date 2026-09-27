# Ghost Net Fishing

Prototyp einer Webanwendung für die Non Profit Organisation *Shea Sepherd* zum Melden und Bergen von
Geisternetzen (herrenlose Fischernetze, die im Meer treiben). Entstanden im Rahmen der Fallstudie
des Kurses IPWA02-01 *Programmierung von industriellen Informationssystemen mit Java EE* (IU Internationale Hochschule).

## Umgesetzte User Stories (Sprint 1)

| Nr. | Priorität | User Story | Umsetzung |
|-----|-----------|------------|-----------|
| 1 | MUST | Als meldende Person möchte ich Geisternetze (anonym) erfassen können. | Seite *Netz melden* |
| 2 | MUST | Als bergende Person will ich mich für die Bergung eines Geisternetzes eintragen können. | *Zu bergende Netze*, Knopf *Bergung übernehmen* |
| 3 | MUST | Als bergende Person möchte ich sehen, welche Geisternetze noch zu bergen sind. | Seite *Zu bergende Netze* mit Filter |
| 4 | MUST | Als bergende Person möchte ich Geisternetze als geborgen melden können. | *Meine Bergungen* bzw. *Zu bergende Netze* |
| 7 | COULD | Als beliebige Person möchte ich Geisternetze als verschollen melden können. | Knopf *Verschollen* (nur nach Anmeldung, nicht anonym) |

Fachliche Regeln: Ein Netz ist höchstens einer bergenden Person zugeordnet (Prüfung im Service mit
pessimistischer Datenbanksperre), bergende Personen können mehrere Netze übernehmen, meldende Personen
können anonym bleiben, verschollen melden ist nicht anonym möglich.

## Technologiestack

* Jakarta Faces (JSF) 4.0 mit Facelets
* CDI 4.0 (Backing Beans und Services)
* Jakarta Persistence (JPA) 3.1 mit Hibernate ORM als Persistenz Provider
* Bean Validation 3.0, Jakarta Transactions (`@Transactional`)
* MySQL 8 als relationale Datenbank
* Applikationsserver WildFly 35 (Jakarta EE 10), Build mit Maven, Java 17 oder neuer

## Projektstruktur

```
src/main/java/de/sheasepherd/ghostnet
├── model     Entitäten Geisternetz, Person und Enum NetzStatus
├── service   GeisternetzService, PersonService (Geschäftslogik, JPA Zugriff), Beispieldaten
└── web       Backing Beans (JSF/CDI): SitzungBean, MeldungBean, NetzUebersichtBean,
              MeineBergungenBean, StartseiteBean
src/main/resources/META-INF/persistence.xml
src/main/webapp   Facelets Seiten (index, melden, netze, anmelden, meine-bergungen), Template, CSS
setup/            SQL Skripte für MySQL und CLI Skript für die WildFly Datenquelle
```

## Installation und Start

1. **Voraussetzungen installieren:** JDK 17 oder 21, Maven 3.9, MySQL 8 und
   [WildFly 35](https://www.wildfly.org/downloads/) (ZIP entpacken).
2. **Datenbank anlegen:**
   ```
   mysql -u root -p < setup/01_mysql_datenbank.sql
   ```
3. **MySQL JDBC Treiber** `mysql-connector-j-9.1.0.jar` von
   <https://dev.mysql.com/downloads/connector/j/> (Platform Independent) herunterladen und in den Ordner `setup/` legen.
4. **WildFly starten:** `<WILDFLY_HOME>/bin/standalone.sh` (Windows: `standalone.bat`).
5. **Datenquelle einrichten** (einmalig, im Projektordner ausführen):
   ```
   <WILDFLY_HOME>/bin/jboss-cli.sh --connect --file=setup/02_wildfly_datasource.cli
   ```
6. **Bauen und deployen:**
   ```
   mvn clean package wildfly:deploy
   ```
   Alternativ `target/ghost-net-fishing.war` nach `<WILDFLY_HOME>/standalone/deployments/` kopieren.
7. Anwendung öffnen: <http://localhost:8080/ghost-net-fishing/>

Beim ersten Start legt Hibernate die Tabellen automatisch an (`hibernate.hbm2ddl.auto=update`), und die
Anwendung füllt die leere Datenbank mit einigen Beispielnetzen. Das lässt sich in `web.xml` über den
Kontextparameter `ghostnet.demodaten` abschalten. Die Tabellenstruktur ist zusätzlich in
`setup/03_schema_mysql.sql` dokumentiert.

## Bedienung

* **Netz melden:** Koordinaten in Dezimalgrad (z. B. 54.1823 und 7.8856) und die geschätzte Größe in m²
  eingeben. Standardmäßig anonym, optional mit Name und Telefonnummer.
* **Anmelden:** Bergende Personen melden sich mit Name und Telefonnummer an (im Prototyp ohne Passwort).
  Bekannte Personen werden über die Telefonnummer wiedererkannt.
* **Zu bergende Netze:** Liste aller Netze mit Status *Gemeldet* oder *Bergung bevorstehend*.
  Angemeldete Personen können eine Bergung übernehmen, eigene Bergungen als geborgen melden
  oder ein Netz als verschollen melden.
* **Meine Bergungen:** eigene bevorstehende und abgeschlossene Bergungen.

## Test

`test/e2e_test.py` ist ein automatisierter Oberflächentest mit Playwright, der alle umgesetzten User Stories
durchspielt (Melden anonym und namentlich, Validierung, Anmelden, Bergung übernehmen, gleichzeitige Übernahme
durch zwei Personen, als geborgen und als verschollen melden) und dabei Screenshots erzeugt. Er erwartet eine
frische Datenbank mit Beispieldaten:

```
pip install playwright && playwright install chromium
python test/e2e_test.py http://localhost:8080/ghost-net-fishing screenshots
```
