
import * as helper from '../../help.js';
import path from "path";

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.exe_json(path.join(arg.targetDir, "package.json"), j => {
            const main = j.main;
            j.scripts.dev = `node ${main}`;
            const test = j.scripts.test;
            delete j.scripts.test;
            j.scripts.test = test;
            return j
        }),
        helper.exe_copy(path.join(arg.libRoot, "template"), arg.targetDir)
    ]
}
