<template>
  <div ref="container" class="dice3d-container"></div>
</template>

<script lang="ts">
import * as THREE from 'three'
import { defineComponent, onMounted, ref } from 'vue'

// Globale Variablen
let cube: THREE.Mesh
let scene: THREE.Scene              // dreidimensionale Szene
let camera: THREE.PerspectiveCamera // das „Auge“, mit dem wir die Bühne betrachten
let renderer: THREE.WebGLRenderer   // ist für die Darstellung der Szene auf dem Bildschirm verantwortlich

// Positionen der Kanten des Würfels
const faceRotations: Record<number, { x: number; y: number }> = {
  1: { x: Math.PI, y: 0 },
  2: { x: -Math.PI / 2, y: 0 },
  3: { x: 0, y: Math.PI / 2 },
  4: { x: 0, y: -Math.PI / 2 },
  5: { x: Math.PI / 2, y: 0 },
  6: { x: 0, y: 0 }
}

export function rollDice() {
  const result = Math.floor(Math.random() * 6) + 1
  console.log('Augenzahl:', result)

  const duration = 1300
  const start = performance.now()
  const target = faceRotations[result]!       // Winkel, damit die gewünschte Kante zur Kamera zeigt
  const startRotation = cube.rotation.clone() // aktuelle Position des Würfels (Kopie, um zu wissen, woher gedreht werden soll)

  // hinzufügen von paar extra Rotierungen des Würfels
  const randomTurns = {
    x: (Math.floor(Math.random() * 2) + 1) * Math.PI * 2,
    y: (Math.floor(Math.random() * 2) + 1) * Math.PI * 2
  }

  // Bestimmen der Endposition der Animation
  const endRotation = new THREE.Euler(
    target.x + randomTurns.x,
    target.y + randomTurns.y,
    0
  )

  const minScale = 0.3
  const endScale = 1

  function animate(now: number) {
    const elapsed = now - start
    const t = Math.min(elapsed / duration, 1)
    const eased = easeInOutCubic(t)

    // Maschtabieren
    let s: number

    if (t < 0.5) {
      // Erste Hälfte der Animation. Der Würfel wird kleiner
      s = 1 - (1 - minScale) * (t * 2)
    } else {
      // Zweite Hälfte. Der Würfel kehrt zu seiner normalen Größe zurück
      s = minScale + (endScale - minScale) * ((t - 0.5) * 2)
    }

    cube.scale.set(s, s, s)


    // Rotation des Würfels
    cube.rotation.x = startRotation.x + (endRotation.x - startRotation.x) * eased
    cube.rotation.y = startRotation.y + (endRotation.y - startRotation.y) * eased
    cube.rotation.z = 0

    renderer.render(scene, camera)
    if (t < 1) requestAnimationFrame(animate)
  }

  requestAnimationFrame(animate)
}

// Funktion für sanfte Bewegung
function easeInOutCubic(t: number) {
  return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
}

export default defineComponent({
  name: 'Dice3D',
  setup() {
    const container = ref<HTMLDivElement | null>(null)

    onMounted(() => {
      scene = new THREE.Scene()
      camera = new THREE.PerspectiveCamera(75, 1, 0.1, 1000)
      camera.position.z = 5

      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
      renderer.setClearColor(0x000000, 0)
      renderer.setSize(400, 400)
      container.value!.appendChild(renderer.domElement)

      const textureOrder = [4, 3, 5, 2, 6, 1]
      const materials = textureOrder.map(i =>
        new THREE.MeshBasicMaterial({
          map: new THREE.TextureLoader().load(`/dice/dice-${i}.png`)
        })
      )

      const geometry = new THREE.BoxGeometry(2, 2, 2)
      cube = new THREE.Mesh(geometry, materials)
      scene.add(cube)

      animate()
    })

    function animate() {
      requestAnimationFrame(animate)
      renderer.render(scene, camera)
    }

    return { container }
  }
})
</script>
