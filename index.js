#! /usr/bin/env node

const PREFIX_TYPE = "type-";
const PREFIX_LIB = "lib-";

import path from "path";
import fs from "fs";
import { cyan, red } from 'kolorist';
import minimist from 'minimist';
import prompts from 'prompts';
import shell from "shelljs";
import { fileURLToPath } from "url";
import { fileNew } from "./help.js";

const desc = (s) => cyan(s);
const err = (s) => red(s);

// [projectType] [projectName] --open
const argv = minimist(process.argv.slice(2));

/**
 * 源码文件夹根目录
 */
const srcDir = path.resolve(fileURLToPath(import.meta.url), '../');

(async () => {
    let projectType = argv._[0];
    let projectName = argv._[1];
    let open = argv.open;

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
        projectType = TYPES[t]; projectName = n;
    }

    if (!projectType || !projectName) {
        return
    }
    // 要创建的项目文件夹
    const projectDir = path.resolve(process.cwd(), projectName);
    const tasks = [];

    // 确保目标文件夹可用
    if (fs.existsSync(projectDir)) {
        tasks.push([fileNew(projectDir)]);
    }

    // type-根文件夹
    const typeRootDir = path.resolve(srcDir, `${PREFIX_TYPE}${projectType}`);

    // 执行type/index.js
    const typeIndexJs = path.resolve(typeRootDir, 'index.js');
    if (fs.existsSync(typeIndexJs)) {
        console.log(typeIndexJs);
        const typeIndex = await import(`file:///${typeIndexJs}`);
        const typeTask = typeIndex.default(typeRootDir, projectDir, projectName);
        if (typeTask) {
            tasks.push(typeTask);
        }
    }
    // 执行lib/index.js
    const LIBS = fs.readdirSync(typeRootDir).filter(f => f.startsWith(PREFIX_LIB)).map(f => f.replace(PREFIX_LIB, ''));
    // 选择lib
    const { libs } = await prompts([
        {
            type: 'multiselect',
            name: 'libs',
            message: desc('chose lib type:'),
            choices: LIBS
        }
    ], {
        onCancel: () => console.log(err("cancel."))
    });

    if (libs) {
        for (const index of libs) {
            const lib = LIBS[index];
            const libRootDir = path.resolve(typeRootDir, `${PREFIX_LIB}${lib}`);
            const libIndexJs = path.resolve(libRootDir, 'index.js');

            if (fs.existsSync(libIndexJs)) {
                const libIndex = await import(`file:///${libIndexJs}`);
                const libTask = libIndex.default(libRootDir, projectDir, projectName);
                if (libTask) {
                    tasks.push(libTask);
                }
            }
        }
    }

    let index = 0;
    for (const f of tasks) {
        index++;
        let j = 0;
        for (const ff of f) {
            console.log(desc(`${index}.${j++}: ${ff.desc}`));
            const isOk = await ff.exec();
            if (!isOk) {
                return;
            }
        }
        console.log('');
    }

    console.log("success");

    if (open) {
        shell.exec(`code ${projectDir}`);
    }
})();
