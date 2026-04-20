locals {
  project     = "${var.project_name}-${var.environment}"
  ssm_prefix  = "/${var.project_name}/${var.environment}"
  lambda_name = "${local.project}-handler"
}
