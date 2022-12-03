/// 返回[rfid]指定的地址
fn _dir(rfid: &windows_sys::core::GUID) -> super::WinResult<std::path::PathBuf> {
    use windows_sys::Win32;
    let mut path_ptr: windows_sys::core::PWSTR = std::ptr::null_mut();
    let htoken = Win32::Foundation::HANDLE::default();
    let dwflags = Win32::UI::Shell::KF_FLAG_DEFAULT;
    let result =
        unsafe { Win32::UI::Shell::SHGetKnownFolderPath(rfid, dwflags, htoken, &mut path_ptr) };
    super::win_result(result)?;
    let len = unsafe { Win32::Globalization::lstrlenW(path_ptr) as usize };
    let str = unsafe { std::slice::from_raw_parts(path_ptr, len) };
    use std::os::windows::ffi::OsStringExt;
    return Ok(std::path::PathBuf::from(std::ffi::OsString::from_wide(str)));
}

macro_rules! re_exports_enum_windir {
    ([$($name:ident),*]) => {
        ///
        /// *windows下的文件夹地址
        ///
        /// ```rust
        /// use liblib::win::WindowDir;
        ///
        /// WindowDir::FOLDERID_Desktop.get().is_ok();
        /// WindowDir::FOLDERID_RoamingAppData.get().is_ok();
        /// WindowDir::FOLDERID_Music.get().is_ok();
        /// WindowDir::FOLDERID_Pictures.get().is_ok();
        /// WindowDir::FOLDERID_Videos.get().is_ok();
        /// WindowDir::FOLDERID_Downloads.get().is_ok();
        /// WindowDir::FOLDERID_Documents.get().is_ok();
        /// WindowDir::FOLDERID_LocalAppData.get().is_ok();
        /// WindowDir::FOLDERID_Profile.get().is_ok();
        /// WindowDir::FOLDERID_Startup.get().is_ok();
        /// WindowDir::FOLDERID_StartMenu.get().is_ok();
        /// WindowDir::FOLDERID_ProgramFiles.get().is_ok();
        /// WindowDir::FOLDERID_ProgramFilesX64.get().is_ok();
        /// WindowDir::FOLDERID_ProgramFilesX86.get().is_ok();
        /// WindowDir::FOLDERID_SavedGames.get().is_ok();
        /// WindowDir::FOLDERID_System.get().is_ok();
        /// WindowDir::FOLDERID_Windows.get().is_ok();
        /// ```
        ///
        #[allow(non_camel_case_types)]
        #[derive(Debug)]
        pub enum WindowDir{
            $(
                /// from windows_sys::Win32::UI::Shell::*
                $name
            ),*
        }
        impl Into<windows_sys::core::GUID> for WindowDir {
            fn into(self) -> windows_sys::core::GUID {
                match self {
                    $(WindowDir::$name => return windows_sys::Win32::UI::Shell::$name,)*
                };
            }
        }
    };
}

// cargo expand win::dir > src/win/dir_expand.rs  (主目录下运行)
re_exports_enum_windir!([
    FOLDERID_Desktop,
    FOLDERID_RoamingAppData,
    FOLDERID_Music,
    FOLDERID_Pictures,
    FOLDERID_Videos,
    FOLDERID_Downloads,
    FOLDERID_Documents,
    FOLDERID_LocalAppData,
    FOLDERID_Profile,
    FOLDERID_Startup,
    FOLDERID_StartMenu,
    FOLDERID_ProgramFiles,
    FOLDERID_ProgramFilesX64,
    FOLDERID_ProgramFilesX86,
    FOLDERID_SavedGames,
    FOLDERID_System,
    FOLDERID_Windows
]);

impl WindowDir {
    /// 获取该文件夹的绝对路径
    pub fn get(self) -> super::WinResult<std::path::PathBuf> {
        _dir(&self.into())
    }
}

#[cfg(test)]
mod tests {
    use super::{WindowDir::*, *};

    #[test]
    fn test_dir() {
        fn test_win_dir(d: WindowDir) {
            println!("{}: {:?}", stringify!(d), d.get());
        }
        test_win_dir(FOLDERID_Desktop);
        test_win_dir(FOLDERID_RoamingAppData);
        test_win_dir(FOLDERID_Music);
        test_win_dir(FOLDERID_Pictures);
        test_win_dir(FOLDERID_Videos);
        test_win_dir(FOLDERID_Downloads);
        test_win_dir(FOLDERID_Documents);
        test_win_dir(FOLDERID_LocalAppData);
        test_win_dir(FOLDERID_Profile);
        test_win_dir(FOLDERID_Startup);
        test_win_dir(FOLDERID_StartMenu);
        test_win_dir(FOLDERID_ProgramFiles);
        test_win_dir(FOLDERID_ProgramFilesX64);
        test_win_dir(FOLDERID_ProgramFilesX86);
        test_win_dir(FOLDERID_SavedGames);
        test_win_dir(FOLDERID_System);
        test_win_dir(FOLDERID_Windows);
    }
}
