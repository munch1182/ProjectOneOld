import { EOL } from "os";
import path from "path";
import { cmd, fileCopy, fileDel, fileReplace, fileUpdateLine } from "../help.js";

export default function (currDir, targetDir, arg) {
    const name = arg.projectName;
    const templateDir = path.join(currDir, 'template');
    return [
        cmd(`${arg.pm} create vite@latest ${name} -- --template vue-ts`),
        // 删除HelloWorld.vue
        fileDel(path.join(targetDir, 'src', 'components', 'HelloWorld.vue')),
        fileDel(path.join(targetDir, '.vscode')),
        fileDel(path.join(targetDir, 'src', 'assets', 'vue.svg')),
        // 更改其它设置
        fileCopy(path.join(templateDir), path.join(targetDir)),
        // 更改index.html的title
        fileUpdateLine('update title', path.join(targetDir, 'index.html'), async (line, fos) => {
            let str = line;
            if (line.includes("<title>")) {
                str = `    <title>${name}</title>`;
            }
            fos.write(str);
            fos.write(EOL);
        }),
        fileReplace(path.join(targetDir, `README.md`), `${name}`)
    ]
}