import path from "path";
import fs from "fs";
import { cmd, fileReplace } from "../help.js";

export default async function (currDir, targetDir, arg) {
    const name = arg.projectName;
    const gitfile = fs.readFileSync(path.join(currDir, '_gitignore'));
    return [
        cmd(`cargo init ${name}`), //当前是要创建项目的文件夹, 项目文件夹还未创建
        fileReplace(path.join(targetDir, '.gitignore'), gitfile), // npm不会打包.gitignore文件
    ]
}