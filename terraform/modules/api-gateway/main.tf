resource "aws_cloudwatch_log_group" "access" {
  name              = "/aws/apigateway/${var.project}"
  retention_in_days = var.log_retention_days
  tags              = { Name = "${var.project}-apigw-logs" }
}

resource "aws_apigatewayv2_api" "this" {
  name          = "${var.project}-api"
  protocol_type = "HTTP"
  description   = "Entry point for Stripe webhooks — forwards the payload to SQS without invoking the Lambda directly."
}

# AWS service integration — API Gateway calls SQS:SendMessage directly, no Lambda in the hot path.
# The Stripe-Signature header is forwarded as an SQS message attribute so the consumer
# can re-verify the signature before any processing.
resource "aws_apigatewayv2_integration" "sqs" {
  api_id                 = aws_apigatewayv2_api.this.id
  integration_type       = "AWS_PROXY"
  integration_subtype    = "SQS-SendMessage"
  credentials_arn        = aws_iam_role.integration.arn
  payload_format_version = "1.0"

  request_parameters = {
    QueueUrl    = var.sqs_queue_url
    MessageBody = "$request.body"
    MessageAttributes = jsonencode({
      "Stripe-Signature" = {
        DataType    = "String"
        StringValue = "$request.header.Stripe-Signature"
      }
    })
  }
}

resource "aws_apigatewayv2_route" "webhook" {
  api_id    = aws_apigatewayv2_api.this.id
  route_key = "POST /webhook"
  target    = "integrations/${aws_apigatewayv2_integration.sqs.id}"
}

resource "aws_apigatewayv2_stage" "this" {
  api_id      = aws_apigatewayv2_api.this.id
  name        = var.stage_name
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.access.arn
    format = jsonencode({
      requestId        = "$context.requestId"
      httpMethod       = "$context.httpMethod"
      routeKey         = "$context.routeKey"
      status           = "$context.status"
      responseLength   = "$context.responseLength"
      integrationError = "$context.integrationErrorMessage"
    })
  }

  default_route_settings {
    throttling_burst_limit = 100
    throttling_rate_limit  = 50
  }
}
