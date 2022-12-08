
import * as helper from '../help.js';
import path from "path";
import fs from "fs";

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.exe_cmd(`cargo init ${arg.projectName}`),
        // 写入gitignore
        helper.exe_write(path.join(arg.targetDir, '.gitignore'), fs.readFileSync(path.join(arg.typeRoot, '_gitignore')).toString()),
    ]
}
