<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{ maxEnergy: number, currentEnergy: number }>(),
  { maxEnergy: 10, currentEnergy: 0 }
)

// Fuellung in % von 0 bis 100 ausrechnen
const filledSegments = computed(() => Math.max(0, Math.min(100, props.currentEnergy / props.maxEnergy) * 100))

// gucken, ob Leiste voll (fuer Einfaerbung)
const isFull = computed(() => {
  return props.currentEnergy >= props.maxEnergy
})
</script>

<template>
  <div class="leiste backdrop-blur-sm">
    <div
      class="leiste-fuellung"
      :class="{ 'is-full': isFull }"
      :style="{ height: filledSegments + '%' }">
    </div>
  </div>
  <div class="label font-bold">
    Sprung
  </div>
</template>

<style scoped>
  .label {
    /* TODO: Anpassen an restlichem Design */
    color: #f0e2d0;
    font-size: 1.6rem;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 1px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
  }

  .leiste {
    width: 4em;
    height: 20em;
    position: relative;

    background-color: rgba(25, 180, 200, 0.25);
    box-shadow: 0 4px 6px rgba(0,0,0,0.3);

    border: .3em solid hsl(180 20% 45%);
    border-radius: 10px;
    overflow: hidden;
  }

  .leiste-fuellung {
    /* Positionierung innerhalb der Bar richtig setzen */
    width: 100%;
    position: absolute;
    bottom: 0;
    left: 0;

    /* Verlauf als Fuellung */
    background: linear-gradient(to top, #ff8c00, #ffb700);

    /* Animation des Balkens bei Aenderung */
    transition: height 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  }

  .leiste-fuellung.is-full {
    background: linear-gradient(to top, #4dc92a, #46dc52);

    /* Leuchten der Fuellung */
    box-shadow: 0 0 15px #00ff00;
  }
</style>

