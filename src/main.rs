mod entity;
mod migration;
mod proto;

use std::env;

use sea_orm::Database;
use sea_orm_migration::MigratorTrait;
use tonic::transport::Server;

use migration::Migrator;
use tracing::info;
use tracing_subscriber::EnvFilter;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt()
        .with_env_filter(
            EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| EnvFilter::new("competition=debug,info")),
        )
        .init();

    let database_url = std::env::var("DATABASE_URL").map_err(|_| "DATABASE_URL is not set")?;

    let db = Database::connect(&database_url).await?;
    Migrator::up(&db, None).await?; // apply any migrations that haven't run yet

    let grpc_port: u16 = env::var("GRPC_PORT").unwrap().parse().unwrap();
    let addr = format!("0.0.0.0:{grpc_port}").parse()?;
    info!(%addr, "HelloService listening");

    Server::builder()
        .add_service(None)
        .serve(addr)
        .await?;

    Ok(())
}
