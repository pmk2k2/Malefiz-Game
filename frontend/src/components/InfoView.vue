<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="rules-modal-content parchment">
      <button @click="$emit('close')" class="close-btn">✕</button>
      
      <div class="scroll-content">
        <h3 class="rules-title">Spielregeln</h3>
        <pre class="rules-text">{{ gameRules }}</pre>
      </div>
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
        Allgemeine Regeln

        Spieler
        Jeder Spieler hat 5 Spielfiguren.
        Wechsel: Erst Würfeln → Figur auswählen → Bewegung.

        Ziel des Spiels
        Ziel-Position (Finish) erreichen.
        Sobald eine der 5 Figuren das Ziel erreicht, hat der Spieler gewonnen.
        Kein exakter Wurf nötig, Überwurf zählt als Ziel erreicht.

        Spielmechaniken
        Spielmodel und Würfeln:
            Jeder Spieler kann alle "n" Sekunden würfeln (Echtzeit, nicht rundenbasiert).
            Würfelergebnis = Anzahl der Felder.
            Nur eine Richtung pro Zug (vorwärts oder rückwärts).
            Ablauf: Erst Würfeln → Figur auswählen → Bewegung.
        Bewegung und Besetzung von Feldern:
            Zäune nicht überspringen, Spielfiguren schon.
            Nur ein Objekt (Figur oder Hindernis) pro Feld.
            Eigene Figur auf Zielfeld: nicht zulässig.
            Gegnerische Figur auf Zielfeld: Duell.

        Duelle
        Beginnen, wenn eine Figur auf eine gegnerische Figur zieht.
        Gewinner nimmt Feld ein, Verlierer kehrt zur Basis zurück.
        Minispiel wird zufällig ausgewählt (mind. 3 Minispiele).
        Timer wird nur für Duell-Teilnehmer angehalten.

        Barrieren
        Dürfen nicht übersprungen werden.
        Überwurf: Figur bleibt direkt vor der Barriere stehen.
        Figur auf Barrierefeld: Figur besetzt Feld. Spieler kann Barriere auf ein beliebiges freies Feld verschieben.
        Zum Verschieben der Barriere muss die exakte Augenzahl gewürfelt werden.
        
        Sprünge
        Würfelpunkte können als „Sprungenergie” gespart werden.
        Energie ermöglicht einen Überblick von oben (kostspielig).
        Gemeinsamer Energie-Pool für alle Figuren eines Spielers.
        Kosten: 10 Würfelpunkte (gesammelt) / 3 Züge verzichten.
    `
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000; 
}

.rules-modal-content {
  position: relative;
  background-color: #f0e2d0; 
  /* Verlauf für die Textur des Papiers */
  background-image: 
    radial-gradient(circle at 50% 50%, rgba(255,255,255,0.2), transparent),
    url('https://www.transparenttextures.com/patterns/pinstriped-suit.png'); /* Leichte Textur */
  
  padding: 40px;
  width: 70%;
  max-width: 800px;
  max-height: 85vh;
  border: 12px solid #3d2b1f;
  border-radius: 4px;
  box-shadow: 
    0 20px 50px rgba(0,0,0,0.8),
    inset 0 0 60px rgba(139, 69, 19, 0.2);
    
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.scroll-content {
  overflow-y: auto;
  padding-right: 15px;
}

.rules-title {
  text-align: center;
  font-family: 'Kanit', sans-serif;
  color: #2d1b0d;
  font-size: 2.5rem;
  font-weight: 900;
  text-transform: uppercase;
  margin-bottom: 25px;
  border-bottom: 2px dashed rgba(61, 43, 31, 0.3);
  padding-bottom: 10px;
}

.rules-text {
  white-space: pre-wrap;
  font-family: 'Kanit', sans-serif; 
  font-size: 1.1rem;
  line-height: 1.6;
  color: #3d2b1f;
  font-weight: 500;
}

.close-btn {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 50px;
  height: 50px;
  background: #6d2d2d;
  color: #f0e2d0;
  border: 4px solid #2d1b0d;
  border-radius: 50%;
  font-size: 1.5rem;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 8px rgba(0,0,0,0.5);
  transition: all 0.2s;
  z-index: 2001;
}

.close-btn:hover {
  transform: scale(1.1) rotate(90deg);
  background: #c62828;
}

.scroll-content::-webkit-scrollbar {
  width: 10px;
}

.scroll-content::-webkit-scrollbar-track {
  background: rgba(61, 43, 31, 0.1);
  border-radius: 5px;
}

.scroll-content::-webkit-scrollbar-thumb {
  background: #3d2b1f;
  border-radius: 5px;
}
</style>
