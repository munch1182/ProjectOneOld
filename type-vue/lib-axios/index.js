import { cmd } from "../../help.js";

export default function (currDir, targetDir, name) {
    return [
        cmd(`cd ${targetDir} && npm install axios`),
    ]
}