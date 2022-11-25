const LIB_PREFIX = "lib-";

const fs = require('fs');
const path = require('path');
const shell = require('shelljs');
const prompts = require('prompts');
const { red, cyan, yellow } = require('kolorist');
const { emptyDir } = require('../help');

async function create(target, root, name) {

    const libs = fs.readdirSync(root).filter(f => f.startsWith(LIB_PREFIX)).map(f => f.replace(LIB_PREFIX, "").replace(".js", ""));

    const { lib } = await prompts([
        {
            type: 'multiselect',
            name: 'lib',
            message: cyan("select lib"),
            choices: libs
        }
    ], {
        onCancel: () => console.log(red("Cancel"))
    })

    const isOk = await createViteVue(target, name);
    console.log(isOk);
    if (!isOk) {
        return
    }

    let index = 2;
    for (const l of lib.map(i => libs[i])) {
        console.log(cyan(`${index++}. create ${l}.`));

        let jspath = path.join(root, `${LIB_PREFIX}${l}`)
        let js = require(`${jspath}/index.js`);

        // todo
    }
}

async function createViteVue(dir, name) {
    const projectDir = path.join(dir, name);
    // 如果文件已存在
    if (fs.existsSync(projectDir)) {
        if (fs.lstatSync(projectDir).isDirectory()) {
            if (fs.readdirSync(projectDir).length) {
                const { sure } = await prompts({
                    type: 'confirm',
                    name: 'sure',
                    message: yellow(`dir ${projectDir} is not empty, sure to del?`)
                });
                if (!sure) {
                    return false;
                }
                console.log(red(`del dir ${projectDir}`));
                emptyDir(projectDir);
            }
        } else { //如果已存在但却是文件(不带后缀才相同)
            const { sure } = await prompts({
                type: 'confirm',
                name: 'sure',
                message: yellow(`exists file it ${projectDir}, sure to del?`)
            });
            if (!sure) {
                return false;
            }
            console.log(red(`del file ${projectDir}`));
            emptyDir(projectDir);
        }
    }
    console.log(cyan("1. create vite-vue-ts"));
    const cmd = `npm create vite@latest ${name} -- --template vue-ts`;
    return shell.exec(cmd).code == 0
}

module.exports = { create }