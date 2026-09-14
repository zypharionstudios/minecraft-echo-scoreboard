# EchoSMP

Ein vollständig automatisches Sidebar-Scoreboard-Plugin für Paper/Spigot 1.21+.

## Features

- Sidebar mit dem Titel `echo smp` und einem glatten Drei-Farben-Gradienten.
- Persönlicher Spielername.
- Aktuelle Online-Spielerzahl und konfigurierte Maximalzahl.
- Persistente Spielzeit pro Spieler im Format `Xs`, `Ym` oder `Xh Ym`.
- Online-Spielerzahl blau und Spielzeit gelb.
- Ping-Anzeige mit Farben: `1-100 ms` grün, `101-214 ms` orange und ab `215 ms` rot.
- MS-Anzeige mit Signal-Emoji, Trennlinie und dunkelviolettem `discord:eosmp`-Hinweis.
- Spielzeit wird beim Join geladen, beim Quit gespeichert, alle fünf Minuten gesichert und beim Server-Stop geschrieben.
- Keine Commands und keine Permissions.
- Rote Score-Zahlen werden mit der Paper-API `NumberFormat.blank()` ausgeblendet.
- Konfiguration wird beim ersten Start automatisch erzeugt und gespeichert.

## Installation

1. Eine aktuelle Paper-1.21+-Serverversion mit Java 21 verwenden.
2. Die Datei `scoreboardpluginah.jar` aus dem Release oder aus `target/` nach `plugins/` kopieren.
3. Den Server starten.
4. Beim ersten Start werden die Dateien im Ordner `plugins/scoreboardpluginah/` angelegt.

Die Anzeigen und Speicherungen laufen automatisch. Zusätzlich gibt es `/scoreboard settings` und `/sell`.

Mit `/scoreboard settings` kann jeder Spieler in einem 3x9-Menü Uhr, Geld, MS/Ping und Spielerzahl einzeln ein- oder ausschalten. Die Auswahl wird in `plugins/scoreboardpluginah/settings.yml` gespeichert.

Mit `/sell` öffnet sich ein 3x9-Menü. Verkaufbare Blöcke werden in die oberen 25 Slots gelegt. Die grüne Glasscheibe unten rechts ist nicht entnehmbar und bestätigt den Verkauf. Nicht verkaufbare Items werden zurückgegeben. Guthaben liegt in `plugins/scoreboardpluginah/money.yml`, Blockpreise in `plugins/scoreboardpluginah/prices.yml`; diese Preisdatei wird automatisch für wirklich jeden Block angelegt. Die neue Preisstaffel verwendet realistische Gruppenwerte statt eines 1er-Fallbacks. Eine vorhandene alte Preisdatei wird beim nächsten Serverstart einmalig auf Preis-Schema 2 migriert und kann danach manuell angepasst werden.

Mit `/ah` öffnet sich das 6x9-Auktionshaus. Die unteren Bedienelemente sind Aktualisieren, Sortierung, Suche und eigene Angebote. Eigene Angebote werden über die Truhe geöffnet und mit dem Buch erstellt. Nach der Eingabe eines Preises wird das Angebot 24 Stunden lang angezeigt; Käufer erhalten das Item und der Verkäufer das Geld. Eigene Angebote können nicht gekauft werden.

Im 3x9-Menü sind die Slots `0-24` für genau einen Item-Stack frei. Nur das graue Glas in Slot 25 und das grüne Bestätigungs-Glas in Slot 26 sind gesperrt. Ein Klick auf ein eigenes gelistetes Angebot nimmt es aus dem AH und gibt den Item-Stack zurück.

Schnellbefehle: `/ah sell 40k` verkauft den Item-Stack aus der Haupthand direkt für 40.000. `/ah diamond ore` öffnet das AH direkt mit der Suche; Unterstriche, Leerzeichen und Groß-/Kleinschreibung werden gleich behandelt.

## Build lokal

Voraussetzung ist ein installiertes JDK 21.

```text
mvn clean package
```

Die fertige Datei liegt danach unter `target/EchoSMP-1.0.0.jar`.

## GitHub Actions

Der Workflow `.github/workflows/build.yml` baut bei jedem Push und Pull Request mit Java 21 über Maven. Die erzeugte JAR wird als Workflow-Artefakt `EchoSMP` hochgeladen.

## Konfiguration

Beim ersten Start wird die Vorlage aus `src/main/resources/config.yml` nach `plugins/EchoSMP/config.yml` kopiert und danach gespeichert. Die Standardwerte sind:

```yaml
update-interval-ticks: 20
max-players: -1
gradient-start: "#FF8A8A"
gradient-mid: "#E63946"
gradient-end: "#FFA500"
emoji-player: "👤"
emoji-clock: "⏰"
scoreboard-title: "echo smp"
```

`max-players: -1` verwendet automatisch `Bukkit.getMaxPlayers()`. Änderungen werden beim nächsten Serverstart eingelesen; es gibt keinen Reload-Befehl.

## Datenspeicherung

Die Spielzeiten liegen in `plugins/scoreboardpluginah/playerdata.yml`:

```yaml
<UUID>: <sekunden>
```

Die Datei wird bei jedem Quit, alle fünf Minuten und beim Server-Stop gespeichert. Die aktuell laufende Session wird für die Anzeige aus dem gespeicherten Basiswert und der Join-Zeit berechnet.

## Repository auf GitHub veröffentlichen

Im Repository-Ordner ausführen:

```text
git init
git add .
git commit -m "Initiale EchoSMP-Version"
git branch -M main
git remote add origin https://github.com/DEIN-ACCOUNT/DEIN-REPOSITORY.git
git push -u origin main
```

Die URL des Remotes durch das eigene GitHub-Repository ersetzen. Nach dem Push ist der Build unter dem Tab **Actions** sichtbar.
