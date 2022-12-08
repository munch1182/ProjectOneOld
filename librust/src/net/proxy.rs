//!
//! 网络代理相关

use axum::Router;
use reqwest::StatusCode;

use crate::err_to;

type NetResult<T> = crate::Result<T>;
/// 网络代理
#[derive(Debug)]
pub struct NetProxy;

impl NetProxy {
    /// 在线程中开启代理服务, 使用随机可用的端口, 成功则返回该端口
    pub fn start_new_thread() -> NetResult<u16> {
        Self::start_new_thread_port(0)
    }

    /// 在线程中开启代理服务并指定端口, 成功则返回端口地址
    pub fn start_new_thread_port(port: u16) -> NetResult<u16> {
        tokio::runtime::Builder::new_multi_thread()
            .enable_all()
            .build()?
            .block_on(Self::start_port(port))
    }

    /// 开启一个代理服务, 使用随机可用的端口, 成功则返回该端口
    pub async fn start() -> NetResult<u16> {
        Self::start_port(0).await
    }

    /// 开启一个代理服务并指定端口, 成功则返回端口地址
    pub async fn start_port(port: u16) -> NetResult<u16> {
        let addr = &err_to!(format!("0.0.0.0:{}", port).parse())?;
        let app = Router::new().fallback(Self::fallback);
        let server = axum::Server::bind(addr).serve(app.into_make_service());

        let port = server.local_addr().port();
        log::debug!("start server: port:{}", port);

        match server.await {
            Ok(_) => Ok(port),
            Err(_) => Err(crate::err!()),
        }
    }

    async fn fallback() -> (StatusCode, String) {
        (StatusCode::OK, format!("No route for 1"))
    }
}
