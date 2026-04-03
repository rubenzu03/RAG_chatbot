import jwt
from datetime import datetime, timezone
import os

token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MThAdGVzdC5jb20iLCJpYXQiOjE3NzQ0NzQ3MDAsImV4cCI6MTc3NDU2MTEwMH0.WbSHcluM8dgpRFkPBHJA86G0xmRPKBAhvVx19Fi95AE"
claims = jwt.decode(token, options={"verify_signature": False})
print("iat:", datetime.fromtimestamp(claims["iat"], tz=timezone.utc))
print("exp:", datetime.fromtimestamp(claims["exp"], tz=timezone.utc))
print("now:", datetime.now(timezone.utc))

print("BEARER_TOKEN:", os.environ.get("BEARER_TOKEN"))
print("MODEL_NAME:", os.environ.get("MODEL_NAME"))
