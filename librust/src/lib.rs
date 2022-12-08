//!
//! 基础逻辑库
//!
#![deny(missing_docs)]

mod macros;
mod slog;
///
/// 依赖于windows环境的方法
///
/// require features `win`
///
#[cfg(feature = "win")]
#[cfg(target_os = "windows")]
pub mod win;

/// std::io::Error => liblib::Error
pub type Error = std::io::Error;
/// T: std::io::Result => liblib::Result
pub type Result<T, E = Error> = std::result::Result<T, E>;
/// 字符串相关
pub mod str;

/// 宏相关
pub use macros::*;
/// 网络相关
#[cfg(feature = "net")]
pub mod net;
/// 重导出log, 使用需要调用
pub use log::{debug, error, info, trace, warn};
pub use slog::{log_set, LogType};

/// proc-macro
pub mod derver {
    #[cfg(feature = "d_builder")]
    pub use derver_builder::Builder;
    #[cfg(feature = "d_enum")]
    pub use derver_enum::EnumName;
    #[cfg(feature = "d_enum")]
    pub use derver_enum::EnumNumber;
}
