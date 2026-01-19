<template>
    <div>
        <!-- Pause Button - Fixed in top-right corner -->
        <button class="pause-btn" @click="openPauseMenu" title="Pause (ESC)">
            ⏸
        </button>

        <!-- Pause Menu Overlay -->
        <div v-if="showPauseMenu" class="pause-overlay" @click.self="closePauseMenu">
            <div class="pause-modal">
                <h2 class="pause-title">Pause</h2>

                <div class="pause-buttons">
                    <button class="btn continue-btn" @click="closePauseMenu">
                        Weiterspielen
                    </button>

                    <button class="btn export-btn" @click="exportBoard">
                        Board exportieren
                    </button>

                    <button class="btn rules-btn" @click="toggleRules">
                        Spielregeln
                    </button>

                    <button class="btn settings-btn" @click="handleSettings">
                        Einstellungen
                    </button>

                    <button class="btn leave-btn" @click="openLeaveConfirmation">
                        Spiel verlassen
                    </button>
                </div>
            </div>
        </div>

        <!-- Rules View Overlay -->
        <div v-if="showRules" class="rules-overlay" @click.self="toggleRules">
            <div class="rules-container">
                <button class="close-rules-btn" @click="toggleRules">✕</button>
                <InfoView @close="toggleRules" />
            </div>
        </div>

        <!-- Leave Confirmation Dialog -->
        <div v-if="showLeaveConfirmation" class="confirmation-overlay" @click.self="closeLeaveConfirmation">
            <div class="confirmation-modal">
                <h3 class="confirmation-title">Spiel wirklich verlassen?</h3>
                <p class="confirmation-text">
                    Du wirst das laufende Spiel verlassen und zur Hauptmenü zurückkehren.
                </p>

                <div class="confirmation-buttons">
                    <button class="btn cancel-btn" @click="closeLeaveConfirmation">
                        Abbrechen
                    </button>
                    <button class="btn confirm-leave-btn" @click="confirmLeave">
                        Verlassen
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '@/stores/gamestore'
import InfoView from '@/components/InfoView.vue'
import { useInfo } from '@/composable/useInfo'

const router = useRouter()
const gameStore = useGameStore()

const showPauseMenu = ref(false)
const showRules = ref(false)
const showLeaveConfirmation = ref(false)
const { nachrichten, loescheInfo, setzeInfo } = useInfo()

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

function openPauseMenu() {
    showPauseMenu.value = true
}

function closePauseMenu() {
    showPauseMenu.value = false
}

function toggleRules() {
    showRules.value = !showRules.value
}

function handleSettings() {
    // Placeholder for future settings functionality
    console.log('Settings clicked - not implemented yet')
    alert('Einstellungen sind noch nicht verfügbar')
}

function openLeaveConfirmation() {
    showLeaveConfirmation.value = true
}

function closeLeaveConfirmation() {
    showLeaveConfirmation.value = false
}

async function exportBoard() {
    const { gameCode } = gameStore.gameData

    if (!gameCode) {
        alert('Kein Spielcode gefunden')
        return
    }

    try {
        const response = await fetch(`${API_BASE_URL}/board?code=${gameCode}`)

        if (!response.ok) {
            throw new Error('Failed to fetch board')
        }

        const boardData = await response.json()

        if (!boardData) {
            alert('Kein Board gefunden')
            return
        }

        // Format the board data to match the exact structure of SmallerBoard.json
        const formattedBoard = {
            cols: boardData.cols,
            rows: boardData.rows,
            grid: boardData.grid.map((row: any[]) =>
                row.map((cell: any) => ({
                    i: cell.i,
                    j: cell.j,
                    type: cell.type
                }))
            )
        }


        // Convert the formattedBoard object to a JSON string
        const jsonString = JSON.stringify(formattedBoard, null, 2);

        // Create blob from JSON string
        const blob = new Blob([jsonString], { type: 'application/json' });

        // Generate filename with incrementing number
        const filename = getNextFilename()

        // Create download link
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = filename

        // Trigger download
        document.body.appendChild(link)
        link.click()

        // Cleanup
        document.body.removeChild(link)
        URL.revokeObjectURL(url)

        console.log(`Board exported as ${filename}`)
    } catch (error) {
        console.error('Error exporting board:', error)
        alert('Fehler beim Exportieren des Boards')
    }
}

function getNextFilename(): string {
    // Check localStorage for last used number
    const lastNumber = localStorage.getItem('lastExportNumber')
    const nextNumber = lastNumber ? parseInt(lastNumber) + 1 : 1

    // Save the new number
    localStorage.setItem('lastExportNumber', nextNumber.toString())

    return `CustomBoard${nextNumber}.json`
}

async function confirmLeave() {
    const { playerId, gameCode } = gameStore.gameData

    if (playerId && gameCode) {
        try {
            await fetch(`${API_BASE_URL}/game/leave`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    playerId,
                    code: gameCode,
                }),
            })

            gameStore.disconnect()
            gameStore.resetGameCode()

            router.push('/main')
        } catch (error) {
            console.error('Error leaving game:', error)
            // Still navigate away even if the request fails
            gameStore.resetGameCode()
            router.push('/main')
        }
    } else {
        // No game data, just go back
        router.push('/main')
    }
}

