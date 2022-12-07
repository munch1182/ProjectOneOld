import fs from 'fs';
import { yellow, cyan, red } from 'kolorist';
import path from 'path';
import shell from "shelljs";

export const desc = (s) => cyan(s);
export const err = (s) => red(s);
export const warn = (s) => yellow(s);

/**
 * 删除文件或者文件夹
 *  
 * @param {string} file 
 */
export function empty(file) {
    if (!fs.existsSync(file)) {
        return;
    }
    if (fs.lstatSync(file).isFile()) {
        fs.unlinkSync(file);
        return;
    }
    for (const f of fs.readdirSync(file)) {
        const abs = path.resolve(file, f);
        empty(abs);
    }
    fs.rmdirSync(file);
}

/**
 * 
 * @param {string} src 源文件或者文件夹
 * @param {string} dest 目标文件夹
 */
export function copy(src, dest) {
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
 * 如果这个文件不存在, 如果这个文件是.gitignore或者带有后缀, 则作为文件创建, 否则作为文件夹
 * @param {string} file 
 */
export function existfile(file) {
    if (!fs.existsSync(file)) {
        const name = path.basename(file);
        if (name === ".gitignore" || name === "_gitignore" || path.extname(file)) {
            fs.writeFileSync(file, "");
        } else {
            fs.mkdirSync(file);
        }
    }
}


//////////////////// ////////////////////

export const TYPE_CREATE = "create:";
export const TYPE_OPERATE = "operate:";

/**
 * @param {string} cmd 执行生成项目的命令, 一个type只会有一个命令
 */
export function exe_createProject(cmd) {
    return {
        type: TYPE_CREATE,
        desc: cmd,
        exe: async () => shell.exec(cmd)
    }
}

/**
 * @param {string} cmd 执行非生成项目的命令
 */
export function exe_cmd(cmd) {
    return {
        type: TYPE_OPERATE,
        desc: cmd,
        exe: async () => shell.exec(cmd)
    }
}

/**
 * 向该文件写入内容
 * @param {string} file 
 * @param {string} content 
 * @param {boolean|undefined} append
 */
export function exe_write(file, content, append) {
    return {
        type: TYPE_OPERATE,
        desc: `write to ${path.basename(file)}`,
        exe: async () => {
            existfile(file);
            if (append) {
                fs.appendFileSync(file, content);
            } else {
                fs.writeFileSync(file, content);

            }
        }
    }
}

/**
 * 删除一些文件或者文件夹
 * @param {string | string[]} p
 */
export function exe_del(p) {
    return {
        type: TYPE_OPERATE,
        desc: `del files`,
        exe: async () => {
            if (typeof p === 'string') {
                empty(p);
            } else {
                for (const i of p) {
                    empty(i);
                }
            }
        }
    }
}

/**
 * 替换某些内容
 * @param {string} file 
 * @param {{reg:RegExp|string, str:string}|{reg:RegExp|string, str:string}[]} reg 
 */
export function exe_replace(file, reg) {
    return {
        type: TYPE_OPERATE,
        desc: `update ${path.basename(file)}`,
        exe: async () => {
            if (!fs.existsSync(file)) return;
            let newcontent = fs.readFileSync(file, { encoding: 'utf-8' });
            if (Array.isArray(reg)) {
                for (const r of reg) {
                    newcontent = newcontent.replace(r.reg, r.str);
                }
            } else {
                newcontent = newcontent.replace(reg.reg, reg.str);
            }

            fs.writeFileSync(file, newcontent);
        }
    }
}

/**
 * 将src的所有内容复制到desc中
 * @param {string} src 
 * @param {string} desc 
 */
export function exe_copy(src, desc) {
    return {
        type: TYPE_OPERATE,
        desc: `update ${path.basename(desc)}`,
        exe: async () => copy(src, desc)
    }
}

/**
 * 将文件内容转为json并将返回的json再写入到文件中
 * 
 * @param {string} file 
 * @param {(any)=>any} json 
 * @returns 
 */
export function exe_json(file, json) {
    return {
        type: TYPE_OPERATE,
        desc: `update ${path.basename(file)}`,
        exe: async () => {
            if (!fs.existsSync(file)) return;
            const newjson = json(JSON.parse(fs.readFileSync(file).toString()));
            fs.writeFileSync(file, JSON.stringify(newjson, null, 2));
        }
    }
}