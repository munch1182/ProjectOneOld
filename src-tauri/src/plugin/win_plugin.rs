use tauri::{plugin::{Plugin}, Runtime, Window};
use window_shadows::set_shadow;

pub struct WindowsShandow {
}

impl WindowsShandow {
    pub fn new() -> Self {
        Self {}
    }
}

impl<R: Runtime> Plugin<R> for WindowsShandow{
    fn name(&self) -> &'static str {
        "com.munch.WindowsShandow"
    }

    fn created(&mut self, window: Window<R>) {
       if let Err(_) = set_shadow(&window, true) {
           println!("Failed to set shadow");
       }
    }
}