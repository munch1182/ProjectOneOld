
import * as helper from '../../help.js';
import path from 'path';


const content = `content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ]`;
const imp = `
@tailwind base;
@tailwind components;
@tailwind utilities;
`;

/**
 * @param {{pm: string, targetDir: string, projectName: string,type: string, typeRoot: string, lib: string, libRoot: string }} arg 
 * @returns {{ type:string, exe: () => Promise<ShellString> }[]}
 */
export default function (arg) {
    return [
        helper.exe_cmd(`cd ${arg.targetDir} && ${arg.pm} i -D tailwindcss postcss autoprefixer && npx tailwindcss init -p`),
        helper.exe_replace(path.join(arg.targetDir, "tailwind.config.cjs"), { reg: "content: []", str: content }),
        helper.exe_write(path.join(arg.targetDir, "src", "style.css"), imp, true)
    ]
}
