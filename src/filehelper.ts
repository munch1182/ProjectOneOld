import * as fs from "fs";
import { type } from "os";
import * as path from "path";

export class FileHelper {

    static read(p: string): FileHelper {
        return new FileHelper(p);
    }

    // 文件之前所有文件夹, 带盘符, 如果是由文件构建的, 则是文件本身
    readonly dir: string;
    // 不带文件夹路径不带后缀的文件名
    readonly filename?: string;
    // 该路径是否是文件
    readonly isDir: boolean = false;
    // 文件后缀, 不带.
    readonly ext?: string

    private constructor(p: string) {
        this.dir = path.dirname(p);
        this.ext = path.extname(p)?.replace(".", "");
        if (this.ext) {
            this.filename = path.basename(p, path.extname(p));
        }
        this.isDir = this.ext == undefined
    }

    convertCMD(cmd: string): string {
        let str = cmd;
        str = str.replace(/\$dir/g, this.dir)
        if (this.filename) {
            str = str.replace(/\$filenameWithoutExt/g, this.filename);
            if (this.ext) {
                str = str.replace(/\$filename/g, `${this.filename}.${this.ext}`);
            }
        }
        return str;
    }
}

export interface Project {
    // 项目路径
    readonly dir: string;
    // 指项目路径的最后一个文件夹名称, 可能不是实际上的项目名称
    readonly name: string;
    // 类型名
    readonly type: string;

    /**
     * @param cmd 将命令中的变量转为实际的地址 
     * @param file 可能牵涉到的文件
     */
    convertCMD(cmd: string, file?: string): string;
}

export class JsProject implements Project {
    /**
     * 传入一个文件夹和该文件夹下的文件, 判断该文件是否是在js项目中(js/ts): 只要有package.json文件存在的文件夹视为js项目
     * 如果[file]未传值, 则从[dir]往文件夹内寻找一个js项目, 否则, 先寻找[file]所在文件夹, 再向外寻找直到[dir]为止, 不包括与[file]同级的文件夹
     * [dir]下如果有多个js项目, 如果传入[file], 则返回离[file]最近的项目, 否则返回第一个js项目
     * 
     * @param dir 文件夹, 只会判断该文件夹之内的内容, 且该路径必须存在
     * @param file 要判断的文件, 该文件必须在[dir]内 
     * @returns  如果是js项目, 返回JsProject对象, 否则, 返回undefined
     */
    static isJs(dir: string, file?: string): JsProject | undefined {
        if (!fs.existsSync(dir)) return undefined
        const packagejson = _findfile("package.json", dir, file);
        if (!packagejson) return undefined;
        return new JsProject(packagejson)
    }

    readonly type: string = "js";
    // 项目路径
    readonly dir: string;
    readonly name: string;
    // pkg路径
    readonly packagejson: string;
    // 是否是ts项目
    readonly isTs: boolean;
    // scripts中的命令的名称的集合
    readonly command?: string[];

    /**
     * @param packagejson 项目的package.json文件地址, 不会对其是否存在进行校验, 也不会对其内容进行校验
     */
    private constructor(packagejson: string) {
        this.packagejson = packagejson;
        this.dir = path.dirname(packagejson);
        this.name = path.basename(this.dir);
        const pack = JSON.parse(fs.readFileSync(packagejson).toString());
        // 只判断main文件是否是ts文件
        this.isTs = (pack.main as string).endsWith(".ts");
        // 只获取scripts第一个命令
        this.command = Object.keys(pack.scripts);
    }

    toString() {
        return `{dir: ${this.dir}, isTs: ${this.isTs}, command: ${this.command}}`
    }

    convertCMD(cmd: string, file?: string): string {
        let str = cmd;
        str = str.replace(/\$projectdir/g, this.dir)
        // packagename只指向项目文件的最后一个文件夹, 没有读取实际的项目名称
        str = str.replace(/\$packagename/g, this.name);
        if (file) {
            str = FileHelper.read(file).convertCMD(str);
        }
        try {
            const reg = /\$js_scripts\[[\w]\]/g;
            const scripts = str.match(reg)?.[0];
            if (scripts) {
                const indexStr = scripts.substring(scripts.indexOf('[') + 1, scripts.indexOf(']'));
                const index = Number.parseInt(indexStr);
                const indexCMD = this.command?.[index];
                if (indexCMD) {
                    str = str.replace(reg, indexCMD);
                }
            }
        } catch (_) {
        }
        return str;
    }
}

export class RustProject implements Project {

