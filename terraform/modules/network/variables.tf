variable "project" {
  description = "Project + environment slug used in resource names."
  type        = string
}

variable "vpc_cidr" {
  description = "IPv4 CIDR block for the VPC."
  type        = string
}

variable "aws_region" {
  description = "AWS region (used when constructing interface endpoint service names)."
  type        = string
}
