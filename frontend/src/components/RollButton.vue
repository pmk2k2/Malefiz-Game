<template>
  <button
    class="roll-button"
    :class="{ 'is-loading': isLoading }"
    :disabled="isLoading"
    @click="handleClick"
  >
    <div class="button-content">
      <span v-if="!isLoading" class="roll-text">
        Würfeln
      </span>
      <span v-else class="loader">
        <span class="hourglass">⏳</span>
      </span>
    </div>
  </button>
</template>

<script setup lang="ts">

const props = defineProps<{
  isLoading: boolean
}>()


const emit = defineEmits(['trigger'])

function handleClick() {
  //Wenn props.isLoading true, dann läuft cooldwon und es darf nicht gewürfelt werden
  if (!props.isLoading){
    emit('trigger')
  }
}
</script>

<style scoped>
.roll-button {
  position: relative;
  width: 280px;
  height: 80px;
  cursor: pointer;
  
  
  background-color: #4d3319;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    repeating-linear-gradient(90deg, transparent, transparent 40px, rgba(0,0,0,0.1) 41px, rgba(0,0,0,0.1) 42px);
  border: 4px solid #ffc107;
  border-bottom: 8px solid #b8860b;
  border-radius: 40px; 
  color: #f0e2d0;
  font-family: 'Kanit', sans-serif;
  transition: all 0.15s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.4);
  outline: none;
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.roll-text {
  font-size: 1.6rem;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.dice-icon {
  margin-right: 10px;
  filter: drop-shadow(0 0 5px rgba(255, 193, 7, 0.5));
}


.roll-button:not(:disabled):hover {
  filter: brightness(1.1);
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(255, 193, 7, 0.2);
}


.roll-button:not(:disabled):active {
  transform: translateY(4px);
  border-bottom-width: 2px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
}


.roll-button:disabled {
  background-color: #2c1e10;
  border-color: #5d4a1f;
  border-bottom-width: 4px;
  cursor: not-allowed;
  filter: grayscale(0.5);
  opacity: 0.8;
}

.loader {
  display: flex;
  align-items: center;
  justify-content: center;
}

.hourglass {
  font-size: 2rem;
  animation: rotateHourglass 1.5s infinite linear;
}

@keyframes rotateHourglass {
  0% { transform: rotate(0deg); }
  50% { transform: rotate(180deg); }
  100% { transform: rotate(360deg); }
}
</style>