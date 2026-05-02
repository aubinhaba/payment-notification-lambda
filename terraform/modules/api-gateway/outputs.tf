output "webhook_url" {
  description = "Full URL Stripe should POST to."
  value       = "${aws_api_gateway_stage.this.invoke_url}/webhook"
}

output "api_id" {
  description = "REST API identifier."
  value       = aws_api_gateway_rest_api.this.id
}

output "stage_name" {
  description = "Deployed stage name."
  value       = aws_api_gateway_stage.this.stage_name
}
