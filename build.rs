use std::path::PathBuf;

use walkdir::WalkDir;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let proto_source_dir = "local-dev/proto";
    let mut proto_files = Vec::new();

    for file in WalkDir::new(proto_source_dir) {
        let file = file?;
        if file.file_type().is_file()
            && file
                .path()
                .extension()
                .is_some_and(|extension| extension == "proto")
        {
            proto_files.push(file.into_path());
        }
    }

    if proto_files.is_empty() {
        return Err(format!("No .proto files found in {proto_source_dir}").into());
    }
    proto_files.sort();

    println!("cargo:rerun-if-changed=local-dev/proto");
    tonic_prost_build::configure()
        .build_client(true)
        .build_server(true)
        .compile_protos(&proto_files, &[PathBuf::from(proto_source_dir)])?;

    return Ok(());
}
