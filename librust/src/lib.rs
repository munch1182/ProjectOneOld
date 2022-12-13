//!
//! 基础逻辑库
//!
#![warn(
    clippy::all,
    clippy::dbg_macro,
    clippy::todo,
    clippy::empty_enum,
    clippy::enum_glob_use,
    clippy::mem_forget,
    clippy::unused_self,
    clippy::filter_map_next,
    clippy::needless_continue,
    clippy::needless_borrow,
    clippy::match_wildcard_for_single_variants,
    clippy::if_let_mutex,
    clippy::mismatched_target_os,
    clippy::await_holding_lock,
    clippy::match_on_vec_items,
    clippy::imprecise_flops,
    clippy::suboptimal_flops,
    clippy::lossy_float_literal,
    clippy::rest_pat_in_fully_bound_structs,
    clippy::fn_params_excessive_bools,
    clippy::exit,
    clippy::inefficient_to_string,
    clippy::linkedlist,
    clippy::macro_use_imports,
    clippy::option_option,
    clippy::verbose_file_reads,
    clippy::unnested_or_patterns,
    clippy::str_to_string,
    rust_2018_idioms,
    future_incompatible
)]
#![deny(unreachable_pub, private_in_public, missing_docs)]
#![cfg_attr(docsrs, feature(doc_auto_cfg, doc_cfg))]

mod slog;
/// macros
#[macro_use]
pub(crate) mod macros;
///
/// 依赖于windows环境的方法
///
/// require features `win`
///
#[cfg(all(windows, feature = "win"))]
pub mod win;

/// std::io::Error => liblib::Error
pub type Error = std::io::Error;
/// T: std::io::Result => liblib::Result
pub type Result<T, E = Error> = std::result::Result<T, E>;
/// 字符串相关
pub mod str;

///
/// 网络相关
///
/// require features `net`
///
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
