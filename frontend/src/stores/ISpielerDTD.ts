import type { IPlayerFigure } from "./IPlayerFigure"
import type { ISpielfigurDTD } from "./ISpielfigurDTD"

export interface ISpielerDTD {
  id: string
  name: string 
  isReady: boolean
  spielfiguren: Array<IPlayerFigure>
  isHost: boolean
  color?: string
}