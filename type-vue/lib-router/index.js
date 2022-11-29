import { cmd, fileCopy, fileUpdate } from "../../help.js";
import { EOL } from "os";
import path from 'path';

const MAIN_TS_SPLIT = 'mount(';
const IMPORT_ROUTER_JUDGE = "import './style.css'";
const IMPORT_ROUTER = "import router from './router'";

export default function (currDir, targetDir, arg) {
    const templateDir = path.join(currDir, 'template');
    return [
        cmd(`cd ${targetDir} && ${arg.pm} i vue-router@4`),
        // 复制src
        fileCopy(path.join(templateDir), path.join(targetDir)),
        // 使用router.ts
        fileUpdate('use router.ts', path.join(targetDir, 'src', 'main.ts'), async (rl, fos) => {
            for await (const line of rl) {
                let str = line
                // import
                if (line.includes(IMPORT_ROUTER_JUDGE)) {
                    fos.write(IMPORT_ROUTER); // 在style.css的引入的上一行引入router
                    fos.write(EOL);
                    // use
                } else if (line.includes(MAIN_TS_SPLIT)) {
                    const index = line.indexOf(MAIN_TS_SPLIT);
                    str = `${line.substring(0, index)}use(router).${line.substring(index)}` // 增加use
                }
                fos.write(str);
                fos.write(EOL);
            }
        })
    ]
}