use liblib::derver::{Builder, EnumName};

#[derive(Debug, PartialEq, EnumName)]
pub enum E1 {
    A,
    B,
    C,
}

#[derive(Debug, Builder)]
pub struct Config {
    path: String,
    arg: u8,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_eunmname() -> liblib::Result<()> {
        let a: &str = E1::A.into();
        assert_eq!("A", a);
        assert_eq!(E1::try_from("A")?, E1::A);
        Ok(())
    }

    #[test]
    fn test_builder() -> liblib::Result<()> {
        let config = Config::builder().path(String::from("./")).arg(1).build();
        assert!(config.is_ok());
        Ok(())
    }
}
