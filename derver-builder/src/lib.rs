//!
//! proc-macro
//!
#![deny(missing_docs, unused_imports)]

mod fun;

use fun::map_fields;
use proc_macro::TokenStream;
use quote::quote;
use syn::{parse_macro_input, Data, DeriveInput, Fields, Type};

fn _field_to_opt((i, t): (&proc_macro2::Ident, &Type)) -> proc_macro2::TokenStream {
    let i = quote::format_ident!("_{}", i);
    quote! {
        /// generate from macro
        #i: Option<#t>,
    }
}

fn _field_to_set_fun((i, t): (&proc_macro2::Ident, &Type)) -> proc_macro2::TokenStream {
    let filed_i = quote::format_ident!("_{}", i);
    quote! {
        /// set from macro
        pub fn #i(mut self, value: #t) -> Self {
            self.#filed_i = Some(value);
            self
        }
    }
}

fn _field_to_get((i, _): (&proc_macro2::Ident, &Type)) -> proc_macro2::TokenStream {
    let build_i = quote::format_ident!("_{}", i);
    quote! {
        let #i = self.#build_i.ok_or(format!("need {}",stringify!(#i)))?;
    }
}

/// builder宏
///
/// ```ignore
/// #[derive(Debug, Builder)]
/// pub struct Config {
///     path: String,
///     arg: u8,
/// }
///
/// let config = Config::builder().path(String::from("./")).arg(1).build();
/// assert!(config.is_ok());
/// ```
///
#[proc_macro_derive(Builder)]
pub fn derive_builder(input: TokenStream) -> TokenStream {
    let _input = input.clone();
    let _input = parse_macro_input!(_input as DeriveInput);
    let ident = &_input.ident;
    if let Data::Struct(r#struct) = _input.data {
        let fields = r#struct.fields;
        if matches!(&fields, Fields::Named(_)) {
            // 根据struct的属性生成对应的Option属性, 并在名字前加上_
            let builder_fields = map_fields(&fields, _field_to_opt);
            // 根据struct的属性生成对应set方法, 方法名与属性保持一致
            let builder_fun = map_fields(&fields, _field_to_set_fun);
            // 根据struct的属性取出Option中的值, 如果没有值, 则向上抛出异常
            let build_lets = map_fields(&fields, _field_to_get);
            // 将struct的属性铺开, 和build_lets组成则组成struct
            let build_values = map_fields(&fields, |(i, _)| quote!(#i,));
            // 根据struct的名字生成struct+Builder的新struct
            let ident_builder = quote::format_ident!("{}Builder", ident);
            return quote! {
                impl #ident {
                    pub fn builder() -> #ident_builder {
                        #ident_builder::default()
                    }
                }

                #[derive(Default)]
                pub struct #ident_builder{
                    #builder_fields
                }

                impl #ident_builder{

                    #builder_fun

                    pub fn build(self) -> Result<#ident, String> {
                        #build_lets
                        Ok(#ident { #build_values })
                    }
                }
            }
            .into();
        }
    }
    panic!("unsupport")
}
