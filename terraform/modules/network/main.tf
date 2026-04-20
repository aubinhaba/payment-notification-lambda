data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = { Name = "${var.project}-vpc" }
}

resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.this.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags              = { Name = "${var.project}-private-${count.index + 1}" }
}

resource "aws_security_group" "lambda" {
  name        = "${var.project}-lambda-sg"
  description = "Egress only — for Lambda in VPC"
  vpc_id      = aws_vpc.this.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Outbound to VPC endpoints and RDS"
  }

  tags = { Name = "${var.project}-lambda-sg" }
}

resource "aws_security_group" "rds" {
  name        = "${var.project}-rds-sg"
  description = "Allow PostgreSQL from Lambda SG only"
  vpc_id      = aws_vpc.this.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.lambda.id]
    description     = "PostgreSQL from Lambda"
  }

  tags = { Name = "${var.project}-rds-sg" }
}

resource "aws_security_group" "endpoints" {
  name        = "${var.project}-endpoints-sg"
  description = "HTTPS from Lambda SG to VPC interface endpoints"
  vpc_id      = aws_vpc.this.id

  ingress {
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.lambda.id]
    description     = "HTTPS from Lambda"
  }

  tags = { Name = "${var.project}-endpoints-sg" }
}

# Interface endpoints so the Lambda can reach SES, SSM, SQS without a NAT Gateway.
resource "aws_vpc_endpoint" "ssm" {
  vpc_id              = aws_vpc.this.id
  service_name        = "com.amazonaws.${var.aws_region}.ssm"
  vpc_endpoint_type   = "Interface"
  private_dns_enabled = true
  subnet_ids          = aws_subnet.private[*].id
  security_group_ids  = [aws_security_group.endpoints.id]
  tags                = { Name = "${var.project}-ssm-endpoint" }
}

resource "aws_vpc_endpoint" "sqs" {
  vpc_id              = aws_vpc.this.id
  service_name        = "com.amazonaws.${var.aws_region}.sqs"
  vpc_endpoint_type   = "Interface"
  private_dns_enabled = true
  subnet_ids          = aws_subnet.private[*].id
  security_group_ids  = [aws_security_group.endpoints.id]
  tags                = { Name = "${var.project}-sqs-endpoint" }
}

resource "aws_vpc_endpoint" "ses" {
  vpc_id              = aws_vpc.this.id
  service_name        = "com.amazonaws.${var.aws_region}.email-smtp"
  vpc_endpoint_type   = "Interface"
  private_dns_enabled = true
  subnet_ids          = aws_subnet.private[*].id
  security_group_ids  = [aws_security_group.endpoints.id]
  tags                = { Name = "${var.project}-ses-endpoint" }
}
