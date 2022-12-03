import fs from "fs";
import path from "path";
import { inline } from "@ltd/j-toml";



/**
 * 传入依赖, 则自动检查和写入
 * 
 * deps如:
 * { name: 'tokio', version: '1', features: ['full'] },
 * { name: 'serde', version: '1', features: ['derive'] },
 * { name: 'serde_json', version: '1', }
 * 
 * @param {string} file 
 * @param {[{name:string,version:string,features:string[]?}]} deps 需要的依赖
 */
export function tomlDep(file, deps) {
    return fileToml(file, async (toml) => {
        const dependencies = toml.dependencies;
        for (const dep of deps) {
            const name = dep.name;
            dependencies[name] = dep.version; // 用于创建
            if (dep.features) {
                dependencies[name] = tomlDepCheck(dependencies[name], dep)
            }
        }
        return toml
    })
}

/**
 * 
 * @param {any} any 需要更改依赖的元素, 即dependencies的子元素
 * @param {{name: string, version: string, features: string[]}} dep 需要添加的feature, dep.features肯定有值
 * @return 更改后的依赖值
 */
export function tomlDepCheck(any, dep) {
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
    return any
}
/**
 * 
 * @param {string} file 要更新的文件
 * @param {funtion: (any) -> any} callback 将文件转为toml对象传入, 更改后再返回
 * @returns 
 */
export function fileToml(file, callback) {
    return {
        "desc": `update toml ${path.basename(file)}`,
        "exec": async () => {
            const ext = path.extname(file).toLowerCase();
            switch (ext) {
                case '.toml':
                    try {
                        const TOML = await import('@ltd/j-toml');
                        const result = TOML.parse(fs.readFileSync(file));
                        const any = await callback(result);
                        fs.writeFileSync(file, TOML.stringify(any, { newline: '\n', newlineAround: 'section' }))
                    } catch (_) {
                    }
                    break;
                default:
                    return false;
            }

            return true;
        }
    }
}