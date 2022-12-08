//! 用于操作注册表

use crate::{err, win::win_result};
use std::{ffi::OsStr, ptr};
use windows_sys::Win32::System::Registry::{
    RegCloseKey, RegCreateKeyExW, RegDeleteValueW, RegOpenKeyExW, RegQueryValueExW, RegSaveKeyExW,
    RegSetValueExW, HKEY, REG_VALUE_TYPE,
};

type Result<T> = crate::Result<T>;

/// *用于操作注册表
///
/// ```rust
/// use liblib::win::{RegHelper, RegKEY};
///
/// let helper = RegHelper::new(RegKEY::HKEY_CURRENT_USER);
/// let netset = helper.open("Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings")?;
///
/// let enable: u32 = netset.read("ProxyEnable")?.try_into()?;
/// let proxyserver: String = netset.read("ProxyServer")?.try_into()?;
///
/// assert!(netset.read("nullkey").is_err());
///
/// # Ok::<(), std::io::Error>(())
/// ```
///
#[derive(Debug)]
pub struct RegHelper {
    hkey: HKEY,
}

/// 注册表数据值
///
/// 支持[String], [Vec<String>], [u32], [u64]与[RegValue]相互转换
#[derive(Debug)]
pub struct RegValue {
    /// 获取到的原始数据
    pub bytes: Vec<u8>,
    /// 数据类型
    pub vtype: RegValueType,
}

/// 可以对注册表的数据增删改查
impl RegHelper {
    /// 创建一个RegHelper对象, 无任何实际操作
    pub fn new(key: RegKEY) -> Self {
        Self::_new(key.into())
    }

    const fn _new(hkey: HKEY) -> Self {
        Self { hkey }
    }

    /// 打开一个项, 打开需要权限, 默认使用用全部权限
    pub fn open<P: AsRef<OsStr>>(&self, path: P) -> Result<RegHelper> {
        self.open_with(path, RegFlag::KEY_ALL_ACCESS)
    }

    /// 打开一个项, 需要传入权限
    pub fn open_with<P: AsRef<OsStr>>(&self, path: P, perms: RegFlag) -> Result<RegHelper> {
        let name = crate::str::to_u16(path);
        let mut newhkey = HKEY::default();
        let status =
            unsafe { RegOpenKeyExW(self.hkey, name.as_ptr(), 0, perms.into(), &mut newhkey) };
        win_result(status as i32)?;
        Ok(RegHelper::_new(newhkey))
    }

    /// 创建一个项, 并返回该对象, 默认使用用全部权限
    pub fn create<P: AsRef<OsStr>>(&self, path: P) -> Result<RegHelper> {
        self.create_with(path, RegFlag::KEY_ALL_ACCESS)
    }

    /// 创建一个项, 并返回该对象
    pub fn create_with<P: AsRef<OsStr>>(&self, path: P, perms: RegFlag) -> Result<RegHelper> {
        let mut newhkey = HKEY::default();
        let status = unsafe {
            RegCreateKeyExW(
                self.hkey,
                crate::str::to_u16(path.as_ref()).as_mut_ptr(),
                0,
                ptr::null_mut(),
                windows_sys::Win32::System::Registry::REG_OPTION_NON_VOLATILE,
                perms.into(),
                ptr::null_mut(),
                &mut newhkey,
                ptr::null_mut(), // 成功返回的信息
            )
        };
        win_result(status as i32)?;
        Ok(RegHelper::_new(newhkey))
    }

    /// 读取一个值的数据
    pub fn read<P: AsRef<OsStr>>(&self, key: P) -> Result<RegValue> {
        let key = crate::str::to_u16(key);
        let name = key.as_ptr();
        let reser = ptr::null_mut();
        let data = ptr::null_mut();
        let mut lptype: REG_VALUE_TYPE = 0;
        let mut lplen = 0u32;
        // 先查询长度
        let status =
            unsafe { RegQueryValueExW(self.hkey, name, reser, &mut lptype, data, &mut lplen) };
        win_result(status as i32)?;
        let mut bytes = vec![0u8; lplen as usize];
        let data = bytes.as_mut_ptr();
        // 再查询值
        let status =
            unsafe { RegQueryValueExW(self.hkey, name, reser, &mut lptype, data, &mut lplen) };
        win_result(status as i32)?;
        // 如果返回的类型不在现有范围内, 会返回REG_NONE类型
        let vtype = RegValueType::from(lptype);
        Ok(RegValue { bytes, vtype })
    }

    /// 创建或者更改一个值的数据
    ///
    /// ```ignore
    /// let helper = RegHelper::new(RegKEY::HKEY_CURRENT_USER);
    /// let netset =
    /// helper.open("Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings")?;
    /// netset.set("KEY_test0", &RegValue::from("value_test_reg_1"))?;
    /// netset.set("KEY_test1",&RegValue::from(vec!["value_test_reg_1","value_test_reg_2","value_test_reg_3"]))?;
    /// ```
    pub fn set<P: AsRef<OsStr>>(&self, key: P, value: &RegValue) -> Result<()> {
        let mut key = crate::str::to_u16(key);
        let name = key.as_mut_ptr();
        let dtype = value.vtype.clone() as REG_VALUE_TYPE;
        let len = value.bytes.len() as u32;
        let data = value.bytes.as_ptr();
        let status = unsafe { RegSetValueExW(self.hkey, name, 0, dtype, data, len) };
        win_result(status as i32)?;
        Ok(())
    }

