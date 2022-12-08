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
/// use liblib::err_to;
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
            Err(_) => Err(liblib::err!($($s)*)),
        }
    };
}

/// 将一个类型的一些值重导出成枚举, 这个类型必须是isize
///
/// ```rust
///
/// use liblib::enum_from_isize;
///
/// enum_from_isize!(#[derive(Debug,PartialEq)], pub, TestEnum, [ 1 => Enable, 2 => Disable]);
/// enum_from_isize!(,, TestEnum1, [ 1 => Enable, 2 => Disable]);
///
/// ```
/// $meta 用于标记声明
/// $vis 可见性, pub pub(crate) pub(super)
/// $enum 枚举的名称
/// $isize 数据值
/// $name 枚举值的名称
///
#[macro_export]
macro_rules! enum_from_isize {
    ($(#[$meta:meta])*, $vis:vis, $enum:ident, [$($isize:expr => $name:ident), *]) => {
        $(#[$meta])*
        $vis enum $enum {
            $(  /// from macro
                $name = $isize
            ),*
        }
    };
}

/// 将一个类型的一些值重导出成枚举, 不建议直接使用, 参数太多了
///
/// 通过宏声明了枚举并实现了这个枚举与指定类型的TryFrom
///
/// ```rust
/// use liblib::enum_from_any;
/// use TT::TTT;
///
/// pub mod TT {
///     pub mod TTT {
///         #[derive(Debug, PartialEq, Eq)] // 因为是使用match, 所以必须是能比较的数据
///         pub struct Any {
///             v: u8,
///         }
///
///         pub const A1: Any = Any { v: 1 };
///         pub const A2: Any = Any { v: 2 };
///         pub const A3: Any = Any { v: 3 };
///     }
/// }
///
/// // 参数太多了, 建议使用时自行实现, 则只需要传最后一个参数即可
/// enum_from_any!(#[derive(Debug,PartialEq)],,TestEnum3, TTT, TT::TTT::Any, [A1 => F, A2 => T, A3 => S ]);
///
/// assert_eq!(TestEnum3::F, TT::TTT::A1.try_into().unwrap());
///
/// ```
///
/// $meta 用于标记声明
/// $vis 可见性, pub pub(crate) pub(super)
/// $enum 枚举的名称
/// $vtype 值的类型
/// $from 值的前缀
/// $any 数据值的后面一部分
/// $name 枚举值的名称
#[macro_export]
macro_rules! enum_from_any {
    ($(#[$meta:meta])*, $vis:vis, $enum:ident, $from:ident, $vtype:path, [$($any:ident => $name:ident), *]) => {
        $(#[$meta])*
        $vis enum $enum {
            $(  /// from macro
                $name
            ),*
        }

        impl TryFrom<$vtype> for $enum {
            type Error = std::io::Error;
            fn try_from(v: $vtype) -> Result<Self,Self::Error> {
                match v {
                    $($from::$any => return Ok($enum::$name),)*
                    _ =>return Err(std::io::Error::new(std::io::ErrorKind::Other, "error"))
                }
            }
        }
    };
}

/// 异步执行, 使用tokio
///
/// ```ignore
/// let result = async_run!({ // 异步线程使用异步环境
///     let a = 1;
///     a*2
/// });
///
/// let result = async_run!(, { // 当前线程使用异步环境
///     let a = 1;
///     a*2
/// });
///
/// let result = async_run!({
///     start_server()?;
///     Ok::<(),Error>(())
/// });
/// ```
#[macro_export]
macro_rules! async_run {
    ($f:expr) => {
        tokio::runtime::Builder::new_multi_thread()
            .enable_all()
            .build()?
            .block_on(async { $f })
    };
    (,$f:expr) => {
        tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()?
            .block_on(async { $f })
    };
}
