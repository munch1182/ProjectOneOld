#! /usr/bin/env node

import path from "path";
import fs from "fs";
import minimist from 'minimist';
import prompts from 'prompts';
import { fileURLToPath } from "url";
import { err, desc, warn, TYPE_CREATE, empty } from "./help.js";

const PREFIX_TYPE = "type-";
const PREFIX_LIB = "lib-";

// [projectType] [projectName] --pm npm
const argv = minimist(process.argv.slice(2), { string: ['_'] });

//源码文件夹根目录
const srcRoot = path.dirname(path.resolve(fileURLToPath(import.meta.url)));
const arg = {
    // 包管理器, 必须要与npm的方法一致
    pm: 'npm',
    // 要创建项目的文件夹(包括项目本身的文件夹)
    targetDir: '',
    // 项目名称
    projectName: '',
    // 项目类型
    type: '',
    // 项目根文件夹 type-
    typeRoot: '',
    // 类型
    lib: '',
    // 类型根文件夹 lib-
    libRoot: ''
};

(async () => {
    let pType = argv._[0];
    let pName = argv._[1];
    if (argv.pm) {
        arg.pm = argv.pm;
    }
    const TYPES = _direnum(srcRoot, PREFIX_TYPE);
    if (!pType || !pName || pType == '/' || pName == '/') {
        const [t, n] = await _askTypeAndProject(TYPES, pType, pName);
        // select会被解析成number
        t ? pType = t : null;
        n ? pName = n : null;
    }

    if (!pType || !pName) {
        return
    }
    arg.projectName = pName;
    arg.type = pType;
    arg.targetDir = path.resolve(process.cwd(), arg.projectName);

    const sure = await _sureProjectCanCreate(arg.targetDir);
    if (!sure) return;

    // 需要执行的所有步骤
    const funs = new Map();
    // type-根文件夹
    arg.typeRoot = path.join(srcRoot, `${PREFIX_TYPE}${pType}`);

    let libindex = 1;

    /**
     * 读取dir下的index.js文件, 并读取其中的default(匿名)方法, 将其返回依序和类型加入到funs中
     * @param {string} dir 
     * @param {number} index 
     */
    async function _indexDefault(dir, index) {
        const indexjs = await import(`file://${path.join(dir, "index.js")}`);
        // default 也即匿名函数
        if (typeof indexjs.default === "function") {
            const ff = await indexjs.default(arg);
            let exeindex = 1;
            for (const f of ff) {
                // TYPE_CREATE一个项目唯一
                if (f.type === TYPE_CREATE) {
                    funs.set(f.type, f);
                } else {
                    funs.set(`${index}.${exeindex++}`, f)
                }
            }
        }
    }

    await _indexDefault(arg.typeRoot, libindex++);
    const LIBS = _direnum(arg.typeRoot, PREFIX_LIB);
    if (LIBS) {
        const libs = await _askLib(LIBS, pType, pName);
        for (const lib of libs) {
            arg.lib = lib;
            // lib-根文件夹
            arg.libRoot = path.join(arg.typeRoot, `${PREFIX_LIB}${lib}`);
            await _indexDefault(arg.libRoot, libindex++);
        }
    }

    funs.forEach(async (v, k) => {
        console.log(desc(`${k} ${v.desc}`));
        await v.exe();
    })
})();



/**
 * 
 * @param {string[]} LIBS 
 * @returns {Promise<string[]>}
 */
async function _askLib(LIBS) {
    const choices = LIBS.map(f => { return { title: f } });
    const result = await prompts([
        {
            type: 'multiselect',
            name: 'libs',
            message: desc('chose lib:'),
            choices: choices,
        }
    ], {
        onCancel: () => console.log(err("cancel."))
    });
    return result.libs.map(f => { return LIBS[f] });
}

/**
 * 确保文件可创建
 * @param {string} project 
 * @returns {Promise<boolean|undefined>}
 */
async function _sureProjectCanCreate(project) {
    if (!fs.existsSync(project)) {
        return true;
    }
    const descStr = `del ${path.basename(project)}, sure?`
    const result = await prompts(
        {
            type: 'confirm',
            name: 'del',
            message: warn(descStr)
        }
        , {
            onCancel: () => console.log(err("cancel."))
        });
    if (result.del) {
        empty(project);
    }
    return result.del;
}

/**
 * 
 * @param {string[]} TYPES 
 * @param {string|undefined} projectType 
 * @param {string|undefined} projectName 
 * @returns {Promise<[string, string]>}
 */
async function _askTypeAndProject(TYPES, projectType, projectName) {
    const choices = TYPES.map(f => { return { title: f } });
    const result = await prompts([
        {
            type: projectType ? null : 'select',
            name: 't',
            message: desc('chose project type:'),
            choices: choices,
        }, {
            type: projectName ? null : 'text',
            name: 'n',
            message: desc('input project name:'),
            initial: 'ProjectNew'
        }
    ], {
        onCancel: () => console.log(err("cancel."))
    });

    return [TYPES[result.t], result.n]
}

function _direnum(dir, prefix) {
    return fs.readdirSync(dir).filter(f => f.startsWith(prefix)).map(f => f.replace(prefix, ''));
}