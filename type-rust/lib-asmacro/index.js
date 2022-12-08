
import * as helper from '../tomlHelp.js';
import path from "path";
import { inline, Section } from '@ltd/j-toml';

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.tomlDep(path.join(arg.targetDir, 'Cargo.toml'),
            [
                { name: 'proc-macro2', version: "1.0" },
                { name: 'quote', version: "1.0" },
                { name: 'syn', version: "1.0" },
            ], "macro"),
        helper.exe_toml(path.join(arg.targetDir, 'Cargo.toml'), async (toml) => {
            const any = {};
            any['proc-macro'] = true;
            toml.lib = Section(any);
            return toml;
        })
    ]
}
