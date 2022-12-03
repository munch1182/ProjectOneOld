///
/// 创建一个std::io::Error
///
#[macro_export]
macro_rules! err {
    () => {
        std::io::Error::new(std::io::ErrorKind::Other, "error")
    };
    // 直接将字符转为std::io::Error
    ($e:expr) => {
        std::io::Error::new(std::io::ErrorKind::Other, $e)
    };

    // 字符带参数
    ($($e:tt)*) => {
        std::io::Error::new(std::io::ErrorKind::Other, format!($($e)*))
    };
}

///
/// 将其它类型的Result转为Err类型为std::io::Error类型的Result
///
/// ```rust
/// use lib::{err_to};
///  
/// let result1: Result<i32,std::num::ParseIntError> = i32::from_str_radix("a12", 10);
/// let result1: Result<i32,std::io::Error> = err_to!(result1);
///
/// let result2: Result<i32,std::num::ParseIntError> = i32::from_str_radix("a12", 10);
/// let result2: Result<i32,std::io::Error> = err_to!(result2, "err text");
///
/// let result3: Result<i32,std::num::ParseIntError> = i32::from_str_radix("a12", 10);
/// let result3: Result<i32,std::io::Error> = err_to!(result3, "err text: {}", "a12");
///
/// ```
#[macro_export]
macro_rules! err_to {
    ($e: expr) => {
        match $e {
            Ok(val) => Ok(val),
            Err(err) => Err(std::io::Error::new(std::io::ErrorKind::Other,(format!("{}", err)))), // 没有传递err类型
        }
    };
    // 第二个参数可传入自定义原因, 用以替代原来的原因, 原来的错误信息会被忽略
    ($e: expr, $($s:tt)*) => {
        match $e {
            Ok(val) => Ok(val),
            Err(_) => Err(err!($($s)*)),
        }
    };
}

///
/// 将一个类型的一些值重导出成枚举
///
#[macro_export]
macro_rules! re_exports_to_enum {
    ($t:ident,[$($v:expr => $name:ident),*]) => {
        pub enum $t {
            $(
                $name = $v
            ),*
        }
    };
}
