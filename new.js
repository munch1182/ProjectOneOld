import minimist from 'minimist';
import path from 'path';
import { PREFIX_TYPE, PREFIX_LIB } from "./c.js";
import { newfile } from './help.js';

// node new [type] [lib]
const argv = minimist(process.argv.slice(2));
const curr = process.cwd();

const type = argv._[0];
const lib = argv._[1];


if (type) {

    const typePath = path.join(curr, PREFIX_TYPE + type);

    newfile(typePath);
    newfile(path.join(typePath, 'index.js'));

    if (lib) {
        const libPath = path.join(typePath, PREFIX_LIB + lib);
        newfile(libPath);
        newfile(path.join(libPath, 'index.js'));
        newfile(path.join(libPath, '_gitignore'));
        newfile(path.join(libPath, 'template'));
    }

    console.log("success");
}

