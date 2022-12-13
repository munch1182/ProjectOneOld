//!
//! 字符串相关
//!
#![deny(missing_docs, unused_imports)]

///
/// 将首字母大写, 不会更改其它字符
///
/// ```rust
/// use liblib::str::upper_first;
///
/// assert_eq!(upper_first("string"),String::from("String"));
/// ```
///
pub fn upper_first(s: &str) -> String {
    let mut chars = s.chars();
    match chars.next() {
        None => return String::new(),
        Some(c) => return c.to_uppercase().collect::<String>() + chars.as_str(),
    }
}

/// 将字符转为u16集合, 适合写入window系统
///
/// ```rust
/// assert_eq!(liblib::str::to_u16("a"), vec![97, 0]);
/// ```
#[cfg(target_os = "windows")]
pub fn to_u16<P: AsRef<std::ffi::OsStr>>(p: P) -> Vec<u16> {
    use std::os::windows::prelude::OsStrExt;
    p.as_ref()
        .encode_wide()
        .chain(Some(0).into_iter()) // 只是在最后一位加上了0
        .collect()
}

/// 将u16字符转为u8字符
///
/// ```rust
///
/// assert_eq!(liblib::str::u16_to_u8(&vec![97, 0]), vec![97, 0, 0, 0]);
/// assert_eq!(liblib::str::u16_to_u8(&vec![97, 0]), vec![97, 0, 0, 0]);
///
/// ```
///
pub fn u16_to_u8(u: &[u16]) -> Vec<u8> {
    use std::slice;
    unsafe { slice::from_raw_parts(u.as_ptr() as *const u8, u.len() * 2).to_vec() }
}

/// 将字符转为u16, 再转为u8
///
/// ```rust
/// assert_eq!(liblib::str::to_u8_code("a"), vec![97, 0, 0, 0]);
/// ```
///
/// [to_u16]
/// [u16_to_u8]
#[cfg(target_os = "windows")]
pub fn to_u8_code<P: AsRef<std::ffi::OsStr>>(p: P) -> Vec<u8> {
    u16_to_u8(&to_u16(p))
}
