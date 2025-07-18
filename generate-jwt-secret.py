#!/usr/bin/env python3
"""
JWT Secret Key Generator

This script generates a cryptographically secure 256-bit secret key for JWT token signing.
The generated key is base64-encoded and suitable for production use.
"""

import secrets
import base64

def generate_jwt_secret():
    """Generate a secure 256-bit secret key for JWT signing."""
    # Generate 32 random bytes (256 bits)
    secret_bytes = secrets.token_bytes(32)
    
    # Encode to base64 for easy storage
    secret_base64 = base64.b64encode(secret_bytes).decode('utf-8')
    
    return secret_base64

def main():
    print("🔐 JWT Secret Key Generator")
    print("=" * 50)
    
    # Generate the secret
    secret = generate_jwt_secret()
    
    print(f"\n✅ Generated secure JWT secret:")
    print(f"   {secret}")
    
    print(f"\n📝 Usage Instructions:")
    print(f"   1. Copy the secret above")
    print(f"   2. Set it as an environment variable:")
    print(f"      export JWT_SECRET='{secret}'")
    print(f"   3. Or update application.properties:")
    print(f"      app.jwt.secret={secret}")
    
    print(f"\n⚠️  Security Notes:")
    print(f"   • Keep this secret confidential")
    print(f"   • Use environment variables in production")
    print(f"   • Rotate secrets periodically")
    print(f"   • Never commit secrets to version control")
    
    print(f"\n🔍 Secret Details:")
    print(f"   • Length: {len(secret)} characters")
    print(f"   • Encoding: Base64")
    print(f"   • Entropy: 256 bits")

if __name__ == "__main__":
    main() 