#! /usr/bin/env node

const PREFIX_TYPE = "type-";
const PREFIX_LIB = "lib-";

import path from "path";
import fs from "fs";
import minimist from 'minimist';
import prompts from 'prompts';
import shell from "shelljs";
import { fileURLToPath } from "url";
import { fileNew, desc, err } from "./help.js";


// [projectType] [projectName] --open --pn npm
const argv = minimist(process.argv.slice(2));

/**
 * 源码文件夹根目录
 */
const srcDir = path.resolve(fileURLToPath(import.meta.url), '../');
const arg = { "pm": 'npm', 'projectName': '' };

(async () => {
    let projectType = argv._[0];
    let projectName = argv._[1];
    let open = argv.open;
    if (argv.pm) {
        arg.pm = argv.pm;
    }

    if (!projectType || !projectName || projectType == '/' || projectName == '/') {
        const TYPES = fs.readdirSync(srcDir).filter(f => f.startsWith(PREFIX_TYPE)).map(f => f.replace(PREFIX_TYPE, ''));
        const { t, n } = await prompts([
            {
                type: projectType ? null : 'select',
                name: 't',
                message: desc('chose project type:'),
                choices: TYPES
            }, {
                type: projectName ? null : 'text',
                name: 'n',
                message: desc('input project name:'),
                initial: 'ProjectNew'
            }
        ], {
            onCancel: () => console.log(err("cancel."))
        });
        // select会被解析成number
        TYPES[t] ? projectType = TYPES[t] : null;
        n ? projectName = n : null;
    }

    if (!projectType || !projectName) {
        return
    }

    arg.projectName = projectName;

    // 要创建的项目文件夹
    const projectDir = path.resolve(process.cwd(), arg.projectName);
    const tasks = [];


    // type-根文件夹
    const typeRootDir = path.resolve(srcDir, `${PREFIX_TYPE}${projectType}`);

    if (!fs.existsSync(typeRootDir)) {
        console.log(err('unsupport type.'));
        return;
    }

    // 执行type/index.js
    const typeIndexJs = path.resolve(typeRootDir, 'index.js');
    
    if (fs.existsSync(typeIndexJs)) {
        const typeIndex = await import(`file:///${typeIndexJs}`);
        const typeTask = await typeIndex.default(typeRootDir, projectDir, arg);

        if (typeTask && typeTask.length) {
            tasks.push(typeTask);
        }
    }
    // 执行lib/index.js
    const LIBS = fs.readdirSync(typeRootDir).filter(f => f.startsWith(PREFIX_LIB)).map(f => f.replace(PREFIX_LIB, ''));
    if (LIBS.length) {
        // 选择lib
        const response = await prompts([
            {
                type: 'multiselect',
                name: 'libs',
                message: desc('chose lib type:'),
                choices: LIBS
            }
        ], {
            onCancel: () => { console.log(err("cancel.")); return false; }
        });

        const libs = response.libs;
        if (!libs) { // cancel
            return
        }

        for (const index of libs) {
            const lib = LIBS[index];
            const libRootDir = path.resolve(typeRootDir, `${PREFIX_LIB}${lib}`);
            const libIndexJs = path.resolve(libRootDir, 'index.js');

            if (fs.existsSync(libIndexJs)) {
                const libIndex = await import(`file:///${libIndexJs}`);
                const libTask = await libIndex.default(libRootDir, projectDir, arg);
                if (libTask && libTask.length) {
                    tasks.push(libTask);
                }
            }
        }
    }

    // 实际的执行方法都在type中, 如果没有值即不创建
    if (!tasks.length) {
        return
    }

    // 确保目标文件夹可用
    if (fs.existsSync(projectDir)) {
        tasks.unshift([fileNew(projectDir)]);
    }

    let index = 0;
    for await (const f of tasks) {
        index++;
        let j = 0;
        if (f) {
            for await (const ff of f) {
                console.log(desc(`${index}.${j++}: ${ff.desc}`));
                const isOk = await ff.exec();
                if (!isOk) {
                    return;
                }
            }
        }
        console.log(''); // 换行
    }

    console.log(desc("success"));

    if (open) {
        shell.exec(`code ${projectDir}`);
    }
})();
