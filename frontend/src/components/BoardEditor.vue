<template>
    <div v-if="isVisible" class="editor-overlay" @click.self="cancel">
        <div class="editor-container">
            <button @click="cancel" class="close-button">✕</button>

            <h2 class="editor-title">Board Editor</h2>

            <!-- Size Controls -->
            <div class="controls-section">
                <div class="size-controls">
                    <div class="control-group">
                        <label>Rows:</label>
                        <input type="number" v-model.number="rows" min="3" max="30" @change="resizeGrid" />
                    </div>
                    <div class="control-group">
                        <label>Columns:</label>
                        <input type="number" v-model.number="cols" min="3" max="30" @change="resizeGrid" />
                    </div>
                </div>

                <!-- Cell Type Palette -->
                <div class="palette">
                    <div class="palette-title">Select Cell Type:</div>
                    <div class="palette-buttons">
                        <button v-for="type in cellTypes" :key="type" class="palette-btn"
                            :class="{ active: selectedType === type, [type.toLowerCase()]: true }"
                            @click="selectedType = type">
                            {{ formatCellType(type) }}
                        </button>
                    </div>
                </div>
            </div>

            <!-- Grid Editor -->
            <div class="grid-container">
                <div class="grid-wrapper">
                    <!-- Direction Labels -->
                    <div class="direction-label top">
                        <span class="direction-text">🎯 GOAL SIDE</span>
                    </div>

                    <div class="grid" :style="{
                        gridTemplateColumns: `repeat(${cols}, 1fr)`,
                        gridTemplateRows: `repeat(${rows}, 1fr)`
                    }">
                        <div v-for="(cell, index) in grid" :key="index" class="cell" :class="cell.type.toLowerCase()"
                            @click="setCellType(cell)" :title="`Grid: (${cell.i}, ${cell.j}) - ${cell.type}`">
                            <span class="cell-label">{{ getCellEmoji(cell.type) }}</span>
                        </div>
                    </div>

                    <div class="direction-label bottom">
                        <span class="direction-text">🏁 START SIDE</span>
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="button-group">
                <button class="btn cancel-btn" @click="cancel">Cancel</button>
                <button class="btn reset-btn" @click="resetGrid">Reset Grid</button>
                <button class="btn confirm-btn" @click="confirmBoard">Confirm Board</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useGameStore } from '@/stores/gamestore'

type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL' | 'BARRIER'

interface Field {
    i: number
    j: number
    type: CellType
}

interface Board {
    cols: number
    rows: number
    grid: Field[][]
}

const emit = defineEmits<{
    (e: 'close'): void
    (e: 'boardSaved'): void
}>()

const props = defineProps<{
    isVisible: boolean
}>()

const gameStore = useGameStore()
const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'

const cellTypes: CellType[] = ['PATH', 'START', 'GOAL', 'BLOCKED', 'BARRIER']
const selectedType = ref<CellType>('PATH')

const rows = ref(8)
const cols = ref(11)
const grid = ref<Field[]>([])

// Initialize grid when component becomes visible
watch(() => props.isVisible, (visible) => {
    if (visible) {
        initializeGrid()
    }
})

function initializeGrid() {
    grid.value = []
    // Build grid from bottom to top (reversed j order)
    for (let j = rows.value - 1; j >= 0; j--) {
        for (let i = 0; i < cols.value; i++) {
            grid.value.push({
                i,
                j,
                type: 'PATH'
            })
        }
    }
}

function resizeGrid() {
    const newGrid: Field[] = []

    // Build grid from bottom to top (reversed j order)
    for (let j = rows.value - 1; j >= 0; j--) {
        for (let i = 0; i < cols.value; i++) {
            // Try to keep existing cell data if it exists
            const existing = grid.value.find(c => c.i === i && c.j === j)
            newGrid.push({
                i,
                j,
                type: existing?.type || 'PATH'
            })
        }
    }

    grid.value = newGrid
}

function setCellType(cell: Field) {
    cell.type = selectedType.value
}

function resetGrid() {
    if (confirm('Are you sure you want to reset the entire grid to PATH tiles?')) {
        grid.value.forEach(cell => {
            cell.type = 'PATH'
        })
    }
}

function formatCellType(type: CellType): string {
    return type.charAt(0) + type.slice(1).toLowerCase()
}

function getCellEmoji(type: CellType): string {
    const emojis: Record<CellType, string> = {
        'PATH': '·',
        'START': 'S',
        'GOAL': 'G',
        'BLOCKED': '🌲',
        'BARRIER': '🚧'
    }
    return emojis[type] || '?'
}

async function confirmBoard() {
    const gameCode = gameStore.gameData.gameCode
    const playerId = gameStore.gameData.playerId

    if (!gameCode || !playerId) {
        alert('Missing game code or player ID')
        return
    }

    // Convert flat grid to 2D array for backend
    // Grid is already in reversed order (bottom to top), 
    // so we need to reverse it back for proper j-indexing
    const grid2D: Field[][] = []
    for (let j = 0; j < rows.value; j++) {
        const row: Field[] = []
        for (let i = 0; i < cols.value; i++) {
            const cell = grid.value.find(c => c.i === i && c.j === j)
            if (cell) {
                row.push(cell)
            }
        }
        grid2D.push(row)
    }

    const boardData: Board = {
        cols: cols.value,
        rows: rows.value,
        grid: grid2D
    }

    try {
        const res = await fetch(
            `${apiBase}/lobby/${gameCode}/board?playerId=${playerId}`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(boardData)
            }
        )

        if (res.ok) {
            emit('boardSaved')
            emit('close')
        } else {
            const err = await res.json()
            alert(err.error || 'Failed to save board')
        }
    } catch (err) {
        console.error('Failed to save board:', err)
        alert('Failed to save board')
    }
}

