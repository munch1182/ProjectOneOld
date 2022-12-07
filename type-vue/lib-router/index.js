
import * as helper from '../../help.js';
import path from 'path';

const judge = "import './style.css'";
const imp = "import router from './router'";

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.exe_cmd(`cd ${arg.targetDir} && ${arg.pm} i vue-router@4`),
        helper.exe_copy(path.join(arg.libRoot, "template"), arg.targetDir),
        helper.exe_replace(path.join(arg.targetDir, 'src', "main.ts"), [
            { reg: judge, str: `${judge}\n${imp}` },
            { reg: ".mount('#app')", str: ".use(router).mount('#app')" },
        ])
    ]
}
