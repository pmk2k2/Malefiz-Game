import type { ISpielfigurDTD } from "./ISpielfigurDTD"

export interface ISpielerDTD {
  id: number
  name: string 
  bereitschaft: boolean
  spielfiguren: Array<ISpielfigurDTD>
  isHost: boolean
}