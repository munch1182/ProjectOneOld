import path from "path";
import { cmd, fileCopy } from "../help.js";

export default async function (currDir, targetDir, arg) {
    const name = arg.projectName;
    const templateDir = path.join(currDir, 'template');
    return [
        cmd(`cargo init ${name}`), //当前是要创建项目的文件夹, 项目文件夹还未创建
        // 更改其它设置
        fileCopy(path.join(templateDir), path.join(targetDir)),
    ]
}