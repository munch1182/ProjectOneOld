const PREFIX_TYPE = "type-";

const fs = require('fs');
const path = require('path');
const prompts = require('prompts');
const { red, cyan } = require('kolorist');
// 参数 {type} {projectName}
const argv = require('minimist')(process.argv.slice(2));

// 创建项目所在文件夹
const root = process.cwd();

(async () => {

    const TYPES = fs.readdirSync(root).filter(f => f.startsWith(PREFIX_TYPE)).map(f => f.replace(PREFIX_TYPE, ""));

    let argType = argv._[0];
    let argProjectName = argv._[1];

    const { t, n } = await prompts([
        {
            type: argType ? null : 'select',
            name: 't',
            message: cyan('chose type'),
            choices: TYPES
        }, {
            type: argProjectName ? null : 'text',
            name: 'n',
            message: cyan('Project Name:'),
            initial: 'P1'
        }
    ], {
        onCancel: () => {
            console.log(red("cancel"));
        }
    });

    t != undefined ? argType = TYPES[t] : null;
    n ? argProjectName = n : null;

    if (!argType || !argProjectName) {
        return
    }

    const nameRoot = `${PREFIX_TYPE}${argType}`;
    const pathRoot = path.join(root, nameRoot);
    // 交由type去实现
    const imp = require(`${pathRoot}\\index.js`);
    // 第一个参数是创建文件的目标文件夹
    // 第二个参数type-的根文件夹
    // 第三个参数是新建文件夹名称
    await imp.create(root, pathRoot, argProjectName);
})();