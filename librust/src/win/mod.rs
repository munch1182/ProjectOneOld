//!
//! 与系统相关的基础功能库
//!
#![deny(missing_docs, unused_imports)]

/// 错误为win返回的错误码
pub type WinResult<T> = crate::Result<T, std::io::Error>;
///
/// windows下的相关文件夹位置
///
mod dir;
mod reg;

pub use dir::*;
pub use reg::*;

pub(crate) fn win_result(status: i32) -> WinResult<()> {
    return match status {
        0 => Ok(()),
        _ => Err(std::io::Error::from_raw_os_error(status)),
    };
}
