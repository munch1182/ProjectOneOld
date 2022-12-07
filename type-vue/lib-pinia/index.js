
import * as helper from '../../help.js';
import path from 'path';

const judge = "import './style.css'";
const imp = "import { createPinia } from 'pinia'";

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.exe_cmd(`cd ${arg.targetDir} && ${arg.pm} i pinia`),
        helper.exe_replace(path.join(arg.targetDir, 'src', "main.ts"), [
            { reg: judge, str: `${judge}\n${imp}` },
            { reg: ".mount('#app')", str: ".use(createPinia()).mount('#app')" },
        ])
    ]
}