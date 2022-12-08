
import * as helper from '../help.js';
import prompts from 'prompts';
import path from 'path';
import shell from 'shelljs';
/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default async function (arg) {
    const result = [
        helper.exe_createProject(`mkdir ${arg.targetDir} && cd ${arg.targetDir} && ${arg.pm} init -y`),
        // 写入gitignore
        helper.exe_write(path.join(arg.targetDir, '.gitignore'), fs.readFileSync(path.join(arg.typeRoot, '_gitignore'), { encoding: 'utf-8' })),
    ];
    const author = await _askAuthor(arg);
    if (author) {
        result.push(author);
    }
    return result;

}

async function _askAuthor(arg) {
    const userResult = shell.exec("git config user.name"); //尝试获取git name
    const user = userResult.code == 0 ? userResult.stdout.trim() : undefined;
    const result = await prompts({
        type: 'text',
        name: 'author',
        message: helper.warn("set author: "),
        initial: user
    }, {
        onCancel: () => console.log(helper.err("cancel."))
    });
    if (!result.author) return undefined;
    return helper.exe_json(path.join(arg.targetDir, "package.json"), j => {
        j.author = result.author;
        return j
    })
}
