import { cmd } from "../../help.js";

export default function (_, targetDir, arg) {
    return [
        cmd(`cd ${targetDir} && ${arg.pm} install axios`),
    ]
}