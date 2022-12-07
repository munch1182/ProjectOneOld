
import * as helper from '../../help.js';
import path from "path";
import shell from "shelljs";
import prompts from "prompts";

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default async function (arg) {
    const tsnode = await _askTsNode(arg);
    const result = [
        helper.exe_json(path.join(arg.targetDir, "package.json"), j => {
            j.main = "index.ts";
            const main = j.main;
            j.scripts.dev = `ts-node ${main}`;
            const test = j.scripts.test;
            delete j.scripts.test;
            j.scripts.test = test;

            return j
        }),
        helper.exe_copy(path.join(arg.libRoot, "template"), arg.targetDir)
    ]
    if (tsnode) {
        result.push(tsnode);
    }
    return result;
}

async function _askTsNode(arg) {
    if (shell.which('ts-node').code != 0) {
        const result = await prompts({
            type: 'confirm',
            name: 'tsnode',
            message: helper.warn(`${arg.pm} i -g ts-node, sure?`),
            initial: user
        }, { onCancel: () => console.log(helper.err("cancel.")) })

        if (result.tsnode) {
            return helper.exe_cmd(`${arg.pm} i -g ts-node`);
        }
    }

    return undefined;
}
