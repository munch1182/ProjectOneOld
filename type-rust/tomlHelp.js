import fs from "fs";
import { inline } from "@ltd/j-toml";
import { TYPE_OPERATE } from "../help.js";

/**
 * 传入依赖, 则自动写入需要的部分
 * 
 * @param {string} file Cargo.toml文件 
 * @param {{ name: string, version: string|undefined, features: string[]|undefined, git: string|undefined}[]} deps 需要的依赖 
 * 
 */
export function tomlDep(file, deps, type) {
    return exe_toml(file, async (toml) => {
        const dependencies = toml.dependencies;
        for (const dep of deps) {
            const name = dep.name;
            if (dep.git) {
                dependencies[name] = inline({ git: dep.git });
            } else {
                dependencies[name] = dep.version; // 用于创建
                if (dep.features) {
                    dependencies[name] = _tomlDepChack(dependencies[name], dep);
                }
            }
        }
        return toml;
    }, type);
}

/**
 * 
 * @param {any} any 需要更改依赖的元素, 即dependencies的子元素
 * @param {{name: string, version: string, features: string[], git:string|undefined}} dep 需要添加的feature, dep.features肯定有值
 */
export function _tomlDepChack(any, dep) {
    // 如果还未添加features
    if (!any || typeof any == 'string' || !any.features) {
        const v = any ? any : dep.version;
        any = inline({ version: v, features: inline(dep.features) });
    } else {
        let f = any.features;
        // 如果已有features
        for (const feature of dep.features) {
            /// 如果有features但是没有添加
            if (!f.includes(feature)) {
                f.push(feature);
            }
        }
    }
    return any;
}

/**
 * 
 * @param {string} file Cargo.toml文件
 * @param {(any) => any} callback 回调读取的toml对象, 返回toml对象并写入 
 * @param {string|undefined} type
 */
export function exe_toml(file, callback, type) {
    return {
        type: TYPE_OPERATE,
        desc: type ? `update toml fot ${type}` : "update toml",
        exe: async () => {
            const TOML = await import("@ltd/j-toml");
            const result = TOML.parse(fs.readFileSync(file));
            const any = await callback(result);
            const str = TOML.stringify(any, { newline: '\n', newlineAround: 'section' });
            fs.writeFileSync(file, str);
        }
    };
}