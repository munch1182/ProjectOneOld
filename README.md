# tauri

官网: https://tauri.app/v1/guides/getting-started/setup

### 项目创建
- 创建前端ui文件夹和后端rust文件夹平级的项目结构, 项目使用`vite`+`vue`+`typescript`

##### 使用cargo创建和执行
1. 第一次需要使用`cargo install create-tauri-app`命令
2. 使用`cargo create-tauri-app`创建项目, 并选择`cargo`作为包管理,`vanilla`作为`UI`模板
3. 此时删除`src`文件夹, 使用`npm create vite@latest`来安装`vite`, 项目名称为`src`, 并选择`vue`+`typescript`
4. 修改`src-tauri/tauri.config.json`文件中的`build`部分
    ```
    "build": {
        "beforeDevCommand": "cd src & npm run dev",
        "beforeBuildCommand": "cd src & npm run build",
        "devPath": "http://127.0.0.1:5173",
        "distDir": "../src/dist",
        "withGlobalTauri": false
    },
    ```
    `before~Command`为`rust`代码执行前的命令, 因此`npm`在`src`目录下才能执行, 开发时需要先开启`vite`的服务, 编译时需要先编译前端代码
    `devPath`为开发环境下的资源路径前缀, 使用`vite`则应该为`vite`的开发服务器地址
    `distDir`为正式环境下的资源路径前缀, 使用`vite`则应该为`vite`的编译结果文件夹
    `withGlobalTauri`为`true`时可直接通过`window.__TAURI__`访问`tauri`, 使用通信来访问则设置`false`
5. 进入`src`文件夹, 使用`npm install`命令, 然后退回项目根目录,即可使用`cargo tauri`相关命令

此方法可使用`vite`和`vue-ts`最新版本, 并更好理解`tauri`的文件结构

##### 使用npm创建并使用cargo执行
1. 使用`npm create tauri-app`命令创建项目, 并选择`npm`作为包管理器,`vue-ts`作为`UI`模板
2. 将目录下除`src-tauri`(和`.vscode`)之外的所有文件移动到与`src-tauri`同级的新建文件夹`src`中
3. 修改`src-tauri/tauri.config.json`文件中的`build`下的`beforeDevCommand`和`beforeBuildCommand`的命令增加`cd src`, 即`cd src & npm run dev`和`cd src & npm run build`
4. 进入`src`文件夹, 使用`npm install`命令, 然后退回项目根目录,即可使用`cargo tauri`相关命令

此方法可自动创建前端`vite`和`vue-ts`相关代码

##### 创建问题
1. `npm`命令如遇网络问题可使用`cnpm`代替
`npm install cnpm -g --registry=https://registry.npmmirror.com`
2. `src/dist`文件夹需要自行创建, 否则会有错误提示
3. 创建完成后, `src`是一个单独前端项目, `src-tauri`是一个单独的`rust`项目, 两者之间通过`tauri`通信, 因此对应`npm`和`cargo`相关命令, 需要在各自文件夹下执行, 只有`cargo tauri`相关命令才能在根目录下执行
4. 需要修改的设置: `src-tauri/tauri.conf.json`中的`version`和`identifier`, `src/package.json`中的`version`和`name`, `src-tauri/Cargo.toml`中的`version`和`authors`, 这些不修改并不影响使用