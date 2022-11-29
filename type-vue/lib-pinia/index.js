import { cmd, fileUpdate } from "../../help.js";
import { EOL } from "os";
import path from "path";

const MAIN_TS_SPLIT = 'mount(';
const IMPORT_PINIA_JUDGE = "import './style.css'";
const IMPORT_PINIA = "import { createPinia } from 'pinia';";

export default function (_, targetDir, arg) {
    return [
        cmd(`cd ${targetDir} && ${arg.pm} install pinia`),
        fileUpdate('use pinia', path.join(targetDir, 'src', 'main.ts'), async (rl, fos) => {
            for await (const line of rl) {
                let str = line
                if (typeof line === 'string') {
                    // import
                    if (line.includes(IMPORT_PINIA_JUDGE)) {
                        fos.write(IMPORT_PINIA); // 在style.css的引入的上一行引入router
                        fos.write(EOL);
                        // use
                    } else if (line.includes(MAIN_TS_SPLIT)) {
                        const index = line.indexOf(MAIN_TS_SPLIT);
                        str = `${line.substring(0, index)}use(createPinia()).${line.substring(index)}` // 增加use
                    }
                }
                fos.write(str);
                fos.write(EOL);
            }
        })
    ]
}