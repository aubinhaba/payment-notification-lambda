# The email identity must be verified out-of-band (click the confirmation link AWS sends)
# before SES will accept a SendEmail call with it as the From: address.
resource "aws_sesv2_email_identity" "from" {
  email_identity = var.from_email
}

resource "aws_sesv2_configuration_set" "this" {
  configuration_set_name = "${var.project}-config-set"

  reputation_options {
    reputation_metrics_enabled = true
  }

  sending_options {
    sending_enabled = true
  }
}
