<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="rules-modal-content">
      <button @click="$emit('close')" class="cancel-button">✕</button>
      <h3>📜 Spielregeln 📜</h3>
      
      <pre class="rules-text">{{ gameRules }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
//Event, das beim Schließen ausgelöst wird
const emit = defineEmits<{
  (e: 'close'): void
}>()

//Regeln von Wiki kopiert
const gameRules = `
    * **Allgemeine Regeln**

    * **Spieler**
        Jeder Spieler hat 5 Spielfiguren.
        Wechsel: Erst Würfeln → Figur auswählen → Bewegung.

    * **Ziel des Spiels**
        Ziel-Position (Finish) erreichen.
        Sobald **eine** der 5 Figuren das Ziel erreicht, hat der Spieler gewonnen.
        **Kein** exakter Wurf nötig, Überwurf zählt als Ziel erreicht.

    * **Spielmechaniken**
        Spielmodel und Würfeln:
            Jeder Spieler kann alle **n** Sekunden würfeln (Echtzeit, nicht rundenbasiert).
            Würfelergebnis = Anzahl der Felder.
            Nur eine Richtung pro Zug (vorwärts oder rückwärts).
            Ablauf: Erst Würfeln → Figur auswählen → Bewegung.
        Bewegung und Besetzung von Feldern:
            Zäune nicht überspringen, Spielfiguren schon.
            Nur ein Objekt (Figur oder Hindernis) pro Feld.
            Eigene Figur auf Zielfeld: nicht zulässig.
            Gegnerische Figur auf Zielfeld: Duell.

    * **Duelle**
        Beginnen, wenn eine Figur auf eine gegnerische Figur zieht.
        Gewinner nimmt Feld ein, Verlierer kehrt zur Basis zurück.
        Minispiel wird zufällig ausgewählt (mind. 3 Minispiele).
        Timer wird **nur** für Duell-Teilnehmer angehalten.

    * **Barrieren**
        Dürfen nicht übersprungen werden.
        Überwurf: Figur bleibt **direkt vor** der Barriere stehen.
        Figur auf Barrierefeld: Figur besetzt Feld. Spieler kann Barriere auf **ein beliebiges freies Feld** verschieben.
        Zum Verschieben der Barriere muss die **exakte Augenzahl** gewürfelt werden.
        
    * **Sprünge**
        Würfelpunkte können als „Sprungenergie” gespart werden.
        Energie ermöglicht einen Überblick von oben (kostspielig).
        **Gemeinsamer Energie-Pool** für alle Figuren eines Spielers.
        Kosten: **10 Würfelpunkte** (gesammelt) / **3 Züge verzichten**.
    `
</script>

<style scoped>
/* Pop-up Hintergrund */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7); /* leicht durchsichtiger Hintergrund */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; 
}

/* Stil für Inhalt des Pop-ups */
.rules-modal-content {
  position: relative;
  background: rgb(25, 40, 25); 
  padding: 30px;
  border-radius: 15px;
  max-width: 80%;
  max-height: 80vh; 
  overflow-y: auto; /* Scrollbar, wenn der Inhalt zu lang ist */
  color: white;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
  border: 3px solid rgb(131, 102, 21);
}

.rules-modal-content h3 {
  text-align: center;
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 2rem;
  font-weight: bold;
}

.rules-text {
  white-space: pre-wrap; 
  font-size: 1.1rem;
  line-height: 1.6;
}

.cancel-button {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 1.5rem;
  background-color: transparent;
  color: white;
  border: none;
  cursor: pointer;
  padding: 5px 10px;
  line-height: 1; 
  margin: 0; 
}
</style>
