import * as vscode from 'vscode';
import { FileHelper, JsProject, RustProject, Project } from './filehelper';

export function activate(context: vscode.ExtensionContext) {
	const extension = new ExtensionRunner();
	vscode.window.onDidCloseTerminal(e => extension.onTerminalClose(e))
	let runfile = vscode.commands.registerCommand('munch1182-runner.runfile', (uri: vscode.Uri) => extension.runFile(uri));
	let runproject = vscode.commands.registerCommand('munch1182-runner.runproject', (uri: vscode.Uri) => extension.runProject(uri));
	context.subscriptions.push(runfile);
	context.subscriptions.push(runproject);
}

export function deactivate() { }


class ExtensionRunner {

	private _output: vscode.OutputChannel;
	private _config: vscode.WorkspaceConfiguration;
	// 文件执行共享一个terminal
	private _file_terminal?: vscode.Terminal;
	private _project_terminal: Map<string, vscode.Terminal | undefined>
	constructor() {
		this._output = vscode.window.createOutputChannel("project-runner")
		this._config = vscode.workspace.getConfiguration("project-runner");
		this._file_terminal = undefined;
		this._project_terminal = new Map();
	}

	onTerminalClose(t: vscode.Terminal) {
		if (this._file_terminal && this._file_terminal === t) {
			this._file_terminal = undefined;
		} else {
			for (const [key, value] of this._project_terminal.entries()) {
				if (value === t) {
					this._project_terminal.set(key, undefined);
					break;
				}
			}
		}
	}

	runFile(uri: vscode.Uri) {
		const helper = FileHelper.read(uri.fsPath);
		const ext = helper.ext;
		if (!ext) {
			vscode.window.showInformationMessage("unsupport file");
			return;
		}
		const config = this._config.get<any>("filecmd");
		if (!config) {
			vscode.window.showInformationMessage("no cmd");
			return;
		}
		const filecmd = config[`.${ext}`] as string;
		if (!filecmd) {
			vscode.window.showInformationMessage("no cmd");
			return;
		}
		const cmd = helper.convertCMD(filecmd);
		if (!this._file_terminal) {
			this._file_terminal = vscode.window.createTerminal("project-runner");
		}

		this._output.appendLine(`execute: ${cmd}`);

		this._file_terminal?.show(true);
		this._file_terminal?.sendText(cmd, true);
	}

	runProject(uri: vscode.Uri) {
		const folder = vscode.workspace.workspaceFolders?.find(f => uri.fsPath.includes(f.uri.fsPath));
		if (!folder) {
			vscode.window.showInformationMessage("not project");
			return;
		}
		const dir = folder.uri.fsPath;
		const f = uri.fsPath;

		let project = this._findProject(dir, f);
		if (!project) {
			vscode.window.showInformationMessage("unsupport project");
			return;
		}
		const cmd = project.getRunCmd();
		if (!cmd) {
			vscode.window.showInformationMessage("project donnot have cmd");
			return;
		}
		this._output.appendLine(`project cmd: ${cmd}`);
		let terminal = this._project_terminal.get(project.dir);
		if (!terminal) {
			this._project_terminal.set(project.dir, vscode.window.createTerminal(`project-runner-${project.type}`));
			terminal = this._project_terminal.get(project.dir);
		}

		terminal?.show(true);
		terminal?.sendText(cmd);
	}

	_findProject(dir: string, file: string | undefined): Project | undefined {
		const js = JsProject.isJs(dir, file);
		if (js) {
			this._output.appendLine(`JS project: ${js}`);
			return js;
		}
		const rs = RustProject.isRust(dir, file);
		if (rs) {
			this._output.appendLine(`RS project: ${rs}`);
			return rs;
		}
		return undefined;
	}
}