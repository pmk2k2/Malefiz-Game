import type { ISpielfigurDTD } from "./ISpielfigurDTD"

export interface ISpielerDTD {
  id: string
  name: string 
  isReady: boolean
  spielfiguren: Array<ISpielfigurDTD>
  isHost: boolean
}