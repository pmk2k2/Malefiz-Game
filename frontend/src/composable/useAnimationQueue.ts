import type { IBewegung } from "@/services/IBewegung"
import { useGameStore } from "@/stores/gamestore"
import { useLoop } from "@tresjs/core"
import { storeToRefs } from "pinia"


// notwendige Parameter der Bewegungsanimation
export type AnimationJob = {
  // Allgemein Animation
  duration: number
  progressTime: number
  // Bei Bewegung von Figuren
  bewegung?: IBewegung | undefined
  startPos?: number[] | undefined
  // Bei drehen von Figuren
  startRot?: number | undefined
  targetRot?: number | undefined
}

export function useAnimationQueue() {
  // State der Figuren aus dem Gamestore beziehen
  const gameStore = useGameStore()
  const { figures } = storeToRefs(gameStore)

  const { onBeforeRender } = useLoop()

  // Der Figur eine Animation in die Queue einschleusen
  function queueMove(index: number, bewegung: IBewegung, duration = 400) {
    console.log("!!! Neues Event fuer Queue")
    let currRot = undefined
    let targetRot = undefined

    if(!figures.value[index]) return

    // Falls Drehung stattfindet (zb bei einer Kurve), diese Animation davor queuen
    if(figures.value[index].orientation !== bewegung.dir.toLowerCase()) {
      console.log("RICHTUNGSWECHSEL, BITTE DREHEN")
      currRot = getRotFromDir(figures.value[index].orientation)
      targetRot = getRotFromDir(bewegung.dir.toLowerCase())
      //queueRotation(index, currRot, targetRot, duration)
    }

    // Startpos aus Figur nehmen, falls vorhanden
    let startPos: Array<number>
    // Falls Figur ausserhalb des Spielfelds startet
    if(figures.value[index].position[0] == undefined || figures.value[index].position[1] == undefined || figures.value[index].position[2] == undefined) return
    if(bewegung.startX === null || bewegung.startZ === null) {
      startPos = [figures.value[index].position[0],figures.value[index].position[1],figures.value[index].position[2]]
    } else {
      startPos = [bewegung.startX, figures.value[index].position[1], bewegung.startZ]
    }

    // Wenn keine "Schritte" gelaufen werden
    if(bewegung.steps <= 0) {
      const newMove: AnimationJob = {
        bewegung: bewegung,
        duration: duration,
        progressTime: -1,
        startPos: startPos,
        startRot: currRot,
        targetRot: targetRot
      }
      console.log("Neuer Move in Queue: ", newMove)
      figures.value[index]?.animQueue.push(newMove)
      return
    }

    // Wenn Schritte gelaufen werden sollen:
    // mehrschrittige Bewegungen aufteilen in einzelne Schritte
    // damit ists einfacher die Animation "richtig" umzusetzen
    // und als erledigt abzuhaken
    console.log("Ihc komme hier an")
    for(let i = bewegung.steps; i > 0; i--) {
      const targetPos = getTargetPosByDir(startPos, bewegung.dir.toLowerCase())

      if(targetPos[0] == undefined || targetPos[2] == undefined) return
      const newBew: IBewegung = {
        ...bewegung,
        endX: targetPos[0],
        endZ: targetPos[2]
      }
      /*
      console.log("queue, newBew: ", newBew)
      console.log("queue, startPos: ", startPos)
      */
      if(startPos[0] == undefined || startPos[1] == undefined || startPos[2] == undefined) return
      const newMove: AnimationJob = {
        bewegung: newBew,
        duration: duration,
        progressTime: -1,
        startPos: [startPos[0], startPos[1], startPos[2]],
        startRot: currRot,
        targetRot: targetRot
      }
      figures.value[index].animQueue.push(newMove)
      startPos[0] = targetPos[0]
      startPos[2] = targetPos[2]
      currRot = targetRot
    }
  }

  function queueRotation(index: number, startRot: number, targetRot: number, duration = 400) {
    if(!figures.value[index]) return

    const newRot: AnimationJob = {
      duration: duration,
      progressTime: -1,
      startRot: startRot,
      targetRot: targetRot
    }

    figures.value[index].animQueue.push(newRot)
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
      const rawtime = Math.min(state.currentAnim.progressTime / state.currentAnim.duration, 1)

      // wenn es eine Rotationsanimation ist, diese ausfuehren
      if(state.currentAnim.startRot != undefined && state.currentAnim.targetRot != undefined) {
        const time = rawtime * rawtime * (3 - 2 * rawtime)  // Animation smoothen
        let startRot = state.currentAnim.startRot
        let targetRot = state.currentAnim.targetRot

        // wenn animation ost <--> sueden geht
        if(startRot === -0.5 && targetRot === 1)
          targetRot = -1
        if(startRot === 1 && targetRot === -0.5)
          startRot = -1

        const result = startRot + (targetRot - startRot) * time
        state.viewDirRot = (result === -1) ? 1 : result
      }
      if(state.currentAnim.bewegung && state.currentAnim.startPos) {
        const time = rawtime
        const newPos = [0,0,0]
        const startPos = [state.currentAnim.startPos[0], state.currentAnim.startPos[1], state.currentAnim.startPos[2]]
        const targetPos = [state.currentAnim.bewegung.endX, startPos[1], state.currentAnim.bewegung.endZ]

        if(startPos[0] == undefined || startPos[1] == undefined || startPos[2] == undefined)  return
        if(targetPos[0] == undefined || targetPos[2] == undefined)  return

        newPos[0] = startPos[0] + (targetPos[0] - startPos[0]) * time
        newPos[1] = startPos[1] + Math.sin(Math.PI * time)
        newPos[2] = startPos[2] + (targetPos[2] - startPos[2]) * time
        state.position = newPos as [number, number, number]
      }


      // absichern, dass Figur tatsaechlich auf richtigem Feld steht bevor naechste Animation startet
      // ...

      // Job beenden
      if(rawtime >= 1) {
        console.log("Animation fertig, entferne aus currentAnim")
        if (state.currentAnim?.bewegung) {
          if (gameStore.gameData.remainingSteps > 0) {
            gameStore.gameData.remainingSteps--
            gameStore.gameData.stepsTaken++
          }
        }
        state.currentAnim = null
      }
    })
  })

  // Schritt fuer Schritt Targetposition erhalten anhand der zu laufenden Richtung
  function getTargetPosByDir(position: Array<number>, direction: string) {

    // momentane und Zielposition halten
    const currentPos = [position[0], position[1], position[2]]
    if(currentPos[0] == undefined || currentPos[2] == undefined) return [position[0], position[1], position[2]]
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

  function getRotFromDir(direction: string) {

    switch(direction) {
      case 'north':
        return 0
      case 'east':
        return -0.5
      case 'south':
        return 1
      case 'west':
        return 0.5
    }
  }

  return {
    //mountFigures,
    queueMove,
    queueRotation
  }

}

