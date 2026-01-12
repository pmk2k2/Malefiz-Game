<template>
    <div v-if="isVisible" class="modal-overlay" @click.self="close">
        <div class="modal-content">
            <button @click="close" class="close-button">✕</button>

            <h2 class="modal-title">Game Settings</h2>

            <div class="settings-section">
                <div class="setting-item">
                    <h3 class="section-subtitle">Dice Cooldown</h3>
                    <div class="settings-control">
                        <button class="settingsButton" @mousedown="startTimer('time', 'decrease')"
                            @mouseup="stopTimer('time')" @mouseleave="stopTimer('time')">
                            -
                        </button>
                        <div class="valueDisplay">{{ time }}s</div>
                        <button class="settingsButton" @mousedown="startTimer('time', 'increase')"
                            @mouseup="stopTimer('time')" @mouseleave="stopTimer('time')">
                            +
                        </button>
                    </div>
                </div>

                <div class="setting-item">
                    <h3 class="section-subtitle">Energy Limit</h3>
                    <div class="settings-control">
                        <button class="settingsButton" @mousedown="startTimer('energy', 'decrease')"
                            @mouseup="stopTimer('energy')" @mouseleave="stopTimer('energy')">
                            -
                        </button>
                        <div class="valueDisplay">{{ maxEnergy }}</div>
                        <button class="settingsButton" @mousedown="startTimer('energy', 'increase')"
                            @mouseup="stopTimer('energy')" @mouseleave="stopTimer('energy')">
                            +
                        </button>
                    </div>
                </div>
            </div>

            <hr class="divider" />

            <h3 class="section-subtitle">Choose Game Board</h3>
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
                    <div class="option-name">Create Custom</div>
                </div>
            </div>

            <div class="button-group">
                <button class="btn cancel-btn" @click="close">Cancel</button>
                <button class="btn select-btn" @click="confirmAll" :disabled="!selectedBoard || loading">
                    {{ loading ? 'Saving...' : 'Apply & Start' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useGameStore } from '@/stores/gamestore'

const props = defineProps<{ isVisible: boolean }>()
const emit = defineEmits(['close', 'openEditor', 'settingsUpdated'])

const gameStore = useGameStore()
const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'

// State
const presets = ref<string[]>([])
const selectedBoard = ref<string | null>(null)
const time = ref(1)
const maxEnergy = ref(10)
const loading = ref(false)
let interval: number | null = null

onMounted(async () => {
    const gameCode = gameStore.gameData.gameCode
    if (!gameCode) return

    // Load Presets
    try {
        const res = await fetch(`${apiBase}/lobby/${gameCode}/boards/presets`)
        if (res.ok) presets.value = await res.json()
    } catch (err) {
        console.error('Presets failed', err)
    }

    // Load Current Settings
    try {
        const resEn = await fetch(`${apiBase}/game/get?code=${gameCode}`)
        const data = await resEn.json()
        maxEnergy.value = data.maxCollectableEnergy

        const resTime = await fetch(`${apiBase}/daten/cooldown`)
        time.value = await resTime.json()
    } catch (err) {
        console.error('Settings load failed', err)
    }
})

// --- Logic for Steppers ---
const startTimer = (type: 'time' | 'energy', action: 'increase' | 'decrease') => {
    if (interval) return
    const run = () => {
        if (type === 'time') {
            action === 'increase' ? time.value++ : time.value > 1 && time.value--
        } else {
            action === 'increase' ? maxEnergy.value++ : maxEnergy.value > 1 && maxEnergy.value--
        }
    }
    run()
    interval = window.setInterval(run, 160)
}

const stopTimer = (type: 'time' | 'energy') => {
    if (interval) {
        clearInterval(interval)
        interval = null
        // Sync to server immediately on release
        if (type === 'time') syncTime()
        else syncEnergy()
    }
}

const syncTime = () => fetch(`${apiBase}/daten/cooldown?seconds=${time.value}`, { method: 'POST' })
const syncEnergy = () => {
    fetch(`${apiBase}/game/updateSettings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            code: gameStore.gameData.gameCode,
            playerId: gameStore.gameData.playerId,
            maxEnergy: maxEnergy.value,
        }),
    })
}

// --- Board Logic ---
const formatBoardName = (n: string) =>
    n
        .replace('.json', '')
        .replace(/([A-Z])/g, ' $1')
        .trim()
const selectPreset = (p: string) => (selectedBoard.value = p)
const openEditor = () => {
    selectedBoard.value = 'custom'
    emit('openEditor')
}

const confirmAll = async () => {
    if (selectedBoard.value === 'custom') {
        emit('openEditor')
        return
    }

    loading.value = true
    try {
        const res = await fetch(
            `${apiBase}/lobby/${gameStore.gameData.gameCode}/board/select-preset?playerId=${gameStore.gameData.playerId}&presetName=${selectedBoard.value}`,
            { method: 'POST' },
        )
        if (res.ok) {
            emit('settingsUpdated')
            close()
        }
    } finally {
        loading.value = false
    }
}

const close = () => emit('close')
</script>

<style scoped>
/* Consolidating styles for a unified "Wooden" UI */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.85);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: #3d2b1f;
    background-image:
        linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%),
        repeating-linear-gradient(90deg,
            transparent,
            transparent 38px,
            rgba(0, 0, 0, 0.15) 39px,
            rgba(0, 0, 0, 0.15) 40px);
    border: 5px solid #2d1b0d;
    border-radius: 15px;
    padding: 25px;
    width: 95%;
    max-width: 550px;
    max-height: 90vh;
    overflow-y: auto;
    position: relative;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.8);
    font-family: 'Kanit', sans-serif;
}

.modal-title {
    color: #ffc107;
    text-align: center;
    margin-bottom: 20px;
    text-transform: uppercase;
    letter-spacing: 2px;
    text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.8);
}

.section-subtitle {
    color: #f0e2d0;
    font-size: 1rem;
    text-align: center;
    margin-bottom: 10px;
    text-transform: uppercase;
}

.settings-section {
    display: flex;
    justify-content: space-around;
    gap: 20px;
    margin-bottom: 20px;
}

.settings-control {
    display: flex;
    align-items: center;
    gap: 10px;
}

.valueDisplay {
    background: #f0e2d0;
    color: #2d1b0d;
    padding: 8px 15px;
    border-radius: 8px;
    font-weight: 900;
    min-width: 60px;
    text-align: center;
    border: 2px solid #2d1b0d;
}

.settingsButton {
    width: 40px;
    height: 40px;
    background: #4d3319;
    color: white;
    border: 2px solid #2d1b0d;
    border-bottom-width: 4px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 1.2rem;
}

.settingsButton:active {
    transform: translateY(2px);
    border-bottom-width: 2px;
}

.divider {
    border: 0;
    border-top: 2px solid #2d1b0d;
    margin: 20px 0;
}

.board-options {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 12px;
    margin-bottom: 25px;
}

.board-option {
    background: #4d3319;
    border: 3px solid #2d1b0d;
    border-radius: 10px;
    padding: 15px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
}

.board-option.selected {
    border-color: #ffc107;
    background: #634121;
    box-shadow: 0 0 15px #ffc10766;
}

.custom-option {
    background: #2d4d19;
}

.option-icon {
    font-size: 1.8rem;
    margin-bottom: 5px;
}

.option-name {
    color: #f0e2d0;
    font-size: 0.8rem;
    font-weight: bold;
}

.button-group {
    display: flex;
    gap: 15px;
}

.btn {
    flex: 1;
    padding: 12px;
    border-radius: 10px;
    border: 3px solid #1e3311;
    font-weight: 800;
    cursor: pointer;
    text-transform: uppercase;
}

.cancel-btn {
    background: #6d2d2d;
    border-color: #421a1a;
    color: white;
}

.select-btn {
    background: #2d4d19;
    color: #a7ff83;
}

.select-btn:disabled {
    opacity: 0.5;
}

.close-button {
    position: absolute;
    top: 10px;
    right: 10px;
    background: none;
    border: none;
    color: #ffc107;
    font-size: 1.5rem;
    cursor: pointer;
}
</style>
