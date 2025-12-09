export type Orientation = 'north' | 'east' | 'south' | 'west'

export interface IPlayerFigure {
  id: string
  position: [number, number, number] // Weltkoordinaten x,y,z
  color: string
  playerId: string
  orientation: Orientation
}
