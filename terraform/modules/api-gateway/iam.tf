data "aws_iam_policy_document" "assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["apigateway.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "integration" {
  name               = "${var.project}-apigw-role"
  assume_role_policy = data.aws_iam_policy_document.assume.json
}

data "aws_iam_policy_document" "sqs_send" {
  statement {
    actions   = ["sqs:SendMessage"]
    resources = [var.sqs_queue_arn]
  }
}

resource "aws_iam_role_policy" "sqs_send" {
  name   = "${var.project}-apigw-sqs"
  role   = aws_iam_role.integration.id
  policy = data.aws_iam_policy_document.sqs_send.json
}
