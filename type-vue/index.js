import * as helper from '../help.js';
import path from "path";
import fs from "fs";
/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        // 创建项目
        helper.exe_createProject(`${arg.pm} create vite@latest ${arg.projectName} -- --template vue-ts`),
        // 写入gitignore
        helper.exe_write(path.join(arg.targetDir, '.gitignore'), fs.readFileSync(path.join(arg.typeRoot, '_gitignore'), { encoding: 'utf-8' })),
        helper.exe_write(path.join(arg.targetDir, 'README.md'), `# ${arg.projectName}`),
        helper.exe_copy(path.join(arg.typeRoot, "template"), arg.targetDir),
        // 删除文件
        helper.exe_del([
            path.join(arg.targetDir, '.vscode'),
            path.join(arg.targetDir, 'src', 'assets'),
            path.join(arg.targetDir, 'src', 'components', 'HelloWorld.vue')
        ]),
        helper.exe_replace(path.join(arg.targetDir, "index.html"), { reg: /<title>.*<\/title>/, str: `<title>${arg.projectName}<title>` })
    ]
}