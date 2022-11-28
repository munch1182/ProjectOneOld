import fs from 'fs';
import path from 'path';
import prompts from 'prompts';
import readline from 'readline';
import { yellow } from 'kolorist';
import shell from "shelljs";

/**
 * 删除文件或者文件夹
 *  
 * @param {string} file 
 */
function empty(file) {
    if (!fs.existsSync(file)) {
        return;
    }
    if (!fs.lstatSync(file).isDirectory()) {
        fs.unlinkSync(file);
        return;
    }
    for (const f of fs.readdirSync(file)) {
        const abs = path.resolve(file, f);
        if (fs.statSync(abs).isDirectory()) {
            empty(abs);
            fs.rmdirSync(abs);
        } else {
            fs.unlinkSync(abs);
        }
    }
}

/**
 * 
 * @param {string} src 源文件或者文件夹
 * @param {string} dest 目标文件夹
 */
function copy(src, dest) {
    const stat = fs.statSync(src);
    if (stat.isDirectory()) {
        copyDir(src, dest);
    } else {
        fs.copyFileSync(src, dest);
    }
}

/**
 * 
 * @param {string} src 源文件或者文件夹
 * @param {string} dest 目标文件夹
 */
function copyDir(src, dest) {
    fs.mkdirSync(dest, { recursive: true });
    for (const f of fs.readdirSync(src)) {
        const s = path.resolve(src, f);
        const d = path.resolve(dest, f);
        copy(s, d);
    }
}

/**
 * 
 * @param {string} f 确保该文件存在 
 */
export function fileNew(f) {
    return {
        "desc": `check ${f}`,
        "exec": async () => {
            if (fs.existsSync(f)) {
                const { del } = await prompts([
                    {
                        type: 'confirm',
                        name: 'del',
                        message: yellow(`${f} exists, sure to del?`),
                    }
                ], {
                    onCancel: () => console.log(err("cancel."))
                });
                if (del) {
                    empty(f);
                }
                return del;
            }
            return true;
        }
    }
}

export function cmd(cmd) {
    return {
        "desc": `${cmd}`,
        "exec": async () => {
            return shell.exec(cmd).code == 0;
        }
    }
}

/**
 * 
 * @param {string} src 复制src的文件
 * @param {string} dest 到dest文件夹中
 * @returns 
 */
export function fileCopy(src, dest) {
    return {
        "desc": `add or set ${path.basename(src)}`,
        "exec": async () => {
            copy(src, dest);
            return true;
        }
    }
}

/**
 * 
 * @param {string} f 删除文件或者文件夹
 * @returns 
 */
export function fileDel(f) {
    return {
        "desc": `del ${path.basename(f)}`,
        "exec": async () => {
            empty(f);
            return true;
        }
    }
}

/**
 * @param {string} file 要更新的文件
*  @param {funtion: (string, fs.WriteStream) -> ()} lineback 源文件逐行和更改缓存文件回调 
 * @returns 
 */
export function fileUpdateLine(desc, file, lineback) {
    return fileUpdate(desc, file, async (rl, fos) => {
        for await (const line of rl) {
            await lineback(line, fos);
        }
    })
}


/**
 * @param {string} file 要更新的文件
 * @param {funtion: (readline.Interface, fs.WriteStream) -> ()} lineback 源文件和更改缓存文件回调 
 * @returns 
 */
export function fileUpdate(desc, file, callback) {
    return {
        "desc": desc,
        "exec": async () => {
            const fos = fs.createWriteStream(`${file}_bak`)
            const rl = readline.createInterface({
                input: fs.createReadStream(file),
                crlfDelay: Infinity,
                terminal: false
            });
            await callback(rl, fos);
            fos.close();
            empty(file);
            fs.renameSync(`${file}_bak`, file);
            return true;
        }
    }
}

/**
 * @param {string} file 要更新的文件
 * @param {string} content 要写入的内容
 * @returns 
 */
export function fileReplace(file, content) {
    return {
        "desc": `update ${path.basename(file)}`,
        "exec": async () => {
            fs.writeFileSync(file, content);
            return true;
        }
    }
}
