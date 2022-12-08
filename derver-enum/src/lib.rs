//!
//! EnumNumber
//!
#![deny(missing_docs)]
use proc_macro::TokenStream;
use quote::quote;
use syn::{parse_macro_input, Data, DataEnum, DeriveInput};

///
#[proc_macro_derive(EnumNumber)]
pub fn derive_builder_number(_input: TokenStream) -> TokenStream {
    // let _input = input.clone();
    // let _input = parse_macro_input!(_input as DeriveInput);
    // let ident = &_input.ident;
    // // let mut from = Vec::new();
    // // let mut into = Vec::new();
    // if let Data::Enum(DataEnum { variants, .. }) = _input.data {
    //     for variant in variants {
    //         let ident_item = &variant.ident;
    //         for a in variant.attrs {
    //             if let Ok(m) = a.parse_meta() {
    //                 if let Some(i) = m.path().get_ident() {
    //                     if i == "repr" {
    //                         m.ne
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     return quote! {
    //         impl Into<isize> for #ident {
    //             fn into(self) -> isize {
    //                 // match self {
    //                 //     #(#into)*
    //                 // }
    //                 self as isize
    //             }
    //         }
    //         impl TryFrom<isize> for #ident {
    //             type Error = std::io::Error;
    //             fn try_from(value: isize) -> Result<Self, Self::Error> {
    //                 // match value {
    //                 //     #(#from)*
    //                 //     _ => Err(std::io::Error::new(std::io::ErrorKind::Other, format!("no enum for {}",value)))
    //                 // }
    //                 Ok(std::mem::transmute(value as usize))
    //             }
    //         }
    //     }
    //     .into();
    // }
    panic!("not imp")
}

/// EnumNumber
///
/// ```ignore
/// #[derive(Debug, PartialEq, EnumName)]
/// pub enum E1 {
///     A,
///     B,
///     C,
/// }
///
/// let a: &str = E1::A.into();
/// assert_eq!("A", a);
/// assert_eq!(E1::try_from("A")?, E1::A);
///
/// ```
#[proc_macro_derive(EnumName)]
pub fn derive_builder_name(input: TokenStream) -> TokenStream {
    let _input = input.clone();
    let _input = parse_macro_input!(_input as DeriveInput);
    let ident = &_input.ident;

    let mut from = Vec::new();
    let mut into = Vec::new();
    if let Data::Enum(DataEnum { variants, .. }) = _input.data {
        for variant in variants {
            let ident_item = &variant.ident;
            from.push(quote! {stringify!(#variant) => Ok(#ident::#ident_item),});
            into.push(quote! {#ident::#ident_item => stringify!(#variant),})
        }
        return quote! {
            impl Into<&'static str> for #ident {
                fn into(self) -> &'static str {
                    match self {
                        #(#into)*
                    }
                }
            }
            impl TryFrom<&'static str> for #ident {
                type Error = std::io::Error;

                fn try_from(value: &'static str) -> Result<Self, Self::Error> {
                    match value {
                        #(#from)*
                        _ => Err(std::io::Error::new(std::io::ErrorKind::Other, format!("no enum for {}",value)))
                    }
                }
            }
        }
        .into();
    }
    panic!("not enum")
}
