//!
//! 基础逻辑库
//!
#![deny(missing_docs)]

mod macros;

///
/// 依赖于windows环境的方法
///
// #[cfg(feature = "win")]
pub mod win;

/// std::io::Error => liblib::Error
pub type Error = std::io::Error;
/// std::io::Result<T> => liblib::Result<T>
pub type Result<T, E = Error> = std::result::Result<T, E>;
pub mod str;
pub use lazy_static::LazyStatic;
pub use macros::*;
