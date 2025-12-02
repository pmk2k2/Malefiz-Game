import {ref,  reactive, readonly } from 'vue'

const info = reactive<{ inhalt: string}>(
    {inhalt : ""}
)

export function useInfo(){

    function loescheInfo(){
        info.inhalt = ""
    }

    function setzeInfo(nachricht: string){
        info.inhalt = nachricht

        setTimeout(() => {
        info.inhalt = "";
        }, 3000); //Infobox verschwindet nach 3 sekunden
    }


    return {info: readonly(info), loescheInfo, setzeInfo};
}
