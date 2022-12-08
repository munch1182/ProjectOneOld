
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
                { name: 'axum', version: '0.6' },
                { name: 'tokio', version: '1', features: ['full'] },
                { name: 'serde', version: '1', features: ['derive'] },
                { name: 'serde_json', version: '1' }
            ], "axum")
    ]
}
