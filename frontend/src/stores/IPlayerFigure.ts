import type { AnimationJob } from "@/composable/useAnimationQueue"

export type Orientation = 'north' | 'east' | 'south' | 'west'

export interface IPlayerFigure {
  id: string
  position: Array<number> // Weltkoordinaten x,y,z
  color: string
  playerId: string
  //orientation: Orientation
  orientation: string
  viewDirRot: number
  // Felder fuer Animation
  animQueue: Array<AnimationJob>   // Queue fuer die Animationen in den einzelnen Figuren halten
  currentAnim: AnimationJob | null
}
