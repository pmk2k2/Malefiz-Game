# 🎲 Milefiz 3D Multiplayer Game (SWT Projekt)

Der aktuelle Stand dient als Startbasis mit einem einfachen Beispiel („Würfel-Button“ im Frontend).

## frontend

Der frontend-Ordner wurde mit `npm create vue@latest frontend` angelegt
Es wurden noch keine Pakete (z.B. tres.js) nachinstalliert.

### frontend starten

Nach dem Klonen des Repo:

```
cd frontend (auf /2025SWTPRO01)
npm install
npm run dev
```

um der aktuelle Stand zu testen.

## Ausführbares Jar generieren

Mit `./gradlew bootJar` wird das Java-Projekt compiliert, das frontend gebaut,
die generierten Frontend-Dateien (aus frontend/dist) in src/main/resources/public
kopiert und das Ganze in ein ausführbares jar verpackt, das von Gradle in
build/libs abgelegt wird.

Die Anwendung sollte also startbar sein mit (bitte genauen Namen in build/libs checken):

```
java -jar build/libs/<DateiName.jar>
```

und dann wie gewohnt unter http://localhost:8080 abgerufen werden können.
