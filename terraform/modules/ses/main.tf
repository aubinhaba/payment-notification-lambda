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