// ESC key to toggle pause menu
function handleKeyPress(event: KeyboardEvent) {
    if (event.key === 'Escape') {
        // If confirmation dialog is open, close it
        if (showLeaveConfirmation.value) {
            closeLeaveConfirmation()
        }
        // If rules are open, close them
        else if (showRules.value) {
            toggleRules()
        }
        // Otherwise toggle pause menu
        else {
            showPauseMenu.value = !showPauseMenu.value
        }
    }
}

onMounted(() => {
    window.addEventListener('keydown', handleKeyPress)
})

onUnmounted(() => {
    window.removeEventListener('keydown', handleKeyPress)
})
</script>

<style scoped>
/* Pause Button */
.pause-btn {
    position: fixed;
    top: 20px;
    right: 20px;
    width: 55px;
    height: 55px;
    border-radius: 12px;
    background-color: #3d2b1f;
    background-image:
        linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent),
        repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0, 0, 0, 0.1) 39px, rgba(0, 0, 0, 0.1) 40px);
    border: 3px solid #2d1b0d;
    border-bottom-width: 6px;
    color: #f0e2d0;
    font-size: 1.5rem;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
    z-index: 60;
}

.pause-btn:hover {
    transform: translateY(-2px);
    filter: brightness(1.15);
}

.pause-btn:active {
    transform: translateY(2px);
    border-bottom-width: 3px;
}

/* Pause Overlay */
.pause-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.85);
    backdrop-filter: blur(8px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 150;
}

.pause-modal {
    background-color: #3d2b1f;
    background-image:
        linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent),
        repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0, 0, 0, 0.1) 39px, rgba(0, 0, 0, 0.1) 40px);
    border: 6px solid #2d1b0d;
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.8), inset 0 0 20px rgba(0, 0, 0, 0.5);
    padding: 40px 50px;
    min-width: 400px;
    text-align: center;
}

.pause-title {
    font-family: 'Kanit', sans-serif;
    font-size: 2.5rem;
    font-weight: 800;
    color: #f0e2d0;
    text-transform: uppercase;
    letter-spacing: 3px;
    margin: 0 0 30px 0;
    text-shadow: 3px 3px 0px rgba(0, 0, 0, 0.8);
}

.pause-buttons {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

/* Button Styles */
.btn {
    width: 100%;
    padding: 12px 0;
    font-size: 1.1rem;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 1.5px;
    border-radius: 12px;
    border: 3px solid;
    border-bottom-width: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
    text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.5);
}

.btn:hover {
    transform: translateY(-2px);
    filter: brightness(1.15);
}

.btn:active {
    transform: translateY(2px);
    border-bottom-width: 3px;
}

.continue-btn {
    background-color: #2d4d19;
    border-color: #1e3311;
    color: #a7ff83;
}

.export-btn {
    background-color: #1a4d6d;
    border-color: #0f2d3d;
    color: #83d7ff;
}

.rules-btn {
    background-color: #4d3319;
    border-color: #3d2b1f;
    color: #f0e2d0;
}

.settings-btn {
    background-color: #634121;
    border-color: #3d2b1f;
    color: #ffcc66;
}

.leave-btn {
    background-color: #6d2d2d;
    border-color: #421a1a;
    color: #ff8a80;
}

/* Rules Overlay */
.rules-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.9);
    backdrop-filter: blur(10px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 200;
}

.rules-container {
    position: relative;
    width: 85vw;
    height: 85vh;
    max-width: 900px;
    background: #1a1a1a;
    border: 8px solid #3d2b1f;
    border-radius: 15px;
    box-shadow: 0 0 50px rgba(0, 0, 0, 1);
    overflow: auto;
    padding: 20px;
}

.close-rules-btn {
    position: absolute;
    top: 15px;
    right: 15px;
    width: 45px;
    height: 45px;
    background: #6d2d2d;
    color: white;
    border: 3px solid #421a1a;
    border-radius: 50%;
    font-weight: bold;
    cursor: pointer;
    z-index: 210;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
    transition: transform 0.2s;
}

.close-rules-btn:hover {
    transform: scale(1.1) rotate(90deg);
    background: #a33535;
}

/* Confirmation Dialog */
.confirmation-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.9);
    backdrop-filter: blur(10px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 250;
}

.confirmation-modal {
    background-color: #3d2b1f;
    background-image:
        linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent),
        repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0, 0, 0, 0.1) 39px, rgba(0, 0, 0, 0.1) 40px);
    border: 6px solid #6d2d2d;
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.9), inset 0 0 20px rgba(0, 0, 0, 0.5), 0 0 30px rgba(224, 59, 59, 0.3);
    padding: 40px 50px;
    min-width: 450px;
    text-align: center;
}

.confirmation-title {
    font-family: 'Kanit', sans-serif;
    font-size: 1.8rem;
    font-weight: 800;
    color: #ff8a80;
    text-transform: uppercase;
    margin: 0 0 20px 0;
    text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.8);
}

.confirmation-text {
    color: #f0e2d0;
    font-size: 1rem;
    margin-bottom: 30px;
    line-height: 1.5;
}

.confirmation-buttons {
    display: flex;
    gap: 15px;
}

.cancel-btn {
    background-color: #4d3319;
    border-color: #3d2b1f;
    color: #f0e2d0;
}

.confirm-leave-btn {
    background-color: #6d2d2d;
    border-color: #421a1a;
    color: #ff8a80;
}

.confirm-leave-btn:hover {
    background-color: #8d3d3d;
}
</style>