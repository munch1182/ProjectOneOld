import minimist from 'minimist';
import path from 'path';
import fs from 'fs';
import { existfile } from './help.js';

const PREFIX_TYPE = "type-";
const PREFIX_LIB = "lib-";

// node new [type] [lib]

// node new vue router && node new vue tailwind && node new vue pinia && node new vue axios && node new rust json && node new rust axum && node new rust librust && node new node
const argv = minimist(process.argv.slice(2));
const curr = process.cwd();

const type = argv._[0];
const lib = argv._[1];

const typeindex = `
import * as helper from '../help.js';

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
    ]
}
`
const libindex = `
import * as helper from '../../help.js';

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
    ]
}
`

if (type) {
    const typePath = path.join(curr, PREFIX_TYPE + type);

    existfile(typePath);
    existfile(path.join(typePath, 'index.js'));
    existfile(path.join(typePath, '_gitignore'));
    existfile(path.join(typePath, 'template'));
    fs.writeFileSync(path.join(typePath, 'index.js'), typeindex);

    if (lib) {
        const libPath = path.join(typePath, PREFIX_LIB + lib);
        existfile(libPath);
        existfile(path.join(libPath, 'template'));
        existfile(path.join(libPath, 'index.js'));
        fs.writeFileSync(path.join(libPath, 'index.js'), libindex);
    }

    console.log("success");
}