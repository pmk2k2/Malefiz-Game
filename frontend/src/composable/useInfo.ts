import type { string } from 'three/tsl'
import { reactive } from 'vue'

export interface Nachricht {
  id: number
  text: string
  typ: 'info' | 'error' | 'success'
}

const nachrichten = reactive<Nachricht[]>([])

export function useInfo() {
  function loescheInfo(id: number) {
    const index = nachrichten.findIndex((n) => n.id === id)
    if (index !== -1) nachrichten.splice(index, 1)
  }

  function setzeInfo(text: string, typ: 'info' | 'error' | 'success' = 'info') {
    const id = Date.now()
    nachrichten.push({ id, text, typ })

    setTimeout(() => {
      loescheInfo(id)
    }, 5000) //Infobox verschwindet nach 5 sekunden
  }

  return { nachrichten, loescheInfo, setzeInfo }
}