function cancel() {
    if (confirm('Discard changes?')) {
        emit('close')
    }
}
</script>

<style scoped>
.editor-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1001;
    padding: 20px;
}

.editor-container {
    background: #3d2b1f;
    background-image:
        linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%);
    border: 5px solid #2d1b0d;
    border-radius: 15px;
    padding: 20px;
    max-width: 95vw;
    max-height: 95vh;
    width: 100%;
    height: 100%;
    position: relative;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.7);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.close-button {
    position: absolute;
    top: 15px;
    right: 15px;
    background: transparent;
    border: none;
    color: #ffc107;
    font-size: 1.5rem;
    cursor: pointer;
    z-index: 10;
}

.editor-title {
    color: #ffc107;
    text-align: center;
    margin-bottom: 12px;
    font-size: 1.4rem;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
    flex-shrink: 0;
}

.controls-section {
    margin-bottom: 12px;
    flex-shrink: 0;
}

.size-controls {
    display: flex;
    gap: 20px;
    justify-content: center;
    margin-bottom: 10px;
}

.control-group {
    display: flex;
    align-items: center;
    gap: 10px;
}

.control-group label {
    color: #f0e2d0;
    font-weight: 600;
}

.control-group input {
    width: 70px;
    padding: 8px;
    border: 2px solid #2d1b0d;
    border-radius: 5px;
    background: #4d3319;
    color: #f0e2d0;
    font-size: 1rem;
    text-align: center;
}

.palette {
    background: #2d1b0d;
    padding: 8px;
    border-radius: 10px;
}

.palette-title {
    color: #ffc107;
    font-weight: 600;
    margin-bottom: 6px;
    text-align: center;
    font-size: 0.85rem;
}

.palette-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
}

.palette-btn {
    padding: 6px 14px;
    border: 2px solid #4d3319;
    border-radius: 8px;
    background: #4d3319;
    color: #f0e2d0;
    font-weight: 600;
    font-size: 0.8rem;
    cursor: pointer;
    transition: all 0.2s;
}

.palette-btn:hover {
    transform: translateY(-2px);
    border-color: #ffc107;
}

.palette-btn.active {
    border-color: #ffc107;
    box-shadow: 0 0 10px rgba(255, 193, 7, 0.5);
    transform: scale(1.05);
}

.palette-btn.start {
    background: #4a90e2;
}

.palette-btn.goal {
    background: #f5a623;
}

.palette-btn.blocked {
    background: #2d1b0d;
}

.palette-btn.barrier {
    background: #d0021b;
}

.palette-btn.path {
    background: #7ed321;
}

.grid-container {
    background: #2d1b0d;
    padding: 12px;
    border-radius: 10px;
    margin-bottom: 10px;
    flex: 1;
    overflow: auto;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 0;
}

.grid-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.direction-label {
    background: #4d3319;
    padding: 8px 20px;
    border-radius: 8px;
    border: 2px solid #ffc107;
    box-shadow: 0 0 10px rgba(255, 193, 7, 0.3);
}

.direction-label.top {
    border-color: #f5a623;
}

.direction-label.bottom {
    border-color: #4a90e2;
}

.direction-text {
    color: #ffc107;
    font-weight: 700;
    font-size: 0.85rem;
    text-transform: uppercase;
    letter-spacing: 1px;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
}

.grid {
    display: grid;
    gap: 2px;
    margin: 0 auto;
    width: fit-content;
    height: fit-content;
}

.cell {
    width: 35px;
    height: 35px;
    border: 1px solid #4d3319;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.1s;
    font-size: 1.1rem;
    flex-shrink: 0;
}

.cell:hover {
    transform: scale(1.1);
    z-index: 10;
    box-shadow: 0 0 5px rgba(255, 193, 7, 0.5);
}

.cell.path {
    background: #7ed321;
}

.cell.start {
    background: #4a90e2;
}

.cell.goal {
    background: #f5a623;
}

.cell.blocked {
    background: #2d1b0d;
}

.cell.barrier {
    background: #d0021b;
}

.cell-label {
    color: white;
    font-weight: bold;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
}

.button-group {
    display: flex;
    gap: 10px;
    justify-content: center;
    flex-shrink: 0;
    flex-direction: row;
}

.btn {
    min-width: 120px;
    padding: 10px 15px;
    font-size: 0.95rem;
    font-weight: 800;
    text-transform: uppercase;
    border-radius: 10px;
    border: 3px solid;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.cancel-btn {
    background: #6d2d2d;
    border-color: #421a1a;
    color: #f0e2d0;
}

.reset-btn {
    background: #4d3319;
    border-color: #2d1b0d;
    color: #ffc107;
}

.confirm-btn {
    background: #2d4d19;
    border-color: #1e3311;
    color: #a7ff83;
}

.btn:hover {
    transform: translateY(-2px);
    filter: brightness(1.1);
}
</style>