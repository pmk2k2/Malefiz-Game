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
    }


    return {info: readonly(info), loescheInfo, setzeInfo};
}