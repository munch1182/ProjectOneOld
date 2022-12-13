//!  日志相关

/// 对log的实现
pub(crate) struct AppLog;

// todo 对log的过滤; 一些功能范围可以指定log的tag, 并通过命令行更改是否输出
// pub(crate) struct AppLog {
//     pub(crate) filter: Option<Vec<String>>, // 输出过滤
// }

/// 日志类型
pub enum LogType {
    /// 打开日志, 默认
    Enable,
    /// 显示Trace及以上
    Trace,
    /// 显示Info及以上
    Info,
    /// 显示Debug及以上
    Debug,
    /// 显示Warn及以上
    Warn,
    /// 显示Err及以上
    Err,
}

/// 日志设置
pub fn log_set(log: LogType) {
    match log {
        LogType::Enable | LogType::Trace => log::set_max_level(log::LevelFilter::Trace),
        LogType::Info => log::set_max_level(log::LevelFilter::Info),
        LogType::Debug => log::set_max_level(log::LevelFilter::Debug),
        LogType::Warn => log::set_max_level(log::LevelFilter::Warn),
        LogType::Err => log::set_max_level(log::LevelFilter::Error),
    }
}

impl log::Log for AppLog {
    fn enabled(&self, _metadata: &log::Metadata<'_>) -> bool {
        // if let Some(v) = &self.filter {
        //     return v.contains(&metadata.target().to_string());
        // }
        true
    }

    fn log(&self, record: &log::Record<'_>) {
        if !self.enabled(record.metadata()) {
            return;
        }
        println!("{}", record.args())
    }

    fn flush(&self) {}
}
