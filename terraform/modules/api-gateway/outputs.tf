output "webhook_url" {
  description = "Full URL Stripe should POST to."
  value       = "${aws_apigatewayv2_api.this.api_endpoint}/${aws_apigatewayv2_stage.this.name}/webhook"
}

output "api_id" {
  description = "HTTP API identifier."
  value       = aws_apigatewayv2_api.this.id
}

output "stage_name" {
  description = "Deployed stage name."
  value       = aws_apigatewayv2_stage.this.name
}
