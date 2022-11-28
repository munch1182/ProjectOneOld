import { cmd, fileUpdate, fileCopy } from "../../help.js";
import { EOL } from "os";
import path from "path";

export default function (currDir, targetDir, name) {
    const templateDir = path.join(currDir, 'template');
    return [
        cmd(`cd ${targetDir} && npm install -D tailwindcss postcss autoprefixer`),
        cmd(`cd ${targetDir} && npx tailwindcss init -p`),
        fileCopy(path.join(templateDir), path.join(targetDir)),
        fileUpdate('update css', path.join(targetDir, 'src', 'style.css'), async (rl, fos) => {
            fos.write('@tailwind base;');
            fos.write(EOL);
            fos.write('@tailwind components;');
            fos.write(EOL);
            fos.write('@tailwind utilities;');
            fos.write(EOL);

            for await (const line of rl) {
                fos.write(line);
                fos.write(EOL);
            }
        }),
    ]
}