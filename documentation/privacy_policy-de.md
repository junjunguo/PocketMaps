**Datenschutzrichtlinie der App PocketMaps**

Diese Applikation sammelt keine persönlichen Daten von seinen Benutzern.
Der Benutzer kann selbst Daten exportieren, oder teilen.

*Daten und Service-Arten*
1. GPS Daten:
- Daten werden nicht übermittelt.
- Der Benutzer kann eine Position mit Freunden teilen.
2. Kartenmaterial herunterladen:
- Für das Herunterladen wird eine normale http-Verbindung aufgebaut.
- Für diese Verbindung werden keine unüblichen Daten übermittelt, außer der App-Version.
- Diese Informationen sind temporär am Server auf den apache-Logfiles nur für Debugzwecke einsehbar.
3. Ortsuche (Geocoding):
Dem User stehen 3 Werkzeuge zur Ortsuche zur Verfügung
- Offline: Die Ortsuche erfolgt komplett offline.
- GoogleMaps: Die benötigten Daten werden an die Google-API übermittelt.
- OpenStreetMaps: Die benötigten Daten werden an die OSM-API übermittelt.
4. Pfadberechnung:
- Die Pfadberechnung erfolgt komplett offline.
5. Speicherzugriff:
- Die App benötigt den Speicherzugriff um das Kartenmaterial, Favouriten, sowie Konfigurationsdaten zu speichern, die für die App nötig sind.
- Auch das Exportieren/Importieren ist als Funktion für den Benutzer möglich.

Der Sourcecode der App ist offen einsehbar (open source), und alle Funktionen somit transparent.

*Eigentümer*
Paul Kashofer
Soundmodul@gmx.at
Entwickler als 'Starcommander' auf github
