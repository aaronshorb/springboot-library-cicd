provider "aws" {
    region = "us-east-2"
}

resource "aws_instance" "library" {
    ami = "ami-0fb653ca2d3203ac1"
    vpc_security_group_ids = [aws_security_group.library_sec_group.id]
    instance_type = "t3.micro"
    key_name = "library-key"

    tags = {
        Name = "library"
    }
}

resource "aws_security_group" "library_sec_group" {
    name = "library-instance"

    ingress {
        from_port = 8080
        to_port = 8080
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    ingress {
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    egress {
        from_port = 0
        to_port = 0
        protocol = "-1"
        cidr_blocks = ["0.0.0.0/0"]
    }
}

output "public_ip" {
  value = aws_instance.library.public_ip
}
