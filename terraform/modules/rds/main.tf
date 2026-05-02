resource "aws_db_subnet_group" "this" {
  name       = "${var.project}-db-subnets"
  subnet_ids = var.subnet_ids
  tags       = { Name = "${var.project}-db-subnets" }
}

resource "aws_db_instance" "this" {
  identifier = "${var.project}-db"

  engine         = "postgres"
  engine_version = "16.13"

  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [var.security_group_id]
  publicly_accessible    = false

  multi_az = false

  skip_final_snapshot        = true
  deletion_protection        = false
  backup_retention_period    = 1
  auto_minor_version_upgrade = true
  apply_immediately          = true

  tags = { Name = "${var.project}-db" }
}
