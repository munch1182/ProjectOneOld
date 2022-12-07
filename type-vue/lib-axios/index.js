
import * as helper from '../../help.js';
import path from 'path';

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.exe_cmd(`cd ${arg.targetDir} && ${arg.pm} i axios`),
    ]
}