    /**
     * 传入一个文件夹和该文件夹下的文件, 判断该文件是否是在rust项目中: 只要有Cargo.toml文件存在的文件夹视为js项目
     * 如果[file]未传值, 则从[dir]往文件夹内寻找一个rust项目, 否则, 先寻找[file]所在文件夹, 再向外寻找直到[dir]为止, 不包括与[file]同级的文件夹
     * [dir]下如果有多个rust项目, 如果传入[file], 则返回离[file]最近的项目, 否则返回第一个rust项目
     * 
     * @param dir 文件夹, 只会判断该文件夹之内的内容, 且该路径必须存在
     * @param file 要判断的文件, 该文件必须在[dir]内 
     * @returns  如果是js项目, 返回JsProject对象, 否则, 返回undefined
     */
    static isRust(dir: string, file?: string): RustProject | undefined {
        if (!fs.existsSync(dir)) return undefined
        const cargotoml = _findfile("Cargo.toml", dir, file);
        if (!cargotoml) return undefined;
        return new RustProject(cargotoml)
    }

    readonly type: string = "rs";
    // 项目文件夹路径
    readonly dir: string;
    readonly name: string;
    // Cargo.toml文件路径
    readonly cargotoml: string;
    // 项目src文件夹路径
    readonly src: string;

    private constructor(cargotoml: string) {
        this.cargotoml = cargotoml
        this.dir = path.dirname(cargotoml)
        this.name = path.basename(this.dir);
        this.src = path.join(this.dir, "src");
    }

    convertCMD(cmd: string, file?: string): string {
        let str = cmd;
        str = str.replace(/\$projectdir/g, this.dir)
        // packagename只指向项目文件的最后一个文件夹, 没有读取实际的项目名称
        str = str.replace(/\$packagename/g, this.name);
        if (file) {
            const helper = FileHelper.read(file);
            str = helper.convertCMD(str);
            str = str.replace(/\$dir/g, helper.dir);
            const paths = path.normalize(helper.dir).split('\\');
            const index = paths.findIndex(f => f == "src" || f == "tests");

            if (index != -1) {
                const pkgs = paths.slice(index + 1);
                if (helper.filename && helper.filename != "lib" && helper.filename != "mod" && helper.filename != "main") {
                    pkgs.push(helper.filename);
                }
                // 只对test的mod名为tests的有效
                str = str.replace(/\$rs_currpackage_tests/g, pkgs.length > 0 ? pkgs.join("::") : "tests");
                str = str.replace(/\$rs_currpackage/g, pkgs.join("::"));
            }
        }
        return str;
    }

    /**
     * 
     * @param version 切换版本 stable/nightly/beta
     * @returns 
     */
    // getDefault(version: RustVersion): string {
    //     return `rustup default ${version}`;
    // }

    toString() {
        return `{dir: ${this.dir}}`;
    }
}


// export enum RustVersion {
//     stable = "stable",
//     nightly = "nightly",
//     beta = "beta"
// }

/**
 * @todo 跳过 node_modules和target文件夹
 * 
 * 在文件夹内寻找一个特定的文件
 * 优先返回同级的文件而不是文件夹内的文件
 * 
 * @param dir 文件夹, 只会在该文件夹内寻找
 * @param file 当前文件, 如果该文件有值, 则会寻找该文件最近的目标文件, 否则从[dir]从上往下寻找
 * @param name 目标文件名
 * @returns  如果找到, 返回该文件路径, 否则, 返回undefined
 */
function _findfile(name: string, dir: string, file?: string): string | undefined {
    // 如果有传入文件且该文件存在
    if (file && fs.existsSync(file)) {
        const lstat = fs.lstatSync(file);
        if (lstat.isFile()) {
            const currdir = path.dirname(file);
            const find = _findfile_imp(name, currdir, undefined);
            if (find) return find;
            // 该文件夹下及其子文件夹下无目标文件, 需要退到上一级文件夹
            const dirs = path.normalize(currdir).split('\\');
            if (dirs) {
                let exclude = path.basename(currdir);
                const last = path.normalize(dir).split('\\');
                dirs.pop();
                while (dirs.length > last.length) {
                    const find = _findfile_imp(name, dirs.join(path.sep), exclude);
                    if (find) return find;
                    dirs.pop();
                    exclude = dirs[dirs.length - 1];
                }
            }
            return undefined;
        }
    }
    // 如果未传入文件或者传入文件无效
    return _findfile_imp(name, dir);
}

/**
 * 
 * @param name 目标文件名
 * @param dir 文件夹
 * @param exclude 已经查找过的文件夹, 向上查找时, 只有一个文件夹需要排除, 向下查找则无需排除
 */
function _findfile_imp(name: string, dir: string, exclude?: string): string | undefined {
    // 将文件排在前面
    const tdir = fs.readdirSync(dir).sort((f, _) => { return fs.lstatSync(path.join(dir, f)).isFile() ? -1 : 1 });
    for (const f of tdir) {
        if (exclude && f == exclude) continue;
        const p = path.join(dir, f);
        const sta = fs.lstatSync(p);
        if (sta.isFile()) {
            if (path.basename(f) === name) return p;
        } else {
            return _findfile(name, p, undefined);
        }
    }
    return undefined
}
