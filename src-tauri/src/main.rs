// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

pub mod plugin;


fn main() {
    tauri::Builder::default()
        .plugin(plugin::WindowsShandow::new())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
