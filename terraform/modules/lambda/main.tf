resource "aws_cloudwatch_log_group" "this" {
  name              = "/aws/lambda/${var.lambda_name}"
  retention_in_days = var.log_retention_days
  tags              = { Name = "${var.lambda_name}-logs" }
}

resource "aws_lambda_function" "this" {
  function_name    = var.lambda_name
  role             = aws_iam_role.this.arn
  runtime          = "java21"
  handler          = var.handler
  memory_size      = var.memory_mb
  timeout          = var.timeout_seconds
  filename         = var.jar_path
  source_code_hash = filebase64sha256(var.jar_path)
  publish          = true

  snap_start {
    apply_on = "PublishedVersions"
  }

  vpc_config {
    subnet_ids         = var.subnet_ids
    security_group_ids = [var.security_group_id]
  }

  environment {
    variables = var.environment_variables
  }

  depends_on = [
    aws_cloudwatch_log_group.this,
    aws_iam_role_policy.inline,
    aws_iam_role_policy_attachment.vpc,
  ]
}

resource "aws_lambda_alias" "live" {
  name             = "live"
  function_name    = aws_lambda_function.this.function_name
  function_version = aws_lambda_function.this.version
}

resource "aws_lambda_event_source_mapping" "sqs" {
  event_source_arn                   = var.sqs_queue_arn
  function_name                      = aws_lambda_alias.live.arn
  batch_size                         = var.batch_size
  function_response_types            = ["ReportBatchItemFailures"]
  maximum_batching_window_in_seconds = 0
}
