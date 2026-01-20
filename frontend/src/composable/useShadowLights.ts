import { DirectionalLight } from "three";
import { shallowRef } from "vue";

const staticLightRef = shallowRef<DirectionalLight>()
const dynamicLightRef = shallowRef<DirectionalLight>()

let timeout: any = null

export function useShadowLights() {
    function calculateStaticShadows() {
        // Wenn kein licht vorhanden
        if(!staticLightRef.value) {
            console.log("SCHATTEN: Kein Licht vorhanden")
            return
        }

        // wenn schatten schon berechnet werden, prozess abbrechen
        if(timeout) clearTimeout(timeout)

        // Warte 200ms bis andere models geladen werden
        timeout = setTimeout(() => {
            console.log("Backe backe Schatten, das Lichtlein hat gerufen")
            staticLightRef.value!.shadow.autoUpdate = false
            staticLightRef.value!.shadow.needsUpdate = true 
        }, 500)
    }

    return {
        staticLightRef,
        dynamicLightRef,
        calculateStaticShadows
    }
}