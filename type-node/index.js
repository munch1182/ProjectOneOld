import shell from "shelljs";
import path from 'path';
import fs from 'fs';
import prompts from 'prompts';
import { cmd, fileCopy, fileJson, err, warn } from "../help.js";

export default async function (currDir, targetDir, arg) {
    const hsTsNode = shell.which('ts-node')?.code == 0; // 是否已全局安装ts-node
    const templateDir = path.join(currDir, 'template');

    const userResult = shell.exec("git config user.name"); //尝试获取git name
    const user = userResult.code == 0 ? userResult.stdout.trim() : undefined;

    // 因为ts是js的超集, 且使用ts时对项目本身无额外增加, ts-node也能编译js, 所以默认使用ts
    let isTs = true;
    if (!hsTsNode) {
        const response = await prompts([
            {
                type: 'confirm',
                name: 'install',
                message: warn(`${arg.pm} i -g ts-node, sure?`),
            }
        ], {
            onCancel: () => { console.log(err("cancel.")); return false; }
        });

        if (response.install == undefined) { // cancel
            return [];
        }
        if (!response.install) {
            isTs = false;
        }
    }
    const indexName = isTs ? 'index.ts' : 'index.js';
    if (!fs.existsSync(targetDir)) {
        fs.mkdirSync(targetDir, { recursive: true });
    }
    // 没有ts-node, 生成js项目, 有ts-node, 生成ts项目
    return [
        cmd(`cd ${targetDir} && ${arg.pm} init -y`),
        fileCopy(path.join(templateDir, indexName), path.join(targetDir, indexName)),
        fileCopy(path.join(templateDir, '.gitignore'), path.join(targetDir, '.gitignore')),
        fileJson(path.join(targetDir, 'package.json'), (any) => {
            user ? any.author = user : null;
            isTs ? null : any.type = `module`; //ts不能使用type module
            any.main = indexName;
            any.scripts.dev = `${isTs ? 'ts-node' : 'node'} ${indexName}`;
            return any;
        }),
    ]
}