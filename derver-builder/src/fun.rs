pub fn map_fields<F>(fields: &syn::Fields, mapper: F) -> proc_macro2::TokenStream
where
    F: Fn((&proc_macro2::Ident, &syn::Type)) -> proc_macro2::TokenStream,
{
    proc_macro2::TokenStream::from_iter(
        fields
            .iter()
            .map(|f| (f.ident.as_ref().unwrap(), &f.ty))
            .map(mapper),
    )
}
