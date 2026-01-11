<template>
    <div v-if="isVisible" class="modal-overlay" @click.self="close">
        <div class="modal-content">
            <button @click="close" class="close-button">✕</button>

            <h2 class="modal-title">Choose Game Board</h2>

            <div class="board-options">
                <div v-for="preset in presets" :key="preset" class="board-option"
                    :class="{ selected: selectedBoard === preset }" @click="selectPreset(preset)">
                    <div class="option-icon">🗺️</div>
                    <div class="option-name">{{ formatBoardName(preset) }}</div>
                    <div v-if="preset === 'DummyBoard.json'" class="default-badge">Default</div>
                </div>

                <div class="board-option custom-option" :class="{ selected: selectedBoard === 'custom' }"
                    @click="openEditor">
                    <div class="option-icon">✏️</div>
                    <div class="option-name">Create Custom Board</div>
                </div>
            </div>

            <div class="button-group">
                <button class="btn cancel-btn" @click="close">Cancel</button>
                <button class="btn select-btn" @click="confirmSelection" :disabled="!selectedBoard">
                    Select Board
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useGameStore } from '@/stores/gamestore'

const emit = defineEmits<{
    (e: 'close'): void
    (e: 'openEditor'): void
    (e: 'boardSelected', boardName: string): void
}>()

const props = defineProps<{
    isVisible: boolean
}>()

const gameStore = useGameStore()
const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'

const presets = ref<string[]>([])
const selectedBoard = ref<string | null>(null)
const loading = ref(false)

onMounted(async () => {
    await loadPresets()
})

async function loadPresets() {
    const gameCode = gameStore.gameData.gameCode
    if (!gameCode) return

    try {
        const res = await fetch(`${apiBase}/lobby/${gameCode}/boards/presets`)
        if (res.ok) {
            presets.value = await res.json()
        }
    } catch (err) {
        console.error('Failed to load presets:', err)
    }
}

function formatBoardName(filename: string): string {
    return filename.replace('.json', '').replace(/([A-Z])/g, ' $1').trim()
}

function selectPreset(preset: string) {
    selectedBoard.value = preset
}

function openEditor() {
    selectedBoard.value = 'custom'
    emit('openEditor')
}

async function confirmSelection() {
    if (!selectedBoard.value) return

    if (selectedBoard.value === 'custom') {
        emit('openEditor')
        return
    }

    loading.value = true
    const gameCode = gameStore.gameData.gameCode
    const playerId = gameStore.gameData.playerId

    try {
        const res = await fetch(
            `${apiBase}/lobby/${gameCode}/board/select-preset?playerId=${playerId}&presetName=${selectedBoard.value}`,
            { method: 'POST' }
        )

        if (res.ok) {
            emit('boardSelected', selectedBoard.value)
            close()
        } else {
            const err = await res.json()
            alert(err.error || 'Failed to select board')
        }
    } catch (err) {
        console.error('Failed to select preset:', err)
        alert('Failed to select board')
    } finally {
        loading.value = false
    }
}

function close() {
    emit('close')
}
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: #3d2b1f;
    background-image:
        linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%),
        repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0, 0, 0, 0.15) 39px, rgba(0, 0, 0, 0.15) 40px);
    border: 5px solid #2d1b0d;
    border-radius: 15px;
    padding: 30px;
    max-width: 600px;
    width: 90%;
    max-height: 80vh;
    overflow-y: auto;
    position: relative;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
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
    padding: 5px 10px;
    transition: transform 0.2s;
}

.close-button:hover {
    transform: scale(1.2);
}

.modal-title {
    color: #ffc107;
    text-align: center;
    margin-bottom: 25px;
    font-size: 1.8rem;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.board-options {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 15px;
    margin-bottom: 25px;
}

.board-option {
    background: #4d3319;
    border: 3px solid #2d1b0d;
    border-radius: 10px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
}

.board-option:hover {
    transform: translateY(-3px);
    border-color: #ffc107;
    box-shadow: 0 5px 15px rgba(255, 193, 7, 0.3);
}

.board-option.selected {
    background: #634121;
    border-color: #ffc107;
    box-shadow: 0 0 20px rgba(255, 193, 7, 0.5);
}

.custom-option {
    background: #2d4d19;
    border-color: #1e3311;
}

.custom-option:hover {
    border-color: #a7ff83;
    box-shadow: 0 5px 15px rgba(167, 255, 131, 0.3);
}

.custom-option.selected {
    background: #3d5d29;
    border-color: #a7ff83;
    box-shadow: 0 0 20px rgba(167, 255, 131, 0.5);
}

.option-icon {
    font-size: 2.5rem;
    margin-bottom: 10px;
}

.option-name {
    color: #f0e2d0;
    font-weight: 600;
    font-size: 0.9rem;
}

.default-badge {
    position: absolute;
    top: 5px;
    right: 5px;
    background: #ffc107;
    color: #2d1b0d;
    font-size: 0.7rem;
    padding: 2px 8px;
    border-radius: 5px;
    font-weight: bold;
}

.button-group {
    display: flex;
    justify-content: space-between;
    gap: 15px;
}

.btn {
    flex: 1;
    padding: 12px 0;
    font-size: 1.2rem;
    font-weight: 800;
    text-transform: uppercase;
    border-radius: 10px;
    border: 3px solid;
    cursor: pointer;
    transition: all 0.2s;
}

.cancel-btn {
    background: #6d2d2d;
    border-color: #421a1a;
    color: #f0e2d0;
}

.cancel-btn:hover {
    transform: translateY(-2px);
    filter: brightness(1.1);
}

.select-btn {
    background: #2d4d19;
    border-color: #1e3311;
    color: #a7ff83;
}

.select-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    filter: brightness(1.1);
}

.select-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}
</style>