    /// 删除一个值
    pub fn del<P: AsRef<OsStr>>(&self, key: P) -> Result<()> {
        let mut key = crate::str::to_u16(key);
        let status = unsafe { RegDeleteValueW(self.hkey, key.as_mut_ptr()) };
        win_result(status as i32)?;
        Ok(())
    }

    /// 将当前项的值和数据导出
    ///
    /// 可能需要管理员权限
    pub fn export<F: AsRef<std::path::Path>>(&self, file: F) -> Result<()> {
        let lpfile = crate::str::to_u16(file.as_ref()).as_mut_ptr();
        let attr = ptr::null_mut();
        let flags = windows_sys::Win32::System::Registry::REG_NO_COMPRESSION;
        let status = unsafe { RegSaveKeyExW(self.hkey, lpfile, attr, flags) };
        win_result(status as i32)?;
        Ok(())
    }

    fn close(&self) -> Result<()> {
        if self.hkey >= windows_sys::Win32::System::Registry::HKEY_CLASSES_ROOT {
            return Ok(());
        }
        win_result(unsafe { RegCloseKey(self.hkey) } as i32)?;
        Ok(())
    }
}

impl Drop for RegHelper {
    fn drop(&mut self) {
        self.close().unwrap_or(());
    }
}

/// 将HKEY转成枚举
macro_rules! hkey {
    ([$($v:ident),*]) => {
        /// 操作表项
        #[allow(non_camel_case_types)]
        #[derive(Debug,Clone,PartialEq)]
        pub enum RegKEY{
            $(  /// from windows_sys::Win32::System::Registry::HKEY_*
                $v
            ),*
        }
        impl Into<HKEY> for RegKEY {
            fn into(self) -> HKEY {
                match self {
                    $(RegKEY::$v => return windows_sys::Win32::System::Registry::$v,)*
                };
            }
        }
    };
}

// 这些值是i32, 不能直接使用enum=的方法
hkey!([
    HKEY_CURRENT_USER,
    HKEY_CLASSES_ROOT,
    HKEY_LOCAL_MACHINE,
    HKEY_USERS,
    HKEY_CURRENT_CONFIG
]);

/// 将reg类型转为枚举
macro_rules! reg_value_type {
    ([$($v:ident),*]) => {
        /// 操作表数据可能的类型
        #[allow(non_camel_case_types)]
        #[derive(Debug,Clone,PartialEq)]
        pub enum RegValueType{
            $(  /// from windows_sys::Win32::System::Registry::REG_*
                $v
            ),*
        }
        impl From<REG_VALUE_TYPE> for RegValueType {
            fn from(t: REG_VALUE_TYPE) -> Self {
                match t {
                    $(windows_sys::Win32::System::Registry::$v => return RegValueType::$v,)*
                    _ => return RegValueType::REG_NONE
                }
            }
        }
    };
}

// cargo expand win::reg > src/win/reg_expand.rs  (主目录下运行)
reg_value_type!([
    REG_NONE,
    REG_SZ,
    REG_EXPAND_SZ,
    REG_BINARY,
    REG_DWORD,
    // REG_DWORD_LITTLE_ENDIAN, // 同REG_DWORD
    REG_DWORD_BIG_ENDIAN,
    REG_LINK,
    REG_MULTI_SZ,
    REG_RESOURCE_LIST,
    REG_FULL_RESOURCE_DESCRIPTOR,
    REG_RESOURCE_REQUIREMENTS_LIST,
    // REG_QWORD_LITTLE_ENDIAN // 同REG_QWORD
    REG_QWORD
]);

/// 将权限flag转为枚举
macro_rules! reg_flags {
    ([$($v:ident),*]) => {
        /// 操作表权限参数
        #[derive(Debug,Clone,PartialEq)]
        #[repr(u32)]
        #[allow(non_camel_case_types)]
        pub enum RegFlag{
            $(  /// from windows_sys::Win32::System::Registry::KEY_*
                $v = windows_sys::Win32::System::Registry::$v
            ),*
        }
        impl Into<u32> for RegFlag {
            fn into(self) -> u32 {
                match(self){
                    $(RegFlag::$v => return windows_sys::Win32::System::Registry::$v),*
                }
            }
        }
    };
}

reg_flags!([
    KEY_QUERY_VALUE,
    KEY_SET_VALUE,
    KEY_CREATE_SUB_KEY,
    KEY_ENUMERATE_SUB_KEYS,
    KEY_NOTIFY,
    KEY_CREATE_LINK,
    KEY_WOW64_32KEY,
    KEY_WOW64_64KEY,
    KEY_WOW64_RES,
    KEY_READ,
    KEY_WRITE,
    // KEY_EXECUTE, //与KEY_WRITE是同样的值
    KEY_ALL_ACCESS
]);

