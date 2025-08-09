terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" { default = "us-east-1" }
variable "vpc_id" {}
variable "public_subnets" { type = list(string) }

resource "aws_lb" "api_gw_alb" {
  name               = "ecom-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = []
  subnets            = var.public_subnets
}

resource "aws_lb_target_group" "kong" {
  name     = "kong-tg"
  port     = 8000
  protocol = "HTTP"
  vpc_id   = var.vpc_id
  health_check {
    path = "/"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.api_gw_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.kong.arn
  }
}

output "alb_dns_name" {
  value = aws_lb.api_gw_alb.dns_name
}



