import { tomlDep } from "../tomlHelp.js";
import path from "path";

export default async function (_, targetDir, _) {
    return [
        tomlDep(path.join(targetDir, 'Cargo.toml'),
            [
                { name: 'serde', version: '1', features: ['derive'] },
                { name: 'serde_json', version: '1', }
            ])
    ]
}