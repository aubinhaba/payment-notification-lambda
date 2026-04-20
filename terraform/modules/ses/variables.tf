variable "project" {
  description = "Project + environment slug used in resource names."
  type        = string
}

variable "from_email" {
  description = "Verified SES identity used as the From: address."
  type        = string
}
