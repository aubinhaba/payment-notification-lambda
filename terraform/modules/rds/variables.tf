variable "project" {
  description = "Project + environment slug used in resource names."
  type        = string
}

variable "subnet_ids" {
  description = "Private subnet IDs for the RDS subnet group."
  type        = list(string)
}

variable "security_group_id" {
  description = "RDS security group ID — ingress restricted to the Lambda SG."
  type        = string
}

variable "db_name" {
  description = "Initial PostgreSQL database name."
  type        = string
}

variable "db_username" {
  description = "Master username."
  type        = string
}

variable "db_password" {
  description = "Master password."
  type        = string
  sensitive   = true
}
