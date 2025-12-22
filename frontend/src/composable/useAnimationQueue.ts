import type { IBewegung } from "@/services/IBewegung"
import { useGameStore } from "@/stores/gamestore"
import type { IPlayerFigure, Orientation } from "@/stores/IPlayerFigure"
import { useLoop } from "@tresjs/core"
import { storeToRefs } from "pinia"
import { ref } from "vue"


// notwendige Parameter der Bewegungsanimation
export type AnimationJob = {
  bewegung: IBewegung
  duration: number
  progressTime: number
  startPos: number[]
}

export function useAnimationQueue() {
  // State der Figuren aus dem Gamestore beziehen
  const gameStore = useGameStore()
  const { figures } = storeToRefs(gameStore)

  const { onBeforeRender } = useLoop()

  // Der Figur eine Animation in die Queue einschleusen
  function queueMove(index: number, bewegung: IBewegung, duration = 400) {
    console.log("!!! Neues Event fuer Queue")

    // Startpos aus Figur nehmen, falls vorhanden
    let startPos
    // Falls Figur ausserhalb des Spielfelds startet
    if(bewegung.startX === null || bewegung.startZ === null) {
      startPos = [figures.value[index].position[0],figures.value[index].position[1],figures.value[index].position[2]]
    } else {
      startPos = [bewegung.startX, figures.value[index].position[1],bewegung.startZ]
    }
    
    // Wenn keine "Schritte" gelaufen werden
    if(bewegung.steps <= 0) {
      const newMove: AnimationJob = {
        bewegung: bewegung,
        duration: duration,
        progressTime: -1,
        startPos: startPos
      }
      console.log("Neuer Move in Queue: ", newMove)
      figures.value[index]?.animQueue.push(newMove)
      return
    }

    // Wenn Schritte gelaufen werden sollen:
    // mehrschrittige Bewegungen aufteilen in einzelne Schritte
    // damit ists einfacher die Animation "richtig" umzusetzen
    // und als erledigt abzuhaken
    for(let i = bewegung.steps; i > 0; i--) {
      const targetPos = getTargetPosByDir(startPos, bewegung.dir.toLowerCase())
      const newBew: IBewegung = {
        ...bewegung,
        endX: targetPos[0],
        endZ: targetPos[2]
      }
      console.log("queue, newBew: ", newBew)
      console.log("queue, startPos: ", startPos)
      const newMove: AnimationJob = {
        bewegung: newBew,
        duration: duration,
        progressTime: -1,
        startPos: [startPos[0], startPos[1], startPos[2]]
      }
      figures.value[index].animQueue.push(newMove)
      startPos[0] = targetPos[0]
      startPos[2] = targetPos[2]
    }
  }

  // Loop zur Verarbeitung der Animationen
  console.log("Starte Animationloop")
  onBeforeRender(({ delta }) => {
    figures.value.forEach((state) => {
      // falls kein Job aktiv, Queue ueberpruefen
      if(!state.currentAnim) {
        if(state.animQueue.length > 0) {
          const nextJob = state.animQueue.shift()!

          // Neuen Job einstellen
          state.currentAnim = nextJob
          state.currentAnim.progressTime = 0
        }
        return
      }

      // Animation ausfuehren
      state.currentAnim.progressTime += delta * 1000
      // Animation laeuft im Grunde von 0-100% durch
      // Dabei ist "(now-startTime) / duration" der Anteil, wie viel von der Animation abgeschlossen wurde
      // Da es zeitbasiert ist, sollte es kein Problem mit verschiedenen Framerates geben
      // Wenn mehr Zeit vergangen ist, als ein Zyklus eigentlich geht, so wird time auf 1
      // und somit die Animation als fertig gesehen
      const time = Math.min(state.currentAnim.progressTime / state.currentAnim.duration, 1)

      let newPos = [0,0,0]
      const startPos = [state.currentAnim.startPos[0], state.currentAnim.startPos[1], state.currentAnim.startPos[2]]
      const targetPos = [state.currentAnim.bewegung.endX, startPos[1], state.currentAnim.bewegung.endZ]
      //console.log("Startpos: ", startPos)
      //console.log("targetPos: ", targetPos)
      newPos[0] = startPos[0] + (targetPos[0] - startPos[0]) * time
      newPos[1] = startPos[1] + Math.sin(Math.PI * time)
      newPos[2] = startPos[2] + (targetPos[2] - startPos[2]) * time
      state.position = newPos


      // absichern, dass Figur tatsaechlich auf richtigem Feld steht bevor naechste Animation startet
      // ...

      // Job beenden
      if(time >= 1) {
        console.log("Animation fertig, entferne aus currentAnim")
        state.currentAnim = null
      }
    })
  })

  // Schritt fuer Schritt Targetposition erhalten anhand der zu laufenden Richtung
  function getTargetPosByDir(position, direction) {
    console.log("Bewegung in Richtung: ", direction)  

    // momentane und Zielposition halten
    let currentPos = [position[0], position[1], position[2]]
    // ein Feld = 2 Einheiten
    // je nach direction anpassen
      let moveNS = 0
      let moveWE = 0
      switch(direction) {
        case 'north':
          moveNS = -2
          break;
        case 'south':
          moveNS = 2
          break;
        case 'west':
          moveWE = -2
          break;
        case 'east':
          moveWE = 2
          break;
      }
      return [currentPos[0] + moveWE, 0.2, currentPos[2] + moveNS]
  }

  return {
    //mountFigures,
    queueMove
  }

}

