module "network" {
  source = "./modules/network"

  project    = local.project
  vpc_cidr   = var.vpc_cidr
  aws_region = var.aws_region
}

module "ssm" {
  source = "./modules/ssm"

  ssm_prefix            = local.ssm_prefix
  db_password           = var.db_password
  stripe_webhook_secret = var.stripe_webhook_secret
  stripe_api_key        = var.stripe_api_key
}

module "rds" {
  source = "./modules/rds"

  project           = local.project
  subnet_ids        = module.network.private_subnet_ids
  security_group_id = module.network.rds_security_group_id
  db_name           = var.db_name
  db_username       = var.db_username
  db_password       = var.db_password
}

module "sqs" {
  source = "./modules/sqs"

  project = local.project
}

module "ses" {
  source = "./modules/ses"

  project    = local.project
  from_email = var.notification_from_email
}

module "lambda" {
  source = "./modules/lambda"

  project            = local.project
  lambda_name        = local.lambda_name
  jar_path           = var.lambda_jar_path
  log_retention_days = var.log_retention_days

  subnet_ids        = module.network.private_subnet_ids
  security_group_id = module.network.lambda_security_group_id

  sqs_queue_arn      = module.sqs.queue_arn
  ssm_parameter_arns = module.ssm.parameter_arns

  notification_from_email = var.notification_from_email

  environment_variables = {
    SSM_PARAMETER_PREFIX    = local.ssm_prefix
    NOTIFICATION_FROM_EMAIL = var.notification_from_email
    DB_URL                  = module.rds.jdbc_url
    DB_USER                 = var.db_username
    SES_CONFIGURATION_SET   = module.ses.configuration_set_name
  }
}

module "api_gateway" {
  source = "./modules/api-gateway"

  project            = local.project
  aws_region         = var.aws_region
  sqs_queue_arn      = module.sqs.queue_arn
  sqs_queue_name     = module.sqs.queue_name
  log_retention_days = var.log_retention_days
  apigw_account_id   = aws_api_gateway_account.this.cloudwatch_role_arn
}
