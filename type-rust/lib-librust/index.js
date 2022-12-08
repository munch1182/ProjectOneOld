
import * as helper from '../tomlHelp.js';
import path from "path";

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.tomlDep(path.join(arg.targetDir, 'Cargo.toml'),
            [
                { name: 'liblib', git: "https://github.com/munch1182/librust.git" },
            ], "librust")
    ]
}
