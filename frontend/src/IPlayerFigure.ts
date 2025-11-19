export interface IPlayerFigure {
    position: [Number, Number, Number],
    color: string,
    player: string,
    orientation: 'north' | 'east' | 'south' | 'west'
}