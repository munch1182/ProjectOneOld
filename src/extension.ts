import * as vscode from 'vscode';
import { FileHelper } from './filehelper';

export function activate(context: vscode.ExtensionContext) {
	const extension = new ExtensionRunner();
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
	private _file_terminal?: vscode.Terminal

	constructor() {
		this._output = vscode.window.createOutputChannel("project-runner")
		this._config = vscode.workspace.getConfiguration("project-runner");
		this._file_terminal = undefined;
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

	runProject(_uri: vscode.Uri) {
	}
}