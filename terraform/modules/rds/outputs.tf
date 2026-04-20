output "endpoint" {
  description = "Host:port string suitable for building a JDBC URL."
  value       = aws_db_instance.this.endpoint
}

output "jdbc_url" {
  description = "JDBC URL to pass to the Lambda as DB_URL."
  value       = "jdbc:postgresql://${aws_db_instance.this.endpoint}/${aws_db_instance.this.db_name}"
}

output "db_name" {
  description = "PostgreSQL database name."
  value       = aws_db_instance.this.db_name
}
