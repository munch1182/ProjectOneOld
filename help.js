import fs from 'fs';
import { yellow, cyan, red } from 'kolorist';
import path from 'path';
import prompts from 'prompts';
import readline from 'readline';
import shell from "shelljs";

export const desc = (s) => cyan(s);
export const err = (s) => red(s);
export const warn = (s) => yellow(s);

/**
 * 删除文件或者文件夹
 *  
 * @param {string} file 
 */
export function empty(file) {
    if (!fs.existsSync(file)) {
        return;
    }
    if (!fs.lstatSync(file).isDirectory()) {
        fs.unlinkSync(file);
        return;
    }
    for (const f of fs.readdirSync(file)) {
        const abs = path.resolve(file, f);
        if (fs.statSync(abs).isDirectory()) {
            empty(abs);
            fs.rmdirSync(abs);
        } else {
            fs.unlinkSync(abs);
        }
    }
}

/**
 * 
 * @param {string} src 源文件或者文件夹
 * @param {string} dest 目标文件夹
 */
export function copy(src, dest) {
    const stat = fs.statSync(src);
    if (stat.isDirectory()) {
        copyDir(src, dest);
    } else {
        fs.copyFileSync(src, dest);
    }
}

/**
 * 
 * @param {string} src 源文件或者文件夹
 * @param {string} dest 目标文件夹
 */
function copyDir(src, dest) {
    fs.mkdirSync(dest, { recursive: true });
    for (const f of fs.readdirSync(src)) {
        const s = path.resolve(src, f);
        const d = path.resolve(dest, f);
        copy(s, d);
    }
}

export function newfile(file) {
    if (!fs.existsSync(file)) {
        if (fs.statSync(src).isDirectory()) {
            fs.mkdirSync(file);
        } else {
            fs.writeFileSync(file, "");
        }
    }
}


//////////////////// ////////////////////

/**
 * @param {string} cmd 执行生成项目的命令, 一个type只会有一个命令
 */
export function createProject(cmd) {
    return {
        type: "createProject",
        exe: async () => shell.exec(cmd)
    }
}

/**
 * @param {async Funtion} asyncfun 要执行的步骤, 是一个异步方法 
 */
export function operate(asyncfun) {
    return {
        type: "operate",
        exe: async () => await asyncfun
    }
}