impl From<&str> for RegValue {
    fn from(str: &str) -> Self {
        let bytes = crate::str::to_u8_code(str);
        let vtype = RegValueType::REG_SZ;
        RegValue { bytes, vtype }
    }
}

impl From<Vec<&str>> for RegValue {
    fn from(str: Vec<&str>) -> Self {
        let mut strs = str
            .into_iter()
            .map(crate::str::to_u16)
            .collect::<Vec<_>>()
            .concat();
        strs.push(0);
        let bytes = crate::str::u16_to_u8(&strs);
        let vtype = RegValueType::REG_MULTI_SZ;
        RegValue { bytes, vtype }
    }
}

impl From<u32> for RegValue {
    fn from(num: u32) -> Self {
        let bytes =
            unsafe { std::slice::from_raw_parts((&num as *const u32) as *const u8, 4) }.to_vec();
        let vtype = RegValueType::REG_DWORD;
        RegValue { bytes, vtype }
    }
}

impl From<u64> for RegValue {
    fn from(num: u64) -> Self {
        let bytes =
            unsafe { std::slice::from_raw_parts((&num as *const u64) as *const u8, 8) }.to_vec();
        let vtype = RegValueType::REG_QWORD;
        RegValue { bytes, vtype }
    }
}

impl TryInto<String> for RegValue {
    type Error = crate::Error;

    fn try_into(self) -> Result<String> {
        match self.vtype {
            RegValueType::REG_SZ | RegValueType::REG_EXPAND_SZ | RegValueType::REG_MULTI_SZ => {
                let data = self.bytes.as_ptr() as *const u16;
                let len = self.bytes.len() / 2;
                let words = unsafe { std::slice::from_raw_parts(data, len) };
                let mut s = String::from_utf16_lossy(words);
                while s.ends_with('\u{0}') {
                    s.pop();
                }
                if self.vtype == RegValueType::REG_MULTI_SZ {
                    return Ok(s.replace('\u{0}', "\n"));
                }
                return Ok(s);
            }
            _ => return Err(err!("cannot get value by type {:?}", self.vtype)),
        }
    }
}

impl TryInto<Vec<String>> for RegValue {
    type Error = crate::Error;

    fn try_into(self) -> Result<Vec<String>> {
        match self.vtype {
            RegValueType::REG_MULTI_SZ => {
                let data = self.bytes.as_ptr() as *const u16;
                let len = self.bytes.len() / 2;
                let words = unsafe { std::slice::from_raw_parts(data, len) };
                let mut s = String::from_utf16_lossy(words);
                while s.ends_with('\u{0}') {
                    s.pop();
                }
                Ok(s.split('\u{0}').map(|x| x.to_owned()).collect())
            }
            _ => return Err(err!("cannot get value by type {:?}", self.vtype)),
        }
    }
}

impl TryInto<u32> for RegValue {
    type Error = crate::Error;

    fn try_into(self) -> Result<u32> {
        match self.vtype {
            RegValueType::REG_DWORD => return Ok(unsafe { *(self.bytes.as_ptr() as *const u32) }),
            _ => return Err(err!("cannot get type {:?} from regvalue", self.vtype)),
        }
    }
}

impl TryInto<u64> for RegValue {
    type Error = crate::Error;

    fn try_into(self) -> Result<u64> {
        match self.vtype {
            RegValueType::REG_QWORD => return Ok(unsafe { *(self.bytes.as_ptr() as *const u64) }),
            _ => return Err(err!("cannot get type {:?} from regvalue", self.vtype)),
        }
    }
}

// #[cfg(test)]
// mod tests {
//     use super::*;

//     #[test]
//     fn test_reg_set_del() -> crate::Result<()> {
//         let helper = RegHelper::new(RegKEY::HKEY_CURRENT_USER);
//         let n = helper.create("test_p")?;
//         n.set("test_1", &"value_test_reg_1".into())?;
//         n.export("a.reg")?;

//         // assert!(netset.read("KEY_test0").is_err());
//         // assert!(netset.read("KEY_test1").is_err());
//         // assert!(netset.read("KEY_test2").is_err());

//         // netset.set("KEY_test0", &RegValue::from("value_test_reg_1"))?;
//         // netset.set("KEY_test1", &RegValue::from(vec!["v_t_r_1", "v_t_r_2"]))?;
//         // netset.set("KEY_test2", &RegValue::from(1u32))?;

//         // let test0: String = netset.read("KEY_test0")?.try_into()?;
//         // assert_eq!("value_test_reg_1", test0);
//         // assert!(netset.read("KEY_test1").is_ok());
//         // assert!(netset.read("KEY_test2").is_ok());

//         // netset.del("KEY_test0")?;
//         // netset.del("KEY_test1")?;
//         // netset.del("KEY_test2")?;

//         // assert!(netset.read("KEY_test0").is_err());
//         // assert!(netset.read("KEY_test1").is_err());
//         // assert!(netset.read("KEY_test2").is_err());
//         Ok(())
//     }
// }
