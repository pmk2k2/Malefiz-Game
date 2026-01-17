import type { IPlayerFigure } from "./IPlayerFigure"

export interface ISpielerDTD {
  id: string
  name: string 
  isReady: boolean
  spielfiguren: Array<IPlayerFigure>
  isHost: boolean
  color?: string
}