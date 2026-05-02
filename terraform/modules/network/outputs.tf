output "vpc_id" {
  description = "ID of the VPC owning all private subnets and endpoints."
  value       = aws_vpc.this.id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets used by Lambda ENIs and the RDS subnet group."
  value       = aws_subnet.private[*].id
}

output "lambda_security_group_id" {
  description = "Security group attached to the Lambda ENIs."
  value       = aws_security_group.lambda.id
}

output "rds_security_group_id" {
  description = "Security group attached to the RDS instance."
  value       = aws_security_group.rds.id
